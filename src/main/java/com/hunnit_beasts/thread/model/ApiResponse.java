package com.hunnit_beasts.thread.model;

import lombok.Data;

@Data
public class ApiResponse {
    private Long id;
    private String title;
    private String body;
    private Integer userId;
}