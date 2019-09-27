package com.quickcamel.winnow.palindromes.service.springconfig;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class PalindromesServiceLocalContext {

    private static final Logger logger = LoggerFactory.getLogger(PalindromesServiceLocalContext.class);

    @Bean
    public AmazonDynamoDB amazonDynamoDB(@Value("${palindrome.localstack.dynamodb.endpoint:http://localhost:4569/}") String endpoint) {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("accesskey", "secretkey")))
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();
    }

    @Bean
    public AmazonSQSAsync amazonSQS(@Value("${palindrome.localstack.sqs.endpoint:http://localhost:4576/}") String endpoint) {
        AmazonSQSAsync amazonSQS = AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("accesskey", "secretkey")))
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();
        awaitLocalstackReady(amazonSQS);
        return amazonSQS;
    }

    private void awaitLocalstackReady(AmazonSQSAsync amazonSQS) {
        while (true) {
            try {
                if (amazonSQS.listQueues().getQueueUrls().size() > 0) {
                    break;
                }
            } catch (final Exception ignored) {
                try {
                    logger.info("Waiting for SQS to be ready - localstack compromise");
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Bean
    public ResourceIdResolver resourceIdResolver(@Value("${palindrome.localstack.sqs.endpoint:http://localhost:4576/}") String endpoint) {
        return logicalResourceId -> endpoint + "queue/" + logicalResourceId;
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory() {
        SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory = new SimpleMessageListenerContainerFactory();
        simpleMessageListenerContainerFactory.setWaitTimeOut(1);
        simpleMessageListenerContainerFactory.setMaxNumberOfMessages(1);
        return simpleMessageListenerContainerFactory;
    }
}
