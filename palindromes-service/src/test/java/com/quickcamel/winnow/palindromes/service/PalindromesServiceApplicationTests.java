package com.quickcamel.winnow.palindromes.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import com.quickcamel.winnow.palindromes.repositories.PalindromeTaskRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PalindromesServiceApplicationTests {

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer()
            .withServices(SQS, DYNAMODB);

    private static AmazonSQSAsync sqs;
    private static AmazonDynamoDB dynamoDB;
    private static String serviceQueueUrl;
    private PalindromeTaskRepository palindromeTaskRepository;

    @Autowired
    public PalindromesServiceApplicationTests(PalindromeTaskRepository palindromeTaskRepository) {
        this.palindromeTaskRepository = palindromeTaskRepository;
    }

    @TestConfiguration
    static class PalindromesRESTEndpointsApplicationTestConfiguration {

        @Primary
        @Bean
        @Lazy
        @Scope(proxyMode = ScopedProxyMode.INTERFACES)
        @SuppressWarnings("unused")
        AmazonSQSAsync amazonLocalstackSqs() {
            return sqs;
        }

        @Primary
        @Bean
        @Lazy
        @Scope(proxyMode = ScopedProxyMode.INTERFACES)
        @SuppressWarnings("unused")
        AmazonDynamoDB amazonLocalstackDynamoDB() {
            return dynamoDB;
        }

        @Primary
        @Bean
        @SuppressWarnings("unused")
        public SimpleMessageListenerContainerFactory testSimpleMessageListenerContainerFactory() {
            SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory = new SimpleMessageListenerContainerFactory();
            simpleMessageListenerContainerFactory.setWaitTimeOut(1);
            simpleMessageListenerContainerFactory.setMaxNumberOfMessages(1);
            return simpleMessageListenerContainerFactory;
        }
    }

    @BeforeAll
    static void setUp() {
        localStack.start();
        sqs = AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();

        serviceQueueUrl = sqs.createQueue("palindrome-service-queue").getQueueUrl();

        dynamoDB = AmazonDynamoDBAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(DYNAMODB))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();

        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDB);
        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(PalindromeTaskEntity.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        TableUtils.createTableIfNotExists(dynamoDB, tableRequest);
    }

    @Test
    void shouldCompleteSubmittedTask() {
        PalindromeTaskEntity entity = persistMockPalindromeTask("I am Anna!");

        sqs.sendMessage(serviceQueueUrl, wrapInNotification(entity.getTask()));

        await().atMost(20, SECONDS).untilAsserted(() -> assertPalindromeTaskCompleted(entity.getTask()));
    }

    private PalindromeTaskEntity persistMockPalindromeTask(String problem) {
        return palindromeTaskRepository.save(new PalindromeTaskEntity()
                .withStatus(PalindromeTaskEntity.Status.SUBMITTED)
                .withText(problem)
                .withSubmitted(System.currentTimeMillis()));
    }

    @NotNull
    private String wrapInNotification(String taskId) {
        return "{" +
                "  \"Type\" : \"Notification\",\n" +
                "  \"Message\" : \"" + taskId + "\"" +
                "}";
    }

    private void assertPalindromeTaskCompleted(String taskId) {
        PalindromeTaskEntity entity = palindromeTaskRepository.findById(taskId).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo(PalindromeTaskEntity.Status.COMPLETED);
        assertThat(entity.getLargestPalindromeLength()).isNotNull();
    }
}
