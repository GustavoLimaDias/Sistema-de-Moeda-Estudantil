package br.pucminas.moedaestudantil;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo principal: crédito semestral → envio de moedas → resgate com cupom → conferência pela empresa. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:moeda-test;DB_CLOSE_DELAY=-1")
class MoedasIntegrationTest extends ApiTestSupport {

    @Test
    void fluxoCompletoDeMoedas() throws Exception {
        String professor = login("ricardo.alves", "123456");
        String aluno = login("lucas.pereira", "123456");
        Integer alunoId = ler(mvc.perform(auth(get("/api/auth/me"), aluno)).andReturn(), "$.id");

        // professor já recebeu o crédito do semestre na inicialização
        MvcResult extratoProf = mvc.perform(auth(get("/api/professores/me/extrato"), professor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transacoes[*].tipo", hasItem("CREDITO")))
                .andReturn();
        int saldoProfessor = ler(extratoProf, "$.saldo");
        assertTrue(saldoProfessor >= 300);

        // professor vê os alunos da instituição dele
        mvc.perform(auth(get("/api/alunos"), professor)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Lucas Pereira")));

        // envio de moedas (motivo obrigatório)
        mvc.perform(json(post("/api/professores/me/envios"), professor,
                        "{\"alunoId\": " + alunoId + ", \"valor\": 200, \"motivo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").exists());
        mvc.perform(json(post("/api/professores/me/envios"), professor,
                        "{\"alunoId\": " + alunoId + ", \"valor\": 200, \"motivo\": \"Ótima apresentação\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoProfessor").value(saldoProfessor - 200));

        // saldo insuficiente
        mvc.perform(json(post("/api/professores/me/envios"), professor,
                        "{\"alunoId\": " + alunoId + ", \"valor\": 999999, \"motivo\": \"teste\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.campos.valor").exists());

        // aluno de outra instituição (Marina, UFMG)
        String admin = login("admin", "123456");
        List<Integer> marina = ler(mvc.perform(auth(get("/api/alunos"), admin)).andReturn(),
                "$[?(@.nome == 'Marina Costa')].id");
        mvc.perform(json(post("/api/professores/me/envios"), professor,
                        "{\"alunoId\": " + marina.get(0) + ", \"valor\": 10, \"motivo\": \"teste\"}"))
                .andExpect(status().isUnprocessableEntity());

        // aluno vê o recebimento e o email
        mvc.perform(auth(get("/api/alunos/me/extrato"), aluno))
                .andExpect(jsonPath("$.saldo").value(200))
                .andExpect(jsonPath("$.transacoes[0].tipo").value("RECEBIMENTO"))
                .andExpect(jsonPath("$.transacoes[0].contraparte").value("Ricardo Alves"));
        mvc.perform(auth(get("/api/auth/emails"), aluno))
                .andExpect(jsonPath("$[0].assunto").value("Você recebeu 200 moedas!"));

        // resgate da vantagem de 150 moedas
        List<Integer> almoco = ler(mvc.perform(auth(get("/api/vantagens"), aluno)).andReturn(),
                "$[?(@.nome == 'Almoço com 20% de desconto')].id");
        MvcResult resgate = mvc.perform(json(post("/api/alunos/me/resgates"), aluno,
                        "{\"vantagemId\": " + almoco.get(0) + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoAluno").value(50))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();
        String codigo = ler(resgate, "$.codigoCupom");
        assertEquals(12, codigo.length());

        // sem saldo para resgatar de novo
        mvc.perform(json(post("/api/alunos/me/resgates"), aluno, "{\"vantagemId\": " + almoco.get(0) + "}"))
                .andExpect(status().isUnprocessableEntity());

        // emails de cupom para aluno e empresa, com o mesmo código
        mvc.perform(auth(get("/api/auth/emails"), aluno))
                .andExpect(jsonPath("$[0].assunto", startsWith("Seu cupom")));
        String empresa = login("cantina", "123456");
        MvcResult emailsEmpresa = mvc.perform(auth(get("/api/auth/emails"), empresa)).andReturn();
        String corpo = ler(emailsEmpresa, "$[0].corpo");
        assertTrue(corpo.contains(codigo));

        // empresa confere o cupom uma única vez
        mvc.perform(auth(get("/api/empresas/me/resgates"), empresa))
                .andExpect(jsonPath("$[0].codigoCupom").value(codigo));
        mvc.perform(auth(post("/api/empresas/me/resgates/" + codigo.toLowerCase() + "/conferir"), empresa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFERIDO"));
        mvc.perform(auth(post("/api/empresas/me/resgates/" + codigo + "/conferir"), empresa))
                .andExpect(status().isUnprocessableEntity());

        // outra empresa não encontra o cupom
        String papelaria = login("papelaria", "123456");
        mvc.perform(auth(post("/api/empresas/me/resgates/" + codigo + "/conferir"), papelaria))
                .andExpect(status().isNotFound());

        // aluno com movimentação não pode ser excluído
        mvc.perform(auth(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/alunos/" + alunoId), admin))
                .andExpect(status().isUnprocessableEntity());

        // professor não acessa área do aluno
        mvc.perform(auth(get("/api/alunos/me/extrato"), professor)).andExpect(status().isForbidden());
    }

    @Test
    void creditoSemestralEIdempotente() throws Exception {
        String admin = login("admin", "123456");
        mvc.perform(auth(post("/api/admin/creditos-semestrais"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professoresCreditados").value(0));
    }

    @Test
    void importacaoDeProfessores() throws Exception {
        String admin = login("admin", "123456");
        String lista = """
                [{"nome": "Paula Rocha", "email": "paula.rocha@pucminas.br", "cpf": "18609139034",
                  "departamento": "Matemática", "instituicaoId": 1, "login": "paula.rocha"}]
                """;
        // CPF já usado pelo professor Ricardo
        mvc.perform(json(post("/api/professores/importacao"), admin, lista)).andExpect(status().isUnprocessableEntity());

        String valida = lista.replace("18609139034", "663.923.321-54");
        mvc.perform(json(post("/api/professores/importacao"), admin, valida))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].saldo").value(1000));
        // senha inicial = CPF
        login("paula.rocha", "66392332154");
    }
}
