package com.dailycodebuffer.springbootmongodb.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

@Document(collection = "transactions")
@Data
public class Transaction {
    @Id
    private String id;
    private Date date;
    private String type;
    private Double amount;

}

