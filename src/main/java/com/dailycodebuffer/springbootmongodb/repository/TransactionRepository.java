package com.dailycodebuffer.springbootmongodb.repository;

import com.dailycodebuffer.springbootmongodb.model.*;
import org.springframework.data.mongodb.repository.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    @Query("{'date': {$gte: ?0, $lte: ?1}}")
    List<Transaction> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
