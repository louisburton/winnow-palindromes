package com.quickcamel.winnow.palindromes.rest.springconfig;

import com.amazonaws.services.sns.AmazonSNS;
import com.quickcamel.winnow.palindromes.rest.task.SubmissionObserver;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.cloud.aws.messaging.core.TopicMessageChannel;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.messaging.core.DestinationResolvingMessageSendingOperations;

@Configuration
@EnableDynamoDBRepositories(basePackages = "com.quickcamel.winnow.palindromes.repositories")
@EntityScan(basePackages = "com.quickcamel.winnow.palindromes.entities")
public class PalindromesRESTEndpointsContext {

    private DestinationResolvingMessageSendingOperations<TopicMessageChannel> messagingTemplate;

    public PalindromesRESTEndpointsContext(AmazonSNS amazonSNS) {
        this.messagingTemplate = new NotificationMessagingTemplate(amazonSNS);
    }

    @Bean
    SubmissionObserver newTaskNotifier(@Value("${palindrome.newtask.notification.destination:palindrome-submitted}") String destination) {
        return payload -> messagingTemplate.convertAndSend(destination, payload.getTask());
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource);
    }
}
