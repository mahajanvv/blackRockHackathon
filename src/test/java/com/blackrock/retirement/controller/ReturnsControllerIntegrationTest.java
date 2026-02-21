package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.ReturnsRequest;
import com.blackrock.retirement.dto.ReturnsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
        ReturnsRequest request = ReturnsRequest.builder()
                .principal(100000.0)
                .age(30.0)
                .inflationRate(0.03)
                .preTaxSalary(1500000.0)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/returns:nps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projection.projectionType").value("NPS"))
                .andExpect(jsonPath("$.projection.rate").value(0.0711))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ReturnsResponse response = objectMapper.readValue(responseBody, ReturnsResponse.class);

        assertNotNull(response);
        assertNotNull(response.getProjection());
        assertEquals("NPS", response.getProjection().getProjectionType());
        assertEquals(100000.0, response.getProjection().getPrincipal());
        assertEquals(0.0711, response.getProjection().getRate(), 0.0001);
        assertTrue(response.getProjection().getFutureValue() > 100000.0);
        assertTrue(response.getProjection().getTaxBenefit() >= 0);
    }

    @Test
    public void testIndexReturnsEndpoint() throws Exception {
        ReturnsRequest request = ReturnsRequest.builder()
                .principal(100000.0)
                .age(30.0)
                .inflationRate(0.03)
                .preTaxSalary(null)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/returns:index")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projection.projectionType").value("INDEX"))
                .andExpect(jsonPath("$.projection.rate").value(0.1449))
                .andExpect(jsonPath("$.projection.taxBenefit").value(0.0))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ReturnsResponse response = objectMapper.readValue(responseBody, ReturnsResponse.class);

        assertNotNull(response);
        assertNotNull(response.getProjection());
        assertEquals("INDEX", response.getProjection().getProjectionType());
        assertEquals(100000.0, response.getProjection().getPrincipal());
        assertEquals(0.1449, response.getProjection().getRate(), 0.0001);
        assertEquals(0.0, response.getProjection().getTaxBenefit());
        assertTrue(response.getProjection().getFutureValue() > 100000.0);
    }

    @Test
    public void testNPSWithHigherRate() throws Exception {
        // Index Fund has higher rate (14.49% vs 7.11%)
        ReturnsRequest npsRequest = ReturnsRequest.builder()
                .principal(100000.0)
                .age(30.0)
                .inflationRate(0.03)
                .preTaxSalary(0.0)
                .build();

        ReturnsRequest indexRequest = ReturnsRequest.builder()
                .principal(100000.0)
                .age(30.0)
                .inflationRate(0.03)
                .preTaxSalary(null)
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

        // Index should have higher future value due to higher rate
        assertTrue(indexResponse.getProjection().getFutureValue() > 
                   npsResponse.getProjection().getFutureValue());
    }

    @Test
    public void testNPSTaxBenefitZeroSalary() throws Exception {
        ReturnsRequest request = ReturnsRequest.builder()
                .principal(50000.0)
                .age(30.0)
                .inflationRate(0.03)
                .preTaxSalary(0.0)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/blackrock/challenge/v1/returns:nps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        ReturnsResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ReturnsResponse.class);

        assertEquals(0.0, response.getProjection().getTaxBenefit());
    }
}
