package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FastApiResponse {
    @JsonProperty("text")
    private String text;
    
    public FastApiResponse() {}
    
    public FastApiResponse(String text) {
        this.text = text;
    }
}

