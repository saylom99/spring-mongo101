package com.dailycodebuffer.springbootmongodb.service;

import com.dailycodebuffer.springbootmongodb.dto.*;
import com.dailycodebuffer.springbootmongodb.model.*;
import com.dailycodebuffer.springbootmongodb.repository.*;
import org.bson.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Transaction> getTransactionsByMonth(int year, int month) {

        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        return transactionRepository.findByDateBetween(startOfMonth, endOfMonth);
    }

    public Map<String, Object> getMonthlySummary(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59);


        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("date").gte(startOfMonth).lte(endOfMonth)
                ),
                Aggregation.group()
                        .sum("amount").as("totalAmount")
                        .count().as("count")
        );

        Document result = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                Document.class
        ).getUniqueMappedResult();


        Map<String, Object> summary = new HashMap<>();
        if (result != null) {
            Number totalAmount = (Number) result.get("totalAmount");
            Number count = (Number) result.get("count");

            summary.put("totalAmount", totalAmount != null ? totalAmount.doubleValue() : 0.0);
            summary.put("count", count != null ? count.longValue() : 0L);
        } else {
            summary.put("totalAmount", 0.0);
            summary.put("count", 0L);
        }

        return summary;
    }

    public List<TypeDistributionDTO> getTypeDistribution(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("date").gte(startOfMonth).lte(endOfMonth)
                ),
                Aggregation.group("type")
                        .count().as("count"),
                Aggregation.project("count")
                        .and("_id").as("type")
        );

        List<Document> results = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                Document.class
        ).getMappedResults();


        double total = results.stream()
                .mapToDouble(doc -> ((Number) doc.get("count")).doubleValue())
                .sum();

        return results.stream()
                .map(doc -> TypeDistributionDTO.builder()
                        .type(doc.getString("type"))
                        .percentage(((Number) doc.get("count")).doubleValue() / total * 100)
                        .build())
                .collect(Collectors.toList());
    }
}