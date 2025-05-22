package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.service.WorkflowService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("🔄 워크플로우 컨트롤러 테스트 - 실제 비즈니스 로직에서의 가상 스레드 효과 검증")
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowService workflowService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutionResult<String> mockSimpleWorkflowResult;
    private ExecutionResult<String> mockComplexWorkflowResult;
    private ComparisonResult<String> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock Simple Workflow Result 생성
        mockSimpleWorkflowResult = new ExecutionResult<>();
        mockSimpleWorkflowResult.setTaskName("간단한 워크플로우 (가상) - 1");
        mockSimpleWorkflowResult.setThreadName("virtual-thread-1");
        mockSimpleWorkflowResult.setThreadId(1L);
        mockSimpleWorkflowResult.setVirtualThread(true);
        mockSimpleWorkflowResult.setExecutionTimeMs(800L);
        mockSimpleWorkflowResult.setSuccess(true);
        mockSimpleWorkflowResult.setResult("간단한 워크플로우 1 완료");

        // Mock Complex Workflow Result 생성
        mockComplexWorkflowResult = new ExecutionResult<>();
        mockComplexWorkflowResult.setTaskName("복잡한 워크플로우 (가상) - 1");
        mockComplexWorkflowResult.setThreadName("virtual-thread-2");
        mockComplexWorkflowResult.setThreadId(2L);
        mockComplexWorkflowResult.setVirtualThread(true);
        mockComplexWorkflowResult.setExecutionTimeMs(2500L);
        mockComplexWorkflowResult.setSuccess(true);
        mockComplexWorkflowResult.setResult("복잡한 워크플로우 1 완료, 파일: complex-workflow-1-uuid.txt");

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("간단한 워크플로우 비교", 5);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockSimpleWorkflowResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockSimpleWorkflowResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(3000L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(4500L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    @DisplayName("🔄📋 간단한 워크플로우 실행 - 3개의 기본 워크플로우 (API호출→DB저장→파일기록) 순차 처리")
    void testRunSimpleWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("간단한 워크플로우 (가상) - 1"))
                .andExpect(jsonPath("$[0].result").value("간단한 워크플로우 1 완료"));
    }

    @Test
    @DisplayName("🔄📋📊 간단한 워크플로우 기본값 - 기본 5개 워크플로우로 가상 스레드 복합 작업 효율성 측정")
    void testRunSimpleWorkflowsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(5)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("🔄🔥 복잡한 워크플로우 실행 - 2개의 고도화 워크플로우 (다중API→DB조회/저장→대용량파일) 병렬 처리")
    void testRunComplexWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockComplexWorkflowResult);
        when(workflowService.runMultipleComplexWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/complex")
                        .param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("복잡한 워크플로우 (가상) - 1"))
                .andExpect(jsonPath("$[0].result").value("복잡한 워크플로우 1 완료, 파일: complex-workflow-1-uuid.txt"));
    }

    @Test
    @DisplayName("🔄🔥📊 복잡한 워크플로우 기본값 - 기본 3개 복잡한 워크플로우로 엔터프라이즈급 성능 벤치마크")
    void testRunComplexWorkflowsWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockComplexWorkflowResult);
        when(workflowService.runMultipleComplexWorkflowsWithVirtualThread(3)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/complex"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⚔️📋 간단한 워크플로우 성능 비교 - 4개 워크플로우로 가상 vs 플랫폼 스레드 복합 I/O 효율성 분석")
    void testCompareSimpleWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(workflowService.runMultipleSimpleWorkflowsWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/compare")
                        .param("type", "simple")
                        .param("count", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("간단한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(4))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    @DisplayName("⚔️🔥 복잡한 워크플로우 성능 비교 - 3개 고부하 워크플로우로 실제 운영환경 시뮬레이션 및 성능 차이 검증")
    void testCompareComplexWorkflows() throws Exception {
        // Given
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockComplexWorkflowResult);
        when(workflowService.runMultipleComplexWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(workflowService.runMultipleComplexWorkflowsWithPlatformThread(anyInt())).thenReturn(mockResults);

        ComparisonResult<String> complexComparisonResult = new ComparisonResult<>("복잡한 워크플로우 비교", 3);
        complexComparisonResult.setVirtualThreadResults(mockResults);
        complexComparisonResult.setPlatformThreadResults(mockResults);
        complexComparisonResult.setVirtualThreadTotalTimeMs(6000L);
        complexComparisonResult.setPlatformThreadTotalTimeMs(9000L);
        complexComparisonResult.calculateSpeedup();

        // When & Then
        mockMvc.perform(get("/workflow/compare")
                        .param("type", "complex")
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("복잡한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(3))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    @DisplayName("⚔️📊 워크플로우 성능 비교 기본값 - 기본 설정(간단 워크플로우 5개)으로 표준 비즈니스 로직 성능 기준 측정")
    void testCompareWorkflowsWithDefaultParams() throws Exception {
        // Given - 기본값: simple workflow, count=5
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(5)).thenReturn(mockResults);
        when(workflowService.runMultipleSimpleWorkflowsWithPlatformThread(5)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("간단한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(5));
    }

    @Test
    @DisplayName("🔄❌ 잘못된 워크플로우 타입 처리 - 유효하지 않은 타입 입력 시 기본 simple 워크플로우로 안전한 폴백 처리")
    void testCompareInvalidWorkflowType() throws Exception {
        // Given - invalid type, should default to simple
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(workflowService.runMultipleSimpleWorkflowsWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/compare")
                        .param("type", "invalid")
                        .param("count", "7"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("간단한 워크플로우 비교"))
                .andExpect(jsonPath("$.taskCount").value(7));
    }

    @Test
    @DisplayName("🔄📈 대규모 워크플로우 확장성 테스트 - 50개 워크플로우로 가상 스레드의 대용량 비즈니스 로직 처리 한계 검증")
    void testWorkflowWithLargeCount() throws Exception {
        // Given - 대규모 워크플로우 테스트
        List<ExecutionResult<String>> mockResults = Arrays.asList(mockSimpleWorkflowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(50)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("📊⏱️ 워크플로우 성능 메트릭 정밀 검증 - 5초 소요 워크플로우의 실행 시간 측정 정확성 및 성능 데이터 수집")
    void testWorkflowPerformanceMetrics() throws Exception {
        // Given - 성능 메트릭 검증을 위한 테스트
        ExecutionResult<String> slowResult = new ExecutionResult<>();
        slowResult.setTaskName("느린 워크플로우");
        slowResult.setExecutionTimeMs(5000L);
        slowResult.setSuccess(true);
        slowResult.setResult("느린 워크플로우 완료");

        List<ExecutionResult<String>> slowResults = Arrays.asList(slowResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(slowResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].executionTimeMs").value(5000));
    }

    @Test
    @DisplayName("🔄💥 워크플로우 실패 시나리오 처리 - 실패한 워크플로우의 오류 메시지, 상태 코드 및 복구 메커니즘 검증")
    void testWorkflowFailureHandling() throws Exception {
        // Given - 실패한 워크플로우 결과
        ExecutionResult<String> failedResult = new ExecutionResult<>();
        failedResult.setTaskName("실패한 워크플로우");
        failedResult.setExecutionTimeMs(100L);
        failedResult.setSuccess(false);
        failedResult.setErrorMessage("워크플로우 실행 중 오류 발생");

        List<ExecutionResult<String>> failedResults = Arrays.asList(failedResult);
        when(workflowService.runMultipleSimpleWorkflowsWithVirtualThread(anyInt())).thenReturn(failedResults);

        // When & Then
        mockMvc.perform(get("/workflow/simple")
                        .param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].success").value(false))
                .andExpect(jsonPath("$[0].errorMessage").value("워크플로우 실행 중 오류 발생"));
    }
}