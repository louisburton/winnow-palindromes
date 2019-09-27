package com.quickcamel.winnow.palindromes.repositories;

import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface PalindromeTaskRepository extends CrudRepository<PalindromeTaskEntity, String> {
}
