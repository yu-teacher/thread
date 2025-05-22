package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.service.StressTestService;
import com.hunnit_beasts.thread.util.ComparisonResult;
import com.hunnit_beasts.thread.util.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("⚡ 스트레스 테스트 컨트롤러 - 대용량 동시 요청 처리 성능 검증")
class StressTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StressTestService stressTestService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutionResult<String> mockExecutionResult;
    private ComparisonResult<String> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock ExecutionResult 생성
        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test Stress Request");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(50L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult("요청 1 처리 완료");

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("대용량 요청 처리 비교", 1000);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(2000L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(5000L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    @DisplayName("🚀 간단한 대용량 요청 처리 - 500개의 동시 요청을 가상 스레드로 고속 처리")
    void testProcessSimpleRequests() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processMultipleRequestsWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/simple")
                        .param("count", "500"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test Stress Request"));
    }

    @Test
    @DisplayName("🚀📊 간단한 대용량 요청 기본값 - 기본 1000개 요청으로 가상 스레드 확장성 벤치마크")
    void testProcessSimpleRequestsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processMultipleRequestsWithVirtualThread(1000)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/simple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⏱️ 지연 요청 대량 처리 - 200개 요청에 각각 50ms 지연 적용하여 I/O 블로킹 상황 시뮬레이션")
    void testProcessDelayedRequests() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processDelayedRequestsWithVirtualThread(anyInt(), anyLong())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/delayed")
                        .param("count", "200")
                        .param("delayMs", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test Stress Request"));
    }

    @Test
    @DisplayName("⏱️📊 지연 요청 처리 기본값 - 기본 1000개 요청에 100ms 지연으로 표준 블로킹 테스트")
    void testProcessDelayedRequestsWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processDelayedRequestsWithVirtualThread(1000, 100L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/delayed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⚔️ 스트레스 테스트 성능 비교 - 800개 요청(25ms 지연)으로 가상 vs 플랫폼 스레드 한계 성능 측정")
    void testCompareRequestProcessing() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processDelayedRequestsWithVirtualThread(anyInt(), anyLong())).thenReturn(mockResults);
        when(stressTestService.processDelayedRequestsWithPlatformThread(anyInt(), anyLong())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/compare")
                        .param("count", "800")
                        .param("delayMs", "25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("대용량 요청 처리 비교"))
                .andExpect(jsonPath("$.taskCount").value(800))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    @DisplayName("⚔️📊 스트레스 테스트 성능 비교 기본값 - 기본 1000개 요청(50ms 지연)으로 표준 벤치마크")
    void testCompareRequestProcessingWithDefaultParams() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processDelayedRequestsWithVirtualThread(1000, 50L)).thenReturn(mockResults);
        when(stressTestService.processDelayedRequestsWithPlatformThread(1000, 50L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskCount").value(1000));
    }

    @Test
    @DisplayName("🔥 극한 볼륨 스트레스 테스트 - 10,000개 요청으로 가상 스레드의 극한 확장성과 메모리 효율성 검증")
    void testHighVolumeStressTest() throws Exception {
        // Given - 극한 볼륨 테스트
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processMultipleRequestsWithVirtualThread(10000)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/simple")
                        .param("count", "10000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⚡ 저지연 고성능 테스트 - 1000개 요청에 1ms 지연으로 실시간 시스템 처리 능력 검증")
    void testLowDelayStressTest() throws Exception {
        // Given - 낮은 지연 시간 테스트
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockExecutionResult);
        when(stressTestService.processDelayedRequestsWithVirtualThread(1000, 1L)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/stress/delayed")
                        .param("count", "1000")
                        .param("delayMs", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}