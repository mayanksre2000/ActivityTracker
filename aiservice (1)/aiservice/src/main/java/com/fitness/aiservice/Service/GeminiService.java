package com.fitness.aiservice.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient; //WebClient is a way to make HTTP calls from your Java code.
    // Think of it like your browser sending a form to a website. In this case, it sends a prompt to Gemini AI and waits for the response.

    @Value("${gemini.api.url}") //see config and environmental variables for these info.
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

     //we have to give gemini our question in a particular
        //    {
        //  "contents": [
        //    {
        //      "parts": [
        //        {
        //          "text": "User’s question or prompt here"
        //        }
        //      ]
        //    }
        //  ]
        //}

    public String getAnswer(String question) {
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", question)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json") //Tells the server: "I'm sending JSON data in this request."
                .bodyValue(requestBody)//this sends the requestBody created earlier (Java Map) as JSON.
                .retrieve()
                .bodyToMono(String.class) //Tells Spring: “I expect the response body to be a String.”
                .block();
        //A Mono<T> is a type from Project Reactor, used in Reactive Programming.
        //It represents a single asynchronous value (or no value) that will be available sometime in the future.
        //
        //Think of it like a Promise in JavaScript or a Future in other languages, but designed to work reactively and non-blocking.
        //🚀 Why use Mono?
        //Because WebClient is non-blocking and reactive, it doesn't wait for the response to finish before moving on. It returns a Mono, and you can do things like:
            //result.map(response -> doSomething(response))
        //But in your code, you're using:
            //.block();
        //Which forces it to wait for the value synchronously, converting the Mono<String> into just a regular String.

        return response;
    }
}