package com.quickcamel.winnow.palindromes.service.springconfig;


import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDynamoDBRepositories(basePackages = "com.quickcamel.winnow.palindromes.repositories")
@EntityScan(basePackages = "com.quickcamel.winnow.palindromes.entities")
public class PalindromesServiceContext {
}
