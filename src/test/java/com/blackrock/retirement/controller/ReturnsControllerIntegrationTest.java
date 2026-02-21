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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Test type: Integration
 * 2. Validation to be executed: Test NPS and Index returns calculation endpoints
 * 3. Command with the necessary arguments for execution: mvn test -Dtest=ReturnsControllerIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ReturnsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testNPSReturnsEndpoint() throws Exception {
        List<TransactionItem> transactions = Arrays.asList(
                TransactionItem.builder().date("2023-10-12 20:15:30").amount(2512.0).build(),
                TransactionItem.builder().date("2023-10-13 20:15:30").amount(1500.0).build()
        );

        List<FilterPeriodK> kPeriods = Arrays.asList(
                FilterPeriodK.builder().start("2023-10-12 20:15:30").end("2023-10-26 20:15:30").build()
        );

        ReturnsRequest request = ReturnsRequest.builder()
                .wage(50000.0)
                .inflation(5.5)
                .age(29)
                .transactions(transactions)
                .k(kPeriods)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/returns:nps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionsTotalAmount").exists())
                .andExpect(jsonPath("$.transactionsTotalCeiling").exists())
                .andExpect(jsonPath("$.savingsByDates").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ReturnsResponse response = objectMapper.readValue(responseBody, ReturnsResponse.class);

        assertNotNull(response);
        assertNotNull(response.getTransactionsTotalAmount());
        assertNotNull(response.getTransactionsTotalCeiling());
        assertNotNull(response.getSavingsByDates());
        assertTrue(response.getTransactionsTotalAmount() > 0);
        assertTrue(response.getTransactionsTotalCeiling() >= response.getTransactionsTotalAmount());
        assertEquals(1, response.getSavingsByDates().size());
        assertTrue(response.getSavingsByDates().get(0).getTaxBenefit() >= 0);
    }

    @Test
    public void testIndexReturnsEndpoint() throws Exception {
        List<TransactionItem> transactions = Arrays.asList(
                TransactionItem.builder().date("2023-10-12 20:15:30").amount(2512.0).build(),
                TransactionItem.builder().date("2023-10-13 20:15:30").amount(1500.0).build()
        );

        List<FilterPeriodK> kPeriods = Arrays.asList(
                FilterPeriodK.builder().start("2023-10-12 20:15:30").end("2023-10-26 20:15:30").build()
        );

        ReturnsRequest request = ReturnsRequest.builder()
                .wage(50000.0)
                .inflation(5.5)
                .age(29)
                .transactions(transactions)
                .k(kPeriods)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/returns:index")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionsTotalAmount").exists())
                .andExpect(jsonPath("$.transactionsTotalCeiling").exists())
                .andExpect(jsonPath("$.savingsByDates").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ReturnsResponse response = objectMapper.readValue(responseBody, ReturnsResponse.class);

        assertNotNull(response);
        assertNotNull(response.getTransactionsTotalAmount());
        assertNotNull(response.getTransactionsTotalCeiling());
        assertNotNull(response.getSavingsByDates());
        assertTrue(response.getTransactionsTotalAmount() > 0);
        assertTrue(response.getTransactionsTotalCeiling() >= response.getTransactionsTotalAmount());
        assertEquals(1, response.getSavingsByDates().size());
        assertEquals(0.0, response.getSavingsByDates().get(0).getTaxBenefit());
    }

    @Test
    public void testNPSWithHigherRate() throws Exception {
        // Index Fund has higher rate (14.49% vs 7.11%)
        List<TransactionItem> transactions = Arrays.asList(
                TransactionItem.builder().date("2023-10-12 20:15:30").amount(375.0).build()  // remanent = 25
        );

        List<FilterPeriodK> kPeriods = Arrays.asList(
                FilterPeriodK.builder().start("2023-10-12 20:15:30").end("2024-10-12 20:15:30").build()
        );

        ReturnsRequest npsRequest = ReturnsRequest.builder()
                .wage(50000.0)
                .inflation(3.0)
                .age(30)
                .transactions(transactions)
                .k(kPeriods)
                .build();

        ReturnsRequest indexRequest = ReturnsRequest.builder()
                .wage(50000.0)
                .inflation(3.0)
                .age(30)
                .transactions(transactions)
                .k(kPeriods)
                .build();

        // Calculate NPS
        String npsBody = objectMapper.writeValueAsString(npsRequest);
        MvcResult npsResult = mockMvc.perform(post("/blackrock/challenge/v1/returns:nps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(npsBody))
                .andExpect(status().isOk())
                .andReturn();
        ReturnsResponse npsResponse = objectMapper.readValue(
                npsResult.getResponse().getContentAsString(), ReturnsResponse.class);

        // Calculate Index
        String indexBody = objectMapper.writeValueAsString(indexRequest);
        MvcResult indexResult = mockMvc.perform(post("/blackrock/challenge/v1/returns:index")
                .contentType(MediaType.APPLICATION_JSON)
                .content(indexBody))
                .andExpect(status().isOk())
                .andReturn();
        ReturnsResponse indexResponse = objectMapper.readValue(
                indexResult.getResponse().getContentAsString(), ReturnsResponse.class);

        // Index should have higher profit due to higher rate
        assertTrue(indexResponse.getSavingsByDates().get(0).getProfit() > 
                   npsResponse.getSavingsByDates().get(0).getProfit());
    }

    @Test
    public void testNPSTaxBenefitZeroSalary() throws Exception {
        List<TransactionItem> transactions = Arrays.asList(
                TransactionItem.builder().date("2023-10-12 20:15:30").amount(1000.0).build()
        );

        List<FilterPeriodK> kPeriods = Arrays.asList(
                FilterPeriodK.builder().start("2023-10-12 20:15:30").end("2023-11-12 20:15:30").build()
        );

        ReturnsRequest request = ReturnsRequest.builder()
                .wage(0.0)
                .inflation(3.0)
                .age(30)
                .transactions(transactions)
                .k(kPeriods)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/returns:nps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        ReturnsResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ReturnsResponse.class);

        assertEquals(0.0, response.getSavingsByDates().get(0).getTaxBenefit());
    }
}
