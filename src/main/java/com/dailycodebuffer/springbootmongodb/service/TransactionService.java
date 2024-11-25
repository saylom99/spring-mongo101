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
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        Date startDate = calendar.getTime();

        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        Date endDate = calendar.getTime();

        return transactionRepository.findByDateBetween(startDate, endDate);
    }

    public Map<String, Object> getMonthlySummary(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        Date startDate = calendar.getTime();

        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        Date endDate = calendar.getTime();

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("date").gte(startDate).lte(endDate)),
                Aggregation.group()
                        .sum("amount").as("totalAmount")
                        .count().as("count")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, "transactions", Document.class
        );

        Document result = results.getUniqueMappedResult();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", result != null ? result.get("totalAmount") : 0);
        summary.put("count", result != null ? result.get("count") : 0);

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