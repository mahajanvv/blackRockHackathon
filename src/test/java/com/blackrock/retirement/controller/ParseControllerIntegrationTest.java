package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.ParseTransactionResponse;
import com.blackrock.retirement.dto.TransactionItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Integration
 * 2. Validation to be executed: Test parse endpoint integration with Spring Boot context
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=ParseControllerIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ParseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testParseEndpointSuccess() throws Exception {
        List<TransactionItem> items = Arrays.asList(
                TransactionItem.builder()
                        .date("2023-10-12 20:15:30")
                        .amount(250.0)
                        .build(),
                TransactionItem.builder()
                        .date("2023-02-28 15:49:20")
                        .amount(375.0)
                        .build(),
                TransactionItem.builder()
                        .date("2023-12-01 08:30:45")
                        .amount(99.99)
                        .build()
        );

        String requestBody = objectMapper.writeValueAsString(items);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:parse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ParseTransactionResponse[] responses = objectMapper.readValue(responseBody, ParseTransactionResponse[].class);

        assertNotNull(responses);
        assertEquals(3, responses.length);
        
        // Verify first transaction
        assertEquals("2023-10-12 20:15:30", responses[0].getDate());
        assertEquals(250.0, responses[0].getAmount());
        assertEquals(300.0, responses[0].getCeiling());
        assertEquals(50.0, responses[0].getRemanent());
        
        // Verify second transaction
        assertEquals("2023-02-28 15:49:20", responses[1].getDate());
        assertEquals(375.0, responses[1].getAmount());
        assertEquals(400.0, responses[1].getCeiling());
        assertEquals(25.0, responses[1].getRemanent());
    }

    @Test
    public void testParseEndpointWithNegativeAmounts() throws Exception {
        List<TransactionItem> items = Arrays.asList(
                TransactionItem.builder()
                        .date("2023-10-12 20:15:30")
                        .amount(150.0)
                        .build(),
                TransactionItem.builder()
                        .date("2023-02-28 15:49:20")
                        .amount(-50.0)
                        .build()
        );

        String requestBody = objectMapper.writeValueAsString(items);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:parse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ParseTransactionResponse[] responses = objectMapper.readValue(responseBody, ParseTransactionResponse[].class);

        assertEquals(2, responses.length);
        // First should be valid with ceiling 200
        assertEquals(150.0, responses[0].getAmount());
        assertEquals(200.0, responses[0].getCeiling());
        
        // Second should have negative amount - ceiling and remanent will be null for invalid
        assertEquals(-50.0, responses[1].getAmount());
        assertNull(responses[1].getCeiling());  // null because amount is invalid
    }

    @Test
    public void testParseEndpointEmptyArray() throws Exception {
        List<TransactionItem> items = Arrays.asList();

        String requestBody = objectMapper.writeValueAsString(items);

        mockMvc.perform(post("/blackrock/challenge/v1/transactions:parse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

