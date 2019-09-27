package com.quickcamel.winnow.palindromes.rest.springconfig;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class PalindromesRESTEndpointsLocalContext {

    @Bean
    public AmazonDynamoDB amazonDynamoDB(@Value("${palindrome.localstack.dynamodb.endpoint:http://localhost:4569/}") String endpoint) {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("accesskey", "secretkey")))
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();
    }

    @Bean
    public AmazonSNS amazonSNS(@Value("${palindrome.localstack.sns.endpoint:http://localhost:4575/}") String endpoint) {
        return AmazonSNSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("accesskey", "secretkey")))
                .withClientConfiguration(new ClientConfiguration().withSocketTimeout(2000))
                .build();
    }
}
