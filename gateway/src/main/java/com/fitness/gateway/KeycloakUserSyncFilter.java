package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


//You're writing a filter in Spring Cloud Gateway.
//Filters in gateway apps are like traffic controllers: they can inspect, modify, or block requests before they reach the downstream microservices.

//Spring Security validates the JWT before anything else.
//
//Request reaches your WebFilter:
//This is a post-authentication filter in Spring WebFlux.Even though Spring already validated it, you are manually reading it again to extract custom fields like:

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter { //WebFilter = like a middleware,Runs for every request.
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization"); //Extracts the JWT token from the request headers.
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        RegisterRequest registerRequest = getUserDetails(token);

        if (userId == null) {
            userId = registerRequest.getKeycloakId();  //if we dont get from header we get from token subs field
        }

        if (userId != null && token != null){
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist) {
                            // Register User

                            if (registerRequest != null) {
                                return userService.registerUser(registerRequest)
                                        .then(Mono.empty());
                            } else {
                                return Mono.empty();
                            }
                        } else {
                            log.info("User already exist, Skipping sync.");
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate() //“Take the current incoming HTTP request and prepare to change something in it.”
                                .header("X-User-ID", finalUserId) //Adds a custom header to the request: X-User-ID, so microservices know who the user is.
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());//Updates the whole exchange with new request and Forwards the request to the actual service
                    }));
        }
        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();  //Parses the JWT
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            //This line parses the raw JWT string (e.g. "eyJhbGciOiJIUzI1NiIs...") into a SignedJWT object.
            //
            //That object represents the structured token — with 3 parts:
            //1) Header
            //2) Payload (claims)
            //3)Signature
            //
            //This is necessary so you can extract the payload (claims) from it.

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();  //Gets values like email, sub (user ID), given_name, etc from claims or payload.

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setKeycloakId(claims.getStringClaim("sub"));
            registerRequest.setPassword("dummy@123123");
            registerRequest.setFirstName(claims.getStringClaim("given_name"));
            registerRequest.setLastName(claims.getStringClaim("family_name"));
            return registerRequest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}