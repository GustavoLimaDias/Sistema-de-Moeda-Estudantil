package br.pucminas.moedaestudantil;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lab04S02 – health check público e CORS configurável para o front-end no Vercel. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:moeda-deploy-test;DB_CLOSE_DELAY=-1",
        "app.cors.allowed-origins=https://*.vercel.app, https://exemplo.com"})
class DeployIntegrationTest extends ApiTestSupport {

    @Test
    void healthCheckSemLogin() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void corsAceitaOrigensConfiguradasELocalhost() throws Exception {
        for (String origem : new String[]{"https://moeda-estudantil.vercel.app", "https://exemplo.com", "http://localhost:5500"}) {
            mvc.perform(options("/api/auth/login").header("Origin", origem)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origem));
        }
        mvc.perform(options("/api/auth/login").header("Origin", "https://malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
