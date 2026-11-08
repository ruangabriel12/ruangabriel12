package br.com.foodlink;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.dto.HistoricoDTO;
import br.com.foodlink.dto.IndicadorResumoDTO;
import br.com.foodlink.dto.OfertaDTO;
import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Endereco;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Retirada;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.model.enums.StatusOferta;
import br.com.foodlink.repository.EnderecoRepository;
import br.com.foodlink.repository.IndicadorRepository;
import br.com.foodlink.repository.OfertaRepository;
import br.com.foodlink.repository.ReservaRepository;
import br.com.foodlink.repository.RetiradaRepository;
import br.com.foodlink.repository.UsuarioRepository;
import br.com.foodlink.service.IndicadorService;
import br.com.foodlink.service.OfertaService;
import br.com.foodlink.service.ReservaService;
import br.com.foodlink.service.RetiradaService;
import br.com.foodlink.service.UsuarioService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração: executa o MESMO script SQL do projeto (database/script_banco.sql)
 * e valida RF01 a RF06 com persistência real. Usa H2 em modo PostgreSQL para rodar
 * em qualquer máquina sem instalar servidor; em produção o SGBD é o PostgreSQL.
 */
class FluxoIntegracaoTest {

    private static final String URL =
            "jdbc:h2:mem:foodlink;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    private static final ConnectionProvider CONEXAO = () -> DriverManager.getConnection(URL, "sa", "");

    private UsuarioService usuarioService;
    private OfertaService ofertaService;
    private ReservaService reservaService;
    private RetiradaService retiradaService;
    private IndicadorService indicadorService;

    @BeforeAll
    static void criarEsquema() throws Exception {
        String script = new String(Files.readAllBytes(Path.of("database", "script_banco.sql")), "UTF-8");
        StringBuilder sb = new StringBuilder();
        for (String linha : script.split("\n")) {
            if (!linha.trim().startsWith("--")) {
                sb.append(linha).append('\n');
            }
        }
        try (Connection con = CONEXAO.getConexao(); Statement st = con.createStatement()) {
            for (String comando : sb.toString().split(";")) {
                if (!comando.trim().isEmpty()) {
                    st.execute(comando);
                }
            }
        }
    }

    @BeforeEach
    void prepararServicos() throws Exception {
        // limpa os dados entre os testes (mantém o esquema)
        try (Connection con = CONEXAO.getConexao(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM indicador_impacto");
            st.execute("DELETE FROM retirada");
            st.execute("DELETE FROM reserva");
            st.execute("DELETE FROM oferta_doacao");
            st.execute("DELETE FROM usuario");
            st.execute("DELETE FROM endereco");
        }
        UsuarioRepository usuarioRepository = new UsuarioRepository(CONEXAO);
        EnderecoRepository enderecoRepository = new EnderecoRepository(CONEXAO);
        OfertaRepository ofertaRepository = new OfertaRepository(CONEXAO);
        ReservaRepository reservaRepository = new ReservaRepository(CONEXAO, ofertaRepository, usuarioRepository);
        RetiradaRepository retiradaRepository = new RetiradaRepository(CONEXAO, reservaRepository, usuarioRepository);
        IndicadorRepository indicadorRepository = new IndicadorRepository(CONEXAO);

        usuarioService = new UsuarioService(usuarioRepository);
        ofertaService = new OfertaService(ofertaRepository, enderecoRepository);
        reservaService = new ReservaService(reservaRepository, ofertaRepository);
        indicadorService = new IndicadorService(indicadorRepository);
        retiradaService = new RetiradaService(retiradaRepository, reservaRepository, ofertaRepository, indicadorService);
    }

    private static Date emDias(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, dias);
        return c.getTime();
    }

    private Endereco endereco() {
        return new Endereco("Rua das Flores, 100", "Cianorte", "87200-000", -23.6636f, -52.6053f);
    }

