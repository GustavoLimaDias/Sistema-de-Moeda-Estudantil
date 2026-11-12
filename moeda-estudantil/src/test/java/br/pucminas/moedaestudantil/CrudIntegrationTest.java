package br.pucminas.moedaestudantil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa os CRUDs pela API, com banco H2 em memória (dados de exemplo carregados no start). */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:moeda-test;DB_CLOSE_DELAY=-1")
class CrudIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private static final String ALUNO = """
            {
              "nome": "Bruno Lima",
              "email": "bruno.lima@email.com",
              "cpf": "996.030.824-30",
              "rg": "MG7654321",
              "curso": "Ciência da Computação",
              "instituicaoId": 1,
              "endereco": {
                "logradouro": "Av. Afonso Pena", "numero": "1000", "complemento": "",
                "bairro": "Centro", "cidade": "Belo Horizonte", "uf": "mg", "cep": "30130-000"
              },
              "login": "bruno.lima",
              "senha": "segredo1"
            }
            """;

    @Test
    void crudCompletoDeAluno() throws Exception {
        MvcResult criado = mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(ALUNO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("99603082430"))
                .andExpect(jsonPath("$.endereco.uf").value("MG"))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();
        String id = criado.getResponse().getHeader("Location").replace("/api/alunos/", "");

        mvc.perform(get("/api/alunos")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        String edicao = ALUNO.replace("Ciência da Computação", "Sistemas de Informação").replace("\"segredo1\"", "\"\"");
        mvc.perform(put("/api/alunos/" + id).contentType(MediaType.APPLICATION_JSON).content(edicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curso").value("Sistemas de Informação"));

        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(ALUNO.replace("bruno.lima@", "outro@").replace("\"bruno.lima\"", "\"outro\"")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.cpf").exists());

        mvc.perform(delete("/api/alunos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/alunos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaAlunoComCamposInvalidos() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    void crudDeEmpresaComVantagem() throws Exception {
        String empresa = """
                {"nome": "Livraria Central", "email": "contato@livrariacentral.com",
                 "cnpj": "79.711.471/0001-68", "descricao": "Livros técnicos",
                 "login": "livraria", "senha": "segredo1"}
                """;
        MvcResult criada = mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(empresa))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("79711471000168"))
                .andReturn();
        String id = criada.getResponse().getHeader("Location").replace("/api/empresas/", "");

        String vantagem = """
                {"nome": "10% em livros", "descricao": "Desconto em qualquer livro técnico",
                 "fotoUrl": "/img/vantagem-material.svg", "custo": 120}
                """;
        mvc.perform(post("/api/empresas/" + id + "/vantagens").contentType(MediaType.APPLICATION_JSON).content(vantagem))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true));

        mvc.perform(get("/api/empresas/" + id)).andExpect(jsonPath("$.quantidadeVantagens").value(1));
        mvc.perform(get("/api/empresas/" + id + "/vantagens")).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/empresas/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/empresas/" + id)).andExpect(status().isNotFound());
    }
}
