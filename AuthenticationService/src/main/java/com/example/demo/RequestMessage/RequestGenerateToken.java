package com.example.demo.RequestMessage;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RequestGenerateToken {
    @NotNull
    @NotBlank
    private String id;
}
