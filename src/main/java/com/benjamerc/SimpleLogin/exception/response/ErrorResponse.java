package com.benjamerc.SimpleLogin.exception.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ErrorResponse {

    private int status;

    private String message;

    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    @Builder.Default
    private List<String> details = new ArrayList<>();

    private String exception;

    private String path;
}
