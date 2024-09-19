package it.gov.pagopa.miladapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.miladapter.services.ExternalCallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class ExternalCallsControllerTest {

    @Mock
    private ExternalCallService externalCallService;

    @InjectMocks
    private ExternalCallsController externalCallsController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(externalCallsController).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
     void testExternalCall() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", "value");

        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

        when(externalCallService.executeExternalCall(any(Map.class))).thenReturn(ResponseEntity.ok("Success"));

        mockMvc.perform(post("/externalcall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }
}

