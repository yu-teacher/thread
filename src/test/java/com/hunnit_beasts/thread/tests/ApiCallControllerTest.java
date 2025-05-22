package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.controller.ApiCallController;
import com.hunnit_beasts.thread.model.ApiResponse;
import com.hunnit_beasts.thread.service.ApiCallService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiCallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiCallService apiCallService;

    @Autowired
    private ObjectMapper objectMapper;

    private ApiResponse mockApiResponse;
    private ExecutionResult<ApiResponse> mockExecutionResult;
    private ComparisonResult<ApiResponse> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock ApiResponse 생성
        mockApiResponse = new ApiResponse();
        // ApiResponse의 필드가 있다면 설정

        // Mock ExecutionResult 생성
        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test API Call");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(100L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult(mockApiResponse);

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("API 호출 비교", 10);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(500L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(1000L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    void testSingleApiCall() throws Exception {
        // Given
        when(apiCallService.callSingleApi(1)).thenReturn(mockApiResponse);

        // When & Then
        mockMvc.perform(get("/api/single"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testMultipleApiCalls() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/multiple")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test API Call"));
    }

    @Test
    void testMultipleApiCallsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(10)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/multiple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCompareApiCalls() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(apiCallService.callMultipleApisWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/compare")
                        .param("count", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("API 호출 비교"))
                .andExpect(jsonPath("$.taskCount").value(20))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    void testCompareApiCallsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<ApiResponse>> mockResults = Arrays.asList(mockExecutionResult);
        when(apiCallService.callMultipleApisWithVirtualThread(50)).thenReturn(mockResults);
        when(apiCallService.callMultipleApisWithPlatformThread(50)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/api/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskCount").value(50));
    }
}
