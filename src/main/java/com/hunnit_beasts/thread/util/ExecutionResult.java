package com.hunnit_beasts.thread.util;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExecutionResult<T> {
    private String taskName;
    private String threadName;
    private long threadId;
    private boolean isVirtualThread;
    private long executionTimeMs;
    private boolean success;
    private T result;
    private String errorMessage;
}