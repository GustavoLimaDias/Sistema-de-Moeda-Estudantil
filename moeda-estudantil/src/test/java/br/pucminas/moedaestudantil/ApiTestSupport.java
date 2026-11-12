package br.pucminas.moedaestudantil;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Utilidades comuns: login e montagem de requisições autenticadas. */
abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mvc;

    protected String login(String login, String senha) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"" + login + "\", \"senha\": \"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
    }

    protected static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token);
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String token, String body) {
        return auth(req, token).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected static <T> T ler(MvcResult r, String caminho) throws Exception {
        return JsonPath.read(r.getResponse().getContentAsString(), caminho);
    }
}
