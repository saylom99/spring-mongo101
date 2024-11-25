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

import java.math.*;
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
                Aggregation.group("type")
                        .sum("amount").as("totalAmount")
                        .count().as("count")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "transactions",
                Document.class
        );

        Map<String, Object> summary = new HashMap<>();
        List<Map<String, Object>> typeDetails = new ArrayList<>();

        for (Document result : results) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("type", result.getString("_id"));
            detail.put("totalAmount", ((Number) result.get("totalAmount")).doubleValue());
            detail.put("count", ((Number) result.get("count")).longValue());
            typeDetails.add(detail);
        }

        summary.put("types", typeDetails);
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


        BigDecimal total = results.stream()
                .map(doc -> getBigDecimal(doc.get("count")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return results.stream()
                .map(doc -> {
                    BigDecimal count = getBigDecimal(doc.get("count"));
                    BigDecimal percentage = total.equals(BigDecimal.ZERO)
                            ? BigDecimal.ZERO
                            : count.multiply(new BigDecimal("100"))
                            .divide(total, 2, RoundingMode.HALF_UP);

                    return TypeDistributionDTO.builder()
                            .type(doc.getString("type"))
                            .percentage(percentage.doubleValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(((Number) value).toString());
    }
}