package br.com.foodlink;

import br.com.foodlink.config.ConexaoDB;
import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.controller.OfertaController;
import br.com.foodlink.controller.RelatorioController;
import br.com.foodlink.controller.ReservaController;
import br.com.foodlink.controller.RetiradaController;
import br.com.foodlink.controller.UsuarioController;
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
import br.com.foodlink.view.MenuConsole;

import java.sql.Connection;

/**
 * Ponto de entrada do FoodLink.
 * Arquitetura em camadas: Controller -> Service -> Repository -> PostgreSQL.
 */
public class FoodLinkApp {

    public static void main(String[] args) {
        ConnectionProvider conexao = ConexaoDB.instancia();

        // Verificação de conectividade com o banco (persistência é obrigatória).
        try (Connection con = conexao.getConexao()) {
            System.out.println("Conectado ao banco: " + con.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("Não foi possível conectar ao banco de dados.");
            System.err.println("URL: " + ConexaoDB.getUrl());
            System.err.println("Detalhe: " + e.getMessage());
            System.err.println("\nExecute o script database/script_banco.sql e ajuste db.properties "
                    + "(ou as variáveis DB_URL, DB_USER e DB_PASSWORD).");
            System.exit(1);
        }

        // Repositórios
        UsuarioRepository usuarioRepository = new UsuarioRepository(conexao);
        EnderecoRepository enderecoRepository = new EnderecoRepository(conexao);
        OfertaRepository ofertaRepository = new OfertaRepository(conexao);
        ReservaRepository reservaRepository = new ReservaRepository(conexao, ofertaRepository, usuarioRepository);
        RetiradaRepository retiradaRepository = new RetiradaRepository(conexao, reservaRepository, usuarioRepository);
        IndicadorRepository indicadorRepository = new IndicadorRepository(conexao);

        // Serviços
        UsuarioService usuarioService = new UsuarioService(usuarioRepository);
        OfertaService ofertaService = new OfertaService(ofertaRepository, enderecoRepository);
        ReservaService reservaService = new ReservaService(reservaRepository, ofertaRepository);
        IndicadorService indicadorService = new IndicadorService(indicadorRepository);
        RetiradaService retiradaService = new RetiradaService(retiradaRepository, reservaRepository, ofertaRepository, indicadorService);

        // Controllers
        new MenuConsole(
                new UsuarioController(usuarioService),
                new OfertaController(ofertaService),
                new ReservaController(reservaService),
                new RetiradaController(retiradaService),
                new RelatorioController(ofertaService, indicadorService)
        ).iniciar();
    }
}
