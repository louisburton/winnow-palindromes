package com.quickcamel.winnow.palindromes.rest;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import com.quickcamel.winnow.palindromes.repositories.PalindromeTaskRepository;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PalindromesRESTEndpointsApplicationTests {

    private static final String QUEUE_NAME = "assertion-queue";
    private static final String TOPIC_NAME = "palindrome-submitted";

    @LocalServerPort
    private int port;

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer()
            .withServices(SQS, SNS, DYNAMODB);

    private final TestRestTemplate testRestTemplate;
    private static AmazonSNS sns;
    private static AmazonSQS sqs;
    private static AmazonDynamoDB dynamoDB;
    private String assertionQueueURL;
    private String submitTopicARN;
    private PalindromeTaskRepository palindromeTaskRepository;

    @Autowired
    public PalindromesRESTEndpointsApplicationTests(TestRestTemplate testRestTemplate, PalindromeTaskRepository palindromeTaskRepository) {
        this.testRestTemplate = testRestTemplate;
        this.palindromeTaskRepository = palindromeTaskRepository;
    }

    @TestConfiguration
    static class PalindromesRESTEndpointsApplicationTestConfiguration {

        @Primary
        @Bean
        @Lazy
        @Scope(proxyMode = ScopedProxyMode.INTERFACES)
        @SuppressWarnings("unused")
        AmazonSNS amazonLocalstackSns() {
            return sns;
        }

        @Primary
        @Bean
        @Lazy
        @Scope(proxyMode = ScopedProxyMode.INTERFACES)
        @SuppressWarnings("unused")
        AmazonDynamoDB amazonLocalstackDynamoDB() {
            return dynamoDB;
        }
    }

    @BeforeAll
    static void setUpClass() {
        localStack.start();
        sqs = AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();

        sns = AmazonSNSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(localStack.getEndpointConfiguration(SNS))
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();

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

    @BeforeEach
    void setUpTest() {
        // Create queue and subscribe to topic to assert topic delivery
        assertionQueueURL = sqs.createQueue(QUEUE_NAME).getQueueUrl();
        submitTopicARN = sns.createTopic(TOPIC_NAME).getTopicArn();
        sns.subscribe(submitTopicARN, "sqs", "arn:aws:sqs:us-east-1:000000000000:assertion-queue");
    }

    @AfterEach
    void tearDownTest() {
        sqs.deleteQueue(assertionQueueURL);
        sns.deleteTopic(submitTopicARN);
    }

    @Test
    void shouldSubmitPalindromeTaskSuccessfully() throws JSONException, IOException {
        HttpEntity<String> request = getTestRequest("I am Anna");
        ResponseEntity<String> response =
                testRestTemplate.postForEntity("http://localhost:" + port + "/palindrome", request, String.class);

        assertThat(response.getStatusCode()).as("check submission's returned status code")
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getContentType())
                .as("check submission's returned Content-Type")
                .satisfies(MediaType.APPLICATION_JSON::isCompatibleWith);
        String taskId = JsonPath.read(response.getBody(), "$.task");
        assertThat(response.getHeaders().getLocation())
                .as("check task location is returned")
                .isNotNull();
        assertThat(response.getHeaders().getLocation().toString())
                .as("check task location is correct as per task ID")
                .isEqualTo("http://localhost:" + port + "/palindrome/" + taskId);
        assertOutputObjectReflectsSubmitted(response.getBody());

        assertRoutedToQueue(taskId);
    }

    @Test
    void shouldQuerySubmittedPalindromeSuccessfully() throws JSONException {
        HttpEntity<String> request = getTestRequest("I am Anna");
        ResponseEntity<String> postResponse =
                testRestTemplate.postForEntity("http://localhost:" + port + "/palindrome", request, String.class);
        String taskId = JsonPath.read(postResponse.getBody(), "$.task");
        ResponseEntity<String> getResponse =
                testRestTemplate.getForEntity("http://localhost:" + port + "/palindrome/" + taskId, String.class);

        assertThat(getResponse.getStatusCode()).as("check status check returned success status code")
                .isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getHeaders().getContentType())
                .as("check status check returned correct Content-Type")
                .satisfies(MediaType.APPLICATION_JSON::isCompatibleWith);
        assertOutputObjectReflectsSubmitted(getResponse.getBody());
    }

    @Test
    void shouldQueryCompletedPalindromeSuccessfully() throws JSONException {
        HttpEntity<String> request = getTestRequest("I am Anna");
        ResponseEntity<String> postResponse =
                testRestTemplate.postForEntity("http://localhost:" + port + "/palindrome", request, String.class);
        String taskId = JsonPath.read(postResponse.getBody(), "$.task");

        emulateCompletedPalindromeAtRepository(taskId);

        ResponseEntity<String> getResponse =
                testRestTemplate.getForEntity("http://localhost:" + port + "/palindrome/" + taskId, String.class);

        String body = getResponse.getBody();
        assertOutputObjectReflectsCompleted(body);
    }

    @Test
    void shouldReturn404ForNonExistentTask() {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity("http://localhost:" + port + "/palindrome/nonexistent", String.class);

        assertThat(response.getStatusCode()).as("check submission's returned status code")
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(JsonPath.<String>read(response.getBody(), "$.message"))
                .as("check error's message")
                .isEqualTo("Palindrome task with ID 'nonexistent' was not found");
    }

    @Test
    void shouldReturnLocalisedError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguage(Locale.LanguageRange.parse("ar-YE;q=1.0"));
        ResponseEntity<String> response =
                testRestTemplate.exchange(
                        "http://localhost:" + port + "/palindrome/nonexistent", HttpMethod.GET, new HttpEntity<>(headers),
                        String.class);

        assertThat(JsonPath.<String>read(response.getBody(), "$.message"))
                .as("check error's message is arabic")
                .isEqualTo("مهمة سياق متناظر ذات الهوية 'nonexistent' لم توجد");
    }

    private void emulateCompletedPalindromeAtRepository(String taskId) {
        PalindromeTaskEntity entity = palindromeTaskRepository.findById(taskId).orElseThrow();
        entity.setStatus(PalindromeTaskEntity.Status.COMPLETED);
        entity.setStarted(System.currentTimeMillis());
        entity.setCompleted(System.currentTimeMillis());
        entity.setLargestPalindrome("Anna");
        entity.setLargestPalindromeLength(4);
        palindromeTaskRepository.save(entity);
    }

    private void assertOutputObjectReflectsCompleted(String body) {
        assertThat(JsonPath.<String>read(body, "$.status"))
                .as("check response status in completed output object")
                .isEqualTo("completed");
        assertThat(JsonPath.<Object>read(body, "$.timestamps.submitted"))
                .as("check response status has submitted time")
                .isNotNull();
        assertThat(JsonPath.<Object>read(body, "$.timestamps.started"))
                .as("check response status has started time")
                .isNotNull();
        assertThat(JsonPath.<Object>read(body, "$.timestamps.completed"))
                .as("check response status has completed time")
                .isNotNull();
        assertThat(JsonPath.<Integer>read(body, "$.solution.largestPalindromeLength"))
                .as("check solution has length 4")
                .isEqualTo(4);
        assertThat(JsonPath.<String>read(body, "$.solution.largestPalindrome"))
                .as("check solution is Anna")
                .isEqualTo("Anna");
    }

    private void assertOutputObjectReflectsSubmitted(String body) {
        assertThat(JsonPath.<String>read(body, "$.status"))
                .as("check response status in output object")
                .isEqualTo("submitted");
        assertThat(JsonPath.<String>read(body, "$.problem.text"))
                .as("check problem in output object")
                .isEqualTo("I am Anna");
        assertThat(JsonPath.<Object>read(body, "$.timestamps.submitted"))
                .as("check response status has submitted time")
                .isNotNull();
        assertThat(JsonPath.<Object>read(body, "$.timestamps.started"))
                .as("check submission hasn't been started")
                .isNull();
        assertThat(JsonPath.<Object>read(body, "$.solution"))
                .as("check solution isn't present")
                .isNull();
    }

    @NotNull
    private HttpEntity<String> getTestRequest(String text) throws JSONException {
        JSONObject testSubmission = new JSONObject()
                .put("problem", new JSONObject().put("text", text));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(testSubmission.toString(), headers);
    }

    private void assertRoutedToQueue(String returnedTaskId) throws IOException {
        Optional<String> sqsJson = sqs.receiveMessage(assertionQueueURL)
                .getMessages()
                .stream()
                .map(Message::getBody)
                .findFirst();
        assertThat(sqsJson).isNotEmpty();
        String submittedTaskId = getPalindromeTaskIdFromSQSMessage(sqsJson.orElseThrow(IllegalStateException::new));
        assertThat(submittedTaskId)
                .as("check submitted palindrome task is sent with a taskId matching what was returned")
                .isEqualTo(returnedTaskId);
    }

    private String getPalindromeTaskIdFromSQSMessage(String sqsJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(sqsJson)
                .get("Message").textValue();
    }
}
