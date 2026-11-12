package br.pucminas.moedaestudantil;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Casos de uso implementados na Release 2 (Lab04S01). */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:moeda-test;DB_CLOSE_DELAY=-1")
class Release2IntegrationTest extends ApiTestSupport {

    /** UC15 – alterar senha (qualquer perfil). */
    @Test
    void alterarSenha() throws Exception {
        String token = login("marina.costa", "123456");

        mvc.perform(json(put("/api/auth/senha"), token, "{\"senhaAtual\": \"errada\", \"novaSenha\": \"novaSenha1\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.senhaAtual").exists());
        mvc.perform(json(put("/api/auth/senha"), token, "{\"senhaAtual\": \"123456\", \"novaSenha\": \"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.novaSenha").exists());
        mvc.perform(json(put("/api/auth/senha"), token, "{\"senhaAtual\": \"123456\", \"novaSenha\": \"novaSenha1\"}"))
                .andExpect(status().isNoContent());

        login("marina.costa", "novaSenha1");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"marina.costa\", \"senha\": \"123456\"}"))
                .andExpect(status().isUnauthorized());
    }

    /** UC17 – cancelar resgate (aluno e empresa) com estorno; UC14 – editar e excluir professor. */
    @Test
    void cancelamentoDeResgateEProfessores() throws Exception {
        String admin = login("admin", "123456");
        String carla = login("carla.mendes", "123456");
        String ana = login("ana.souza", "123456");
        String cantina = login("cantina", "123456");
        Integer anaId = ler(mvc.perform(auth(get("/api/auth/me"), ana)).andReturn(), "$.id");

        mvc.perform(json(post("/api/professores/me/envios"), carla,
                        "{\"alunoId\": " + anaId + ", \"valor\": 300, \"motivo\": \"Monitoria\"}"))
                .andExpect(status().isCreated());

        List<Integer> cafe = ler(mvc.perform(auth(get("/api/vantagens"), ana)).andReturn(),
                "$[?(@.nome == 'Café com pão de queijo')].id");
        List<Integer> almoco = ler(mvc.perform(auth(get("/api/vantagens"), ana)).andReturn(),
                "$[?(@.nome == 'Almoço com 20% de desconto')].id");

        // aluno cancela o próprio cupom e recebe as moedas de volta
        String codigo1 = ler(mvc.perform(json(post("/api/alunos/me/resgates"), ana,
                "{\"vantagemId\": " + cafe.get(0) + "}")).andExpect(status().isCreated()).andReturn(), "$.codigoCupom");
        mvc.perform(auth(post("/api/alunos/me/resgates/" + codigo1 + "/cancelar"), ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.saldoAluno").value(300))
                .andExpect(jsonPath("$.dataCancelamento").exists());
        mvc.perform(auth(post("/api/alunos/me/resgates/" + codigo1 + "/cancelar"), ana))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(auth(post("/api/empresas/me/resgates/" + codigo1 + "/conferir"), cantina))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(auth(get("/api/alunos/me/extrato"), ana))
                .andExpect(jsonPath("$.saldo").value(300))
                .andExpect(jsonPath("$.transacoes[*].tipo", hasItem("ESTORNO")));

        // empresa cancela; outra empresa não enxerga o cupom
        String codigo2 = ler(mvc.perform(json(post("/api/alunos/me/resgates"), ana,
                "{\"vantagemId\": " + almoco.get(0) + "}")).andExpect(status().isCreated()).andReturn(), "$.codigoCupom");
        String papelaria = login("papelaria", "123456");
        mvc.perform(auth(post("/api/empresas/me/resgates/" + codigo2 + "/cancelar"), papelaria))
                .andExpect(status().isNotFound());
        mvc.perform(auth(post("/api/empresas/me/resgates/" + codigo2 + "/cancelar"), cantina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAluno").value(300));
        mvc.perform(auth(get("/api/auth/emails"), ana))
                .andExpect(jsonPath("$[0].assunto", startsWith("Resgate cancelado")));

        // professor novo: editar e excluir (sem envios)
        String novo = """
                {"nome": "Paulo Reis", "email": "paulo.reis@pucminas.br", "cpf": "468.819.736-59",
                 "departamento": "Física", "instituicaoId": 1, "login": "paulo.reis"}
                """;
        Integer pauloId = ler(mvc.perform(json(post("/api/professores"), admin, novo))
                .andExpect(status().isCreated()).andReturn(), "$.id");
        mvc.perform(json(put("/api/professores/" + pauloId), admin, novo.replace("Física", "Matemática")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departamento").value("Matemática"));
        mvc.perform(auth(delete("/api/professores/" + pauloId), admin)).andExpect(status().isNoContent());

        // professor que já enviou moedas não pode ser excluído
        Integer carlaId = ler(mvc.perform(auth(get("/api/auth/me"), carla)).andReturn(), "$.id");
        mvc.perform(auth(delete("/api/professores/" + carlaId), admin)).andExpect(status().isUnprocessableEntity());
        mvc.perform(auth(delete("/api/professores/" + carlaId), carla)).andExpect(status().isForbidden());
    }

    /** UC13 – excluir instituição só quando não há vínculos. */
    @Test
    void excluirInstituicao() throws Exception {
        String admin = login("admin", "123456");
        Integer id = ler(mvc.perform(json(post("/api/instituicoes"), admin,
                        "{\"nome\": \"Faculdade Teste\", \"cnpj\": \"79.711.471/0001-68\"}"))
                .andExpect(status().isCreated()).andReturn(), "$.id");
        mvc.perform(auth(delete("/api/instituicoes/" + id), admin)).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/instituicoes/1"), admin)).andExpect(status().isUnprocessableEntity());
    }
}