    @Test
    @DisplayName("RF01 - cadastro e autenticacao dos tres tipos de usuario")
    void rf01CadastroEAutenticacao() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "padaria@x.com", "111", "1234");
        Receptor receptor = usuarioService.cadastrarReceptor("ONG", "ong@x.com", "222", "1234");
        Voluntario voluntario = usuarioService.cadastrarVoluntario("Ana", "ana@x.com", "333", "1234");

        assertEquals(3, usuarioService.listar().size());

        assertTrue(usuarioService.autenticar("padaria@x.com", "1234").isPresent());
        assertFalse(usuarioService.autenticar("padaria@x.com", "errada").isPresent());
        assertTrue(usuarioService.autenticar("ong@x.com", "1234").get() instanceof Receptor);
        assertTrue(usuarioService.autenticar("ana@x.com", "1234").get() instanceof Voluntario);
        assertNotNull(doador.getId());

        // UPDATE e DELETE de usuario (CRUD)
        usuarioService.atualizarPerfil(voluntario.getId(), "Ana Souza", null, null);
        assertEquals("Ana Souza", usuarioService.buscarPorId(voluntario.getId()).get().getNome());
        usuarioService.excluir(receptor.getId());
        assertFalse(usuarioService.buscarPorId(receptor.getId()).isPresent());
    }

    @Test
    @DisplayName("RF02 - Doador cadastra oferta com todos os campos")
    void rf02CadastroDeOferta() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "p@x.com", "111", "1234");
        OfertaDoacao oferta = ofertaService.cadastrar(doador, endereco(), "Pães", 20f, 8.5f, emDias(2), "Retirar até 18h");

        OfertaDoacao persistida = ofertaService.buscarPorId(oferta.getId());
        assertEquals("Pães", persistida.getAlimento());
        assertEquals(8.5f, persistida.getPeso(), 0.001f);
        assertEquals(StatusOferta.DISPONIVEL, persistida.getStatus());
        assertNotNull(persistida.getEndereco());
        assertEquals("Padaria", persistida.getDoador().getNome());
    }

    @Test
    @DisplayName("RN01 - oferta com menos de 2h de validade e recusada na publicacao")
    void rn01NaPublicacao() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "p@x.com", "111", "1234");
        Date umaHora = new Date(System.currentTimeMillis() + 60L * 60 * 1000);
        assertThrows(IllegalStateException.class,
                () -> ofertaService.cadastrar(doador, endereco(), "Iogurte", 5f, 2f, umaHora, null));
    }

    @Test
    @DisplayName("RF03 - consulta de ofertas por localizacao e raio")
    void rf03BuscaPorRaio() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "p@x.com", "111", "1234");
        ofertaService.cadastrar(doador, endereco(), "Pães", 20f, 8.5f, emDias(2), null);
        // oferta a ~100 km de distância
        Endereco longe = new Endereco("Rodovia, km 100", "Maringá", "87000-000", -23.4205f, -51.9331f);
        ofertaService.cadastrar(doador, longe, "Frutas", 10f, 12f, emDias(3), null);

        List<OfertaDTO> perto = ofertaService.buscarPorRaio(-23.6636, -52.6053, 10);
        assertEquals(1, perto.size());
        assertEquals("Pães", perto.get(0).getAlimento());
        assertTrue(perto.get(0).getDistanciaKm() >= 0);

        assertEquals(2, ofertaService.buscarPorRaio(-23.6636, -52.6053, 200).size());
    }

    @Test
    @DisplayName("RF04 + RN03 + RN02 - reserva, exclusividade e limite de 3 ativas")
    void rf04Reservas() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "p@x.com", "111", "1234");
        Receptor receptor = usuarioService.cadastrarReceptor("ONG", "ong@x.com", "222", "1234");
        Receptor outro = usuarioService.cadastrarReceptor("Lar", "lar@x.com", "444", "1234");

        OfertaDoacao oferta = ofertaService.cadastrar(doador, endereco(), "Pães", 20f, 8.5f, emDias(2), null);
        Reserva reserva = reservaService.reservar(receptor, oferta.getId());

        assertEquals(1, reservaService.reservasAtivas(receptor.getId()));
        assertEquals(StatusOferta.RESERVADA, ofertaService.buscarPorId(oferta.getId()).getStatus());

        // RN03 - outro receptor nao pode reservar a mesma oferta
        assertThrows(IllegalStateException.class, () -> reservaService.reservar(outro, oferta.getId()));

        // RN02 - limite de 3 reservas ativas
        for (int i = 0; i < 2; i++) {
            OfertaDoacao o = ofertaService.cadastrar(doador, endereco(), "Oferta " + i, 1f, 1f, emDias(2), null);
            reservaService.reservar(receptor, o.getId());
        }
        OfertaDoacao excedente = ofertaService.cadastrar(doador, endereco(), "Excedente", 1f, 1f, emDias(2), null);
        assertThrows(IllegalStateException.class, () -> reservaService.reservar(receptor, excedente.getId()));

        // RN02 - cancelar devolve a cota
        reservaService.cancelar(reserva.getId());
        assertEquals(2, reservaService.reservasAtivas(receptor.getId()));
        assertEquals(StatusOferta.DISPONIVEL, ofertaService.buscarPorId(oferta.getId()).getStatus());
        assertDoesNotThrow(() -> reservaService.reservar(receptor, excedente.getId()));
    }

    @Test
    @DisplayName("RF05 + RF06 - fluxo completo de retirada, historico e indicadores")
    void rf05eRf06FluxoCompleto() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "p@x.com", "111", "1234");
        Receptor receptor = usuarioService.cadastrarReceptor("ONG", "ong@x.com", "222", "1234");
        Voluntario voluntario = usuarioService.cadastrarVoluntario("Ana", "ana@x.com", "333", "1234");

        OfertaDoacao oferta = ofertaService.cadastrar(doador, endereco(), "Pães", 20f, 8.5f, emDias(2), null);
        Reserva reserva = reservaService.reservar(receptor, oferta.getId());

        // RF05 - o voluntario consulta as solicitacoes e assume a retirada
        assertEquals(1, retiradaService.listarSolicitacoes().size());
        Retirada retirada = retiradaService.assumir(voluntario, reserva.getId());
        assertEquals(0, retiradaService.listarSolicitacoes().size(), "reserva ja assumida nao aparece mais");

        retiradaService.registrarColeta(retirada.getId());
        retiradaService.registrarEntrega(retirada.getId());

        // estado final persistido
        assertEquals(StatusOferta.CONCLUIDA, ofertaService.buscarPorId(oferta.getId()).getStatus());
        assertEquals(br.com.foodlink.model.enums.StatusReserva.CONCLUIDA,
                reservaService.buscarPorId(reserva.getId()).getStatus());

        // RF06 - historico e indicadores
        List<HistoricoDTO> historico = ofertaService.listarHistorico();
        assertEquals(1, historico.size());
        assertEquals("Pães", historico.get(0).getAlimento());

        IndicadorResumoDTO resumo = indicadorService.resumo();
        assertEquals(1, resumo.getDoacoesConcluidas());
        assertEquals(8.5, resumo.getPesoTotalKg(), 0.01);
        assertEquals(21.25, resumo.getCo2EvitadoKg(), 0.01);

        // multiplicidade 1:1 — nao gera indicador duplicado
        indicadorService.gerarPara(ofertaService.buscarPorId(oferta.getId()));
        assertEquals(1, indicadorService.resumo().getDoacoesConcluidas());
    }

    @Test
    @DisplayName("CRUD completo da entidade principal (OfertaDoacao)")
    void crudOferta() {
        Doador doador = usuarioService.cadastrarDoador("Padaria", "p@x.com", "111", "1234");
        OfertaDoacao oferta = ofertaService.cadastrar(doador, endereco(), "Pães", 20f, 8.5f, emDias(2), null);

        // READ
        assertTrue(ofertaService.listarDisponiveis().size() == 1);
        assertTrue(ofertaService.listarPorDoador(doador.getId()).size() == 1);

        // UPDATE
        ofertaService.atualizar(oferta.getId(), "Pães integrais", 25f, 9f, emDias(3), "Atualizado");
        OfertaDoacao atualizada = ofertaService.buscarPorId(oferta.getId());
        assertEquals("Pães integrais", atualizada.getAlimento());
        assertEquals(25f, atualizada.getQuantidade(), 0.001f);
        assertEquals("Atualizado", atualizada.getObservacoes());

        // DELETE
        ofertaService.excluir(oferta.getId());
        assertTrue(ofertaService.listarDisponiveis().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> ofertaService.buscarPorId(oferta.getId()));
    }
}
