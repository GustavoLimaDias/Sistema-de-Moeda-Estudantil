package br.pucminas.moedaestudantil;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CRUDs de aluno e empresa parceira com autenticação e regras de acesso. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:moeda-test;DB_CLOSE_DELAY=-1")
class CrudIntegrationTest extends ApiTestSupport {

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
        // cadastro é público
        MvcResult criado = mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(ALUNO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("99603082430"))
                .andExpect(jsonPath("$.endereco.uf").value("MG"))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();
        Integer id = ler(criado, "$.id");

        String tokenAluno = login("bruno.lima", "segredo1");
        mvc.perform(auth(get("/api/alunos/" + id), tokenAluno)).andExpect(status().isOk());

        String edicao = ALUNO.replace("Ciência da Computação", "Sistemas de Informação").replace("\"segredo1\"", "\"\"");
        mvc.perform(json(put("/api/alunos/" + id), tokenAluno, edicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curso").value("Sistemas de Informação"));

        // CPF duplicado
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(ALUNO.replace("bruno.lima@", "outro@").replace("\"bruno.lima\"", "\"outro\"")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.cpf").exists());

        // aluno não lista alunos nem acessa dados de outro aluno
        mvc.perform(auth(get("/api/alunos"), tokenAluno)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/alunos/" + (id - 1)), tokenAluno)).andExpect(status().isForbidden());

        // administrador lista todos
        String tokenAdmin = login("admin", "123456");
        mvc.perform(auth(get("/api/alunos"), tokenAdmin)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));

        // exclusão da própria conta encerra a sessão
        mvc.perform(auth(delete("/api/alunos/" + id), tokenAluno)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/alunos/" + id), tokenAdmin)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/auth/me"), tokenAluno)).andExpect(status().isUnauthorized());
    }

    @Test
    void rejeitaAlunoComCamposInvalidos() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    void exigeLogin() throws Exception {
        mvc.perform(get("/api/vantagens")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/instituicoes")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"admin\", \"senha\": \"errada\"}"))
                .andExpect(status().isUnauthorized());
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
        Integer id = ler(criada, "$.id");
        String token = login("livraria", "segredo1");

        String vantagem = """
                {"nome": "10% em livros", "descricao": "Desconto em qualquer livro técnico",
                 "fotoUrl": "/img/vantagem-material.svg", "custo": 120}
                """;
        mvc.perform(json(post("/api/empresas/" + id + "/vantagens"), token, vantagem))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true));

        mvc.perform(auth(get("/api/empresas/" + id), token)).andExpect(jsonPath("$.quantidadeVantagens").value(1));
        mvc.perform(auth(get("/api/empresas/" + id + "/vantagens"), token)).andExpect(jsonPath("$", hasSize(1)));

        // outra empresa não mexe nas vantagens desta
        String outra = login("papelaria", "123456");
        mvc.perform(json(post("/api/empresas/" + id + "/vantagens"), outra, vantagem)).andExpect(status().isForbidden());

        mvc.perform(auth(delete("/api/empresas/" + id), token)).andExpect(status().isNoContent());
        String admin = login("admin", "123456");
        mvc.perform(auth(get("/api/empresas/" + id), admin)).andExpect(status().isNotFound());
    }
}
