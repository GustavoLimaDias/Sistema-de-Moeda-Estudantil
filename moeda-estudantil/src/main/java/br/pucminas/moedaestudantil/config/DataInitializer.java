package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.model.Administrador;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Endereco;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga inicial executada só no primeiro start (banco vazio): instituições e
 * professores pré-cadastrados, um administrador e dados de demonstração.
 * Senha de todos os usuários de exemplo: 123456
 * Logins: admin, carla.mendes, ricardo.alves (professores), ana.souza, lucas.pereira, marina.costa (alunos), cantina, papelaria (empresas)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final String SENHA_PADRAO = "123456";

    private final InstituicaoEnsinoRepository instituicaoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final EmpresaParceiraRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(InstituicaoEnsinoRepository instituicaoRepository, ProfessorRepository professorRepository,
                           AlunoRepository alunoRepository, EmpresaParceiraRepository empresaRepository,
                           UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
        this.instituicaoRepository = instituicaoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (instituicaoRepository.count() > 0) {
            return;
        }

        InstituicaoEnsino puc = instituicaoRepository.save(new InstituicaoEnsino("PUC Minas", "93786579000140"));
        InstituicaoEnsino ufmg = instituicaoRepository.save(new InstituicaoEnsino("UFMG", "75432319000195"));
        instituicaoRepository.save(new InstituicaoEnsino("CEFET-MG", "48757491000146"));

        Administrador admin = new Administrador();
        acesso(admin, "Administrador", "admin@moedaestudantil.com", "admin");
        usuarioRepository.save(admin);

        // as 1.000 moedas do semestre são creditadas pelo CreditoSemestralService logo após o start
        professor("Carla Mendes", "carla.mendes@pucminas.br", "carla.mendes",
                "08301661305", "Engenharia de Software", puc);
        professor("Ricardo Alves", "ricardo.alves@pucminas.br", "ricardo.alves",
                "18609139034", "Ciência da Computação", puc);

        Aluno aluno = new Aluno();
        acesso(aluno, "Ana Souza", "ana.souza@email.com", "ana.souza");
        aluno.setCpf("52601815906");
        aluno.setRg("MG1234567");
        aluno.setCurso("Engenharia de Software");
        aluno.setInstituicao(puc);
        aluno.setEndereco(new Endereco("Rua Dom José Gaspar", "500", "Prédio 34", "Coração Eucarístico",
                "Belo Horizonte", "MG", "30535901"));
        alunoRepository.save(aluno);

        alunoRepository.save(alunoExemplo("Lucas Pereira", "lucas.pereira@email.com", "lucas.pereira", "62819482112",
                "MG2345678", "Ciência da Computação", puc,
                new Endereco("Av. Amazonas", "1200", null, "Centro", "Belo Horizonte", "MG", "30180001")));
        alunoRepository.save(alunoExemplo("Marina Costa", "marina.costa@email.com", "marina.costa", "99351819019",
                "MG3456789", "Engenharia Civil", ufmg,
                new Endereco("Av. Antônio Carlos", "6627", "Apto 101", "Pampulha", "Belo Horizonte", "MG", "31270901")));

        EmpresaParceira cantina = new EmpresaParceira();
        acesso(cantina, "Cantina do Campus", "contato@cantinadocampus.com", "cantina");
        cantina.setCnpj("18625276000120");
        cantina.setDescricao("Lanches e refeições dentro do campus.");
        cantina.cadastrarVantagem(vantagem("Almoço com 20% de desconto",
                "Desconto de 20% em uma refeição completa no horário de almoço.", "/img/vantagem-almoco.svg", 150));
        cantina.cadastrarVantagem(vantagem("Café com pão de queijo",
                "Um café expresso e uma porção de pão de queijo.", "/img/vantagem-cafe.svg", 50));
        empresaRepository.save(cantina);

        EmpresaParceira papelaria = new EmpresaParceira();
        acesso(papelaria, "Papelaria Universitária", "vendas@papelariauni.com", "papelaria");
        papelaria.setCnpj("01895559000190");
        papelaria.setDescricao("Materiais escolares e impressão.");
        papelaria.cadastrarVantagem(vantagem("Kit de materiais",
                "Caderno, canetas e marca-texto.", "/img/vantagem-material.svg", 300));
        empresaRepository.save(papelaria);
    }

    private void acesso(Usuario usuario, String nome, String email, String login) {
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setLogin(login);
        usuario.alterarSenha(SENHA_PADRAO, encoder);
    }

    private Professor professor(String nome, String email, String login, String cpf, String departamento,
                                InstituicaoEnsino instituicao) {
        Professor p = new Professor();
        acesso(p, nome, email, login);
        p.setCpf(cpf);
        p.setDepartamento(departamento);
        p.setInstituicao(instituicao);
        return professorRepository.save(p);
    }

    private Aluno alunoExemplo(String nome, String email, String login, String cpf, String rg, String curso,
                               InstituicaoEnsino instituicao, Endereco endereco) {
        Aluno a = new Aluno();
        acesso(a, nome, email, login);
        a.setCpf(cpf);
        a.setRg(rg);
        a.setCurso(curso);
        a.setInstituicao(instituicao);
        a.setEndereco(endereco);
        return a;
    }

    private Vantagem vantagem(String nome, String descricao, String fotoUrl, int custo) {
        Vantagem v = new Vantagem();
        v.setNome(nome);
        v.setDescricao(descricao);
        v.setFotoUrl(fotoUrl);
        v.setCusto(custo);
        return v;
    }
}
