package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.ValidatorRequest;
import com.blackrock.retirement.dto.ValidatorResponse;
import com.blackrock.retirement.dto.ValidatorTransactionItem;
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
 * 2. Validation to be executed: Test validator endpoint with wage and transaction validation
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=ValidatorControllerIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ValidatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testValidatorEndpointWithValidTransactions() throws Exception {
        ValidatorRequest request = ValidatorRequest.builder()
                .wage(50000.0)
                .transactions(Arrays.asList(
                        ValidatorTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(2512.0)
                                .ceiling(2600.0)
                                .remanent(88.0)
                                .build(),
                        ValidatorTransactionItem.builder()
                                .date("2023-02-28 15:49:20")
                                .amount(3750.0)
                                .ceiling(3800.0)
                                .remanent(50.0)
                                .build()
                ))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:validator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").isArray())
                .andExpect(jsonPath("$.invalid").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ValidatorResponse response = objectMapper.readValue(responseBody, ValidatorResponse.class);

        assertNotNull(response);
        assertNotNull(response.getValid());
        assertNotNull(response.getInvalid());
        assertEquals(2, response.getValid().size());
        assertEquals(0, response.getInvalid().size());
    }

    @Test
    public void testValidatorEndpointDetectDuplicates() throws Exception {
        ValidatorRequest request = ValidatorRequest.builder()
                .wage(50000.0)
                .transactions(Arrays.asList(
                        ValidatorTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(2512.0)
                                .ceiling(2600.0)
                                .remanent(88.0)
                                .build(),
                        ValidatorTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(2512.0)
                                .ceiling(2600.0)
                                .remanent(88.0)
                                .build(),
                        ValidatorTransactionItem.builder()
                                .date("2023-10-12 20:15:30")
                                .amount(2512.0)
                                .ceiling(2600.0)
                                .remanent(88.0)
                                .build()
                ))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/transactions:validator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").isArray())
                .andExpect(jsonPath("$.invalid").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ValidatorResponse response = objectMapper.readValue(responseBody, ValidatorResponse.class);

        assertNotNull(response);
        // 1 valid transaction, 2 duplicates marked as invalid
        assertEquals(1, response.getValid().size());
        assertEquals(2, response.getInvalid().size());
        
        // Check duplicate messages
        assertTrue(response.getInvalid().get(0).getMessage().contains("Duplicate"));
    }

    @Test
    public void testValidatorEndpointEmptyTransactions() throws Exception {
        ValidatorRequest request = ValidatorRequest.builder()
                .wage(50000.0)
                .transactions(Arrays.asList())
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/blackrock/challenge/v1/transactions:validator")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").isArray())
                .andExpect(jsonPath("$.invalid").isArray());
    }
}
