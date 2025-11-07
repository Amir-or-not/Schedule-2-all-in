package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FastApiRequest {
    @JsonProperty("prompt")
    private String prompt;
    
    public FastApiRequest() {}
    
    public FastApiRequest(String prompt) {
        this.prompt = prompt;
    }
}

