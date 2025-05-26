package com.fitness.aiservice.Service;

import com.fitness.aiservice.model.Activity;
//import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.Repository.RecommendationRepository;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j          //This comes from Lombok. It automatically adds a logger called log. You can log messages like log.info(...).
@RequiredArgsConstructor //Lombok annotation that creates a constructor for all final fields
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "activity.queue")       //Listens to messages from RabbitMQ,it is automatically called when there is message on queue
    public void processActivity(Activity activity) {
        log.info("Received activity for processing: {}", activity.getId());  //that message is coming from activity service->rabbit mq -> this service
        log.info("Generated Recommendation: {}", aiService.generateRecommendation(activity));
        Recommendation recommendation = aiService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
    }
}