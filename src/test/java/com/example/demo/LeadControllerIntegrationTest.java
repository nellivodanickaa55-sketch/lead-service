package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Тесты используют отдельную базу в памяти, а не твой файл data/leads_db
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
class LeadControllerIntegrationTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    LeadRepository repo;

    // Настоящий MailService заменяется моком: письма не отправляются
    @MockitoBean
    MailService mail;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repo.deleteAll(); // каждый тест начинается с пустой базы
    }

    @Test
    void duplicatePost_createsOnlyOneLead() throws Exception {
        String json = """
                {"externalId":"row-1","name":"Ivan","contact":"@ivan","message":"Need site"}
                """;

        // Первый запрос: заявка создаётся
        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("row-1"))
                .andExpect(jsonPath("$.name").value("Ivan"));

        // Второй запрос с тем же externalId: дубль
        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // В базе ровно одна заявка
        mockMvc.perform(get("/api/leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Письмо ушло один раз
        verify(mail, timeout(1000).times(1)).notifyNewLead(any());
    }

    @Test
    void invalidSinceParam_returns400() throws Exception {
        mockMvc.perform(get("/api/leads").param("since", "abc"))
                .andExpect(status().isBadRequest());
    }
}