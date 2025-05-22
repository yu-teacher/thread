package com.hunnit_beasts.thread.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunnit_beasts.thread.controller.DatabaseController;
import com.hunnit_beasts.thread.model.User;
import com.hunnit_beasts.thread.service.DatabaseService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
        // Mock User 생성
        mockUser = new User("testUser", "test@example.com");
        mockUser.setId(1L);

        // Mock ExecutionResult 생성
        mockExecutionResult = new ExecutionResult<>();
        mockExecutionResult.setTaskName("Test DB Query");
        mockExecutionResult.setThreadName("test-thread");
        mockExecutionResult.setThreadId(1L);
        mockExecutionResult.setVirtualThread(true);
        mockExecutionResult.setExecutionTimeMs(200L);
        mockExecutionResult.setSuccess(true);
        mockExecutionResult.setResult(mockUser);

        // Mock ComparisonResult 생성
        mockComparisonResult = new ComparisonResult<>("데이터베이스 쿼리 비교", 10);
        mockComparisonResult.setVirtualThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setPlatformThreadResults(Arrays.asList(mockExecutionResult));
        mockComparisonResult.setVirtualThreadTotalTimeMs(800L);
        mockComparisonResult.setPlatformThreadTotalTimeMs(1500L);
        mockComparisonResult.calculateSpeedup();
    }

    @Test
    void testInitializeData() throws Exception {
        // Given
        doNothing().when(databaseService).initializeTestData();

        // When & Then
        mockMvc.perform(post("/database/init"))
                .andExpect(status().isOk())
                .andExpect(content().string("테스트 데이터가 성공적으로 초기화되었습니다."));
    }

    @Test
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
    void testGetSingleUserWithInvalidId() throws Exception {
        // Given
        when(databaseService.getSingleUser(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/database/single/999"))
                .andExpect(status().isOk());
    }
}
