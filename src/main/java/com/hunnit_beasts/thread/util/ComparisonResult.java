package com.hunnit_beasts.thread.util;

import lombok.Data;
import java.util.List;

@Data
public class ComparisonResult<T> {
    private String scenarioName;
    private int taskCount;
    private long virtualThreadTotalTimeMs;
    private long platformThreadTotalTimeMs;
    private double speedupFactor;
    private List<ExecutionResult<T>> virtualThreadResults;
    private List<ExecutionResult<T>> platformThreadResults;

    public ComparisonResult(String scenarioName, int taskCount) {
        this.scenarioName = scenarioName;
        this.taskCount = taskCount;
    }

    public void calculateSpeedup() {
        if (platformThreadTotalTimeMs > 0) {
            this.speedupFactor = (double) platformThreadTotalTimeMs / virtualThreadTotalTimeMs;
        }
    }
}