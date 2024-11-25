package com.dailycodebuffer.springbootmongodb.dto;

import lombok.*;

@Data
@Builder
public class TypeDistributionDTO {
    private String type;
    private Double percentage;
}
