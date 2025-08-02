package com.mk.springlangchain4j.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseResult {
    private String message;
    private int status;
    private String error;
    private Object data;
}
