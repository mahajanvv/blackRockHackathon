package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Integration
 * 2. Validation to be executed: Test filter endpoint with temporal rules (Q, P, K)
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=FilterControllerIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FilterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFilterEndpointWithQRules() throws Exception {
        FilterRequest request = FilterRequest.builder()
                .wage(50000.0)
                .transactions(Arrays.asList(
                        FilterTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(150.0)
                                .ceiling(200.0)
                                .remanent(50.0)
                                .build()
                ))
                .q(Arrays.asList(
                        FilterPeriodQ.builder()
                                .fixed(100.0)
                                .start("2023-10-10 10:00:00")
                                .end("2023-10-15 10:00:00")
                                .build()
                ))
                .p(Arrays.asList())
                .k(Arrays.asList())
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid.length()").value(0))  // Transaction in Q period is excluded
                .andExpect(jsonPath("$.invalid.length()").value(0))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        FilterResponse response = objectMapper.readValue(responseBody, FilterResponse.class);

        assertNotNull(response);
        assertEquals(0, response.getValid().size());  // Transaction in Q period is excluded
        assertEquals(0, response.getInvalid().size());
    }

    @Test
    public void testFilterEndpointWithPRules() throws Exception {
        FilterRequest request = FilterRequest.builder()
                .wage(50000.0)
                .transactions(Arrays.asList(
                        FilterTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(150.0)
                                .ceiling(200.0)
                                .remanent(50.0)
                                .build()
                ))
                .q(Arrays.asList())
                .p(Arrays.asList(
                        FilterPeriodP.builder()
                                .extra(25.0)
                                .start("2023-10-10 10:00:00")
                                .end("2023-10-15 10:00:00")
                                .build()
                ))
                .k(Arrays.asList())
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        FilterResponse response = objectMapper.readValue(responseBody, FilterResponse.class);

        assertEquals(1, response.getValid().size());
        assertEquals(0, response.getInvalid().size());
        assertEquals(150.0, response.getValid().get(0).getAmount());
    }

    @Test
    public void testFilterEndpointWithKGrouping() throws Exception {
        FilterRequest request = FilterRequest.builder()
                .wage(50000.0)
                .transactions(Arrays.asList(
                        FilterTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(150.0)
                                .ceiling(200.0)
                                .remanent(50.0)
                                .build(),
                        FilterTransactionItem.builder()
                                .date("2023-10-20 20:15:30")
                                .amount(250.0)
                                .ceiling(300.0)
                                .remanent(50.0)
                                .build()
                ))
                .q(Arrays.asList())
                .p(Arrays.asList())
                .k(Arrays.asList(
                        FilterPeriodK.builder()
                                .start("2023-10-10 10:00:00")
                                .end("2023-10-15 10:00:00")
                                .build(),
                        FilterPeriodK.builder()
                                .start("2023-10-15 10:00:00")
                                .end("2023-10-25 10:00:00")
                                .build()
                ))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        FilterResponse response = objectMapper.readValue(responseBody, FilterResponse.class);

        assertEquals(2, response.getValid().size());
        assertEquals(0, response.getInvalid().size());
        // First transaction should be in k_0 period
        assertEquals(true, response.getValid().get(0).getInKPeriod());
        // Second transaction should be in k_1 period
        assertEquals(true, response.getValid().get(1).getInKPeriod());
    }
}
