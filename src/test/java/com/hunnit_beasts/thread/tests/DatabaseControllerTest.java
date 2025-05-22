package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.controller.DatabaseController;
import com.hunnit_beasts.thread.model.User;
import com.hunnit_beasts.thread.service.DatabaseService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("🗄️ 데이터베이스 컨트롤러 테스트")
class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatabaseService databaseService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private ExecutionResult<User> mockExecutionResult;
    private ComparisonResult<User> mockComparisonResult;

    @BeforeEach
    void setUp() {
        // Mock 객체 설정
        mockUser = new User("testUser", "test@example.com");
        mockUser.setId(1L);

        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test DB Query");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(200L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult(mockUser);

        mockComparisonResult = new ComparisonResult<>("데이터베이스 쿼리 비교", 10);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(800L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(1500L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    @DisplayName("🚀 데이터베이스 초기화 - 사용자 및 제품 테스트 데이터 생성")
    void testInitializeData() throws Exception {
        // Given
        doNothing().when(databaseService).initializeTestData();

        // When & Then
        mockMvc.perform(post("/database/init"))
                .andExpect(status().isOk())
                .andExpect(content().string("테스트 데이터가 성공적으로 초기화되었습니다."));
    }

    @Test
    @DisplayName("👤 단일 사용자 조회 - ID로 특정 사용자 정보 조회")
    void testGetSingleUser() throws Exception {
        // Given
        when(databaseService.getSingleUser(anyLong())).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/database/single/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("👥 배치 사용자 조회 - 5명의 사용자를 가상 스레드로 동시 조회")
    void testBatchGetUsers() throws Exception {
        // Given
        List<ExecutionResult<User>> mockResults = Arrays.asList(mockExecutionResult);
        when(databaseService.batchGetUsersWithVirtualThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/database/batch")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].taskName").value("Test DB Query"));
    }

    @Test
    @DisplayName("👥📋 배치 사용자 조회 기본값 - 기본 10명 사용자 조회")
    void testBatchGetUsersWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<User>> mockResults = Arrays.asList(mockExecutionResult);
        when(databaseService.batchGetUsersWithVirtualThread(10)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/database/batch"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("⚔️ DB 쿼리 성능 비교 - 30개 쿼리로 가상 vs 플랫폼 스레드 성능 측정")
    void testCompareDbQueries() throws Exception {
        // Given
        List<ExecutionResult<User>> mockResults = Arrays.asList(mockExecutionResult);
        when(databaseService.batchGetUsersWithVirtualThread(anyInt())).thenReturn(mockResults);
        when(databaseService.batchGetUsersWithPlatformThread(anyInt())).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/database/compare")
                        .param("count", "30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioName").value("데이터베이스 쿼리 비교"))
                .andExpect(jsonPath("$.taskCount").value(30))
                .andExpect(jsonPath("$.speedupFactor").exists());
    }

    @Test
    @DisplayName("⚔️📋 DB 쿼리 성능 비교 기본값 - 기본 50개 쿼리로 벤치마크")
    void testCompareDbQueriesWithDefaultCount() throws Exception {
        // Given
        List<ExecutionResult<User>> mockResults = Arrays.asList(mockExecutionResult);
        when(databaseService.batchGetUsersWithVirtualThread(50)).thenReturn(mockResults);
        when(databaseService.batchGetUsersWithPlatformThread(50)).thenReturn(mockResults);

        // When & Then
        mockMvc.perform(get("/database/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskCount").value(50));
    }

    @Test
    @DisplayName("❌ 존재하지 않는 사용자 조회 - 잘못된 ID로 조회 시 처리")
    void testGetSingleUserWithInvalidId() throws Exception {
        // Given
        when(databaseService.getSingleUser(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/database/single/999"))
                .andExpect(status().isOk());
    }
}