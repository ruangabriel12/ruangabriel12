package br.com.foodlink.view;

import br.com.foodlink.controller.OfertaController;
import br.com.foodlink.controller.RelatorioController;
import br.com.foodlink.controller.ReservaController;
import br.com.foodlink.controller.RetiradaController;
import br.com.foodlink.controller.UsuarioController;
import br.com.foodlink.dto.HistoricoDTO;
import br.com.foodlink.dto.OfertaDTO;
import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Endereco;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Retirada;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

/** Interface de console do FoodLink (camada de visão). */
public class MenuConsole {

    private final Scanner in = new Scanner(System.in);
    private final UsuarioController usuarioController;
    private final OfertaController ofertaController;
    private final ReservaController reservaController;
    private final RetiradaController retiradaController;
    private final RelatorioController relatorioController;

    private Usuario logado;

    public MenuConsole(UsuarioController usuarioController, OfertaController ofertaController,
                       ReservaController reservaController, RetiradaController retiradaController,
                       RelatorioController relatorioController) {
        this.usuarioController = usuarioController;
        this.ofertaController = ofertaController;
        this.reservaController = reservaController;
        this.retiradaController = retiradaController;
        this.relatorioController = relatorioController;
    }

    public void iniciar() {
        System.out.println("\n=========================================");
        System.out.println("            FOODLINK - AEP 2026");
        System.out.println(" Redução de desperdício e logística solidária");
        System.out.println("=========================================");
        boolean executando = true;
        while (executando) {
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println("1) Cadastrar usuário");
            System.out.println("2) Entrar (login)");
            System.out.println("3) Consultar ofertas disponíveis por raio");
            System.out.println("4) Relatórios de impacto");
            System.out.println("5) Listar usuários cadastrados");
            System.out.println("0) Sair");
            String opcao = ler("Opção: ");
            if (opcao == null) {
                return;
            }
            try {
                switch (opcao) {
                    case "1": cadastrarUsuario(); break;
                    case "2": login(); break;
                    case "3": consultarPorRaio(); break;
                    case "4": relatorios(); break;
                    case "5": listarUsuarios(); break;
                    case "0": executando = false; break;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RuntimeException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
        System.out.println("Até logo!");
    }

    /* ---------------------------- RF01 ---------------------------- */
    private void cadastrarUsuario() {
        System.out.println("\nTipo: 1) Doador  2) Receptor  3) Voluntário");
        String tipo = ler("Tipo: ");
        String nome = ler("Nome: ");
        String email = ler("E-mail: ");
        String documento = ler("Documento: ");
        String senha = ler("Senha: ");
        Usuario novo;
        switch (tipo) {
            case "1": novo = usuarioController.cadastrarDoador(nome, email, documento, senha); break;
            case "2": novo = usuarioController.cadastrarReceptor(nome, email, documento, senha); break;
            case "3": novo = usuarioController.cadastrarVoluntario(nome, email, documento, senha); break;
            default:
                System.out.println("Tipo inválido.");
                return;
        }
        System.out.println("Usuário cadastrado: " + novo.getNome() + " (" + novo.getClass().getSimpleName()
                + ") id=" + novo.getId());
    }

    private void login() {
        String email = ler("E-mail: ");
        String senha = ler("Senha: ");
        Optional<Usuario> u = usuarioController.login(email, senha);
        if (!u.isPresent()) {
            System.out.println("Credenciais inválidas.");
            return;
        }
        logado = u.get();
        System.out.println("Bem-vindo(a), " + logado.getNome() + "!");
        if (logado instanceof Doador) {
            menuDoador((Doador) logado);
        } else if (logado instanceof Receptor) {
            menuReceptor((Receptor) logado);
        } else if (logado instanceof Voluntario) {
            menuVoluntario((Voluntario) logado);
        }
        logado = null;
    }

    private void listarUsuarios() {
        System.out.println("\n--- USUÁRIOS ---");
        for (Usuario u : usuarioController.listar()) {
            System.out.printf("%s | %-22s | %-28s | %s%n",
                    u.getId().toString().substring(0, 8), u.getNome(), u.getEmail(),
                    u.getClass().getSimpleName());
        }
    }

    /* ------------------------- RF02 / CRUD ------------------------- */
    private void menuDoador(Doador doador) {
        while (true) {
            System.out.println("\n--- MENU DOADOR (" + doador.getNome() + ") ---");
            System.out.println("1) Publicar oferta de doação");
            System.out.println("2) Minhas ofertas");
            System.out.println("3) Editar oferta");
            System.out.println("4) Excluir oferta");
            System.out.println("5) Atualizar meu perfil");
            System.out.println("0) Sair");
            String opcao = ler("Opção: ");
            if (opcao == null) {
                return;
            }
            try {
                switch (opcao) {
                    case "1": publicarOferta(doador); break;
                    case "2": listarMinhasOfertas(doador); break;
                    case "3": editarOferta(); break;
                    case "4": excluirOferta(); break;
                    case "5": atualizarPerfil(); break;
                    case "0": return;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RuntimeException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    private void publicarOferta(Doador doador) {
        System.out.println("\n--- NOVA OFERTA ---");
        String alimento = ler("Alimento: ");
        float quantidade = lerFloat("Quantidade (un): ");
        float peso = lerFloat("Peso (kg): ");
        Date validade = lerData("Validade (dd/MM/yyyy HH:mm): ");
        String observacoes = ler("Observações: ");
        System.out.println("\n--- ENDEREÇO DE RETIRADA ---");
        String rua = ler("Rua e número: ");
        String cidade = ler("Cidade: ");
        String cep = ler("CEP: ");
        float latitude = lerFloat("Latitude: ");
        float longitude = lerFloat("Longitude: ");
        Endereco endereco = new Endereco(rua, cidade, cep, latitude, longitude);
        OfertaDoacao oferta = ofertaController.publicar(doador, endereco, alimento, quantidade, peso, validade, observacoes);
        System.out.println("Oferta publicada com id " + oferta.getId());
    }

    private void listarMinhasOfertas(Doador doador) {
        System.out.println("\n--- MINHAS OFERTAS ---");
        List<OfertaDTO> ofertas = ofertaController.listarPorDoador(doador);
        if (ofertas.isEmpty()) {
            System.out.println("Nenhuma oferta publicada.");
        }
        for (OfertaDTO o : ofertas) {
            System.out.println(o);
        }
    }

    private void editarOferta() {
        String id = ler("Id da oferta: ");
        String alimento = ler("Novo alimento (Enter mantém): ");
        float quantidade = lerFloat("Nova quantidade (0 mantém): ");
        float peso = lerFloat("Novo peso (0 mantém): ");
        String validadeTxt = ler("Nova validade dd/MM/yyyy HH:mm (Enter mantém): ");
        Date validade = (validadeTxt == null || validadeTxt.trim().isEmpty()) ? null : parseData(validadeTxt);
        String observacoes = ler("Novas observações: ");
        ofertaController.editar(UUID.fromString(id), alimento, quantidade, peso, validade, observacoes);
        System.out.println("Oferta atualizada.");
    }

    private void excluirOferta() {
        String id = ler("Id da oferta: ");
        ofertaController.excluir(UUID.fromString(id));
        System.out.println("Oferta excluída.");
    }

    private void atualizarPerfil() {
        String nome = ler("Novo nome (Enter mantém): ");
        String email = ler("Novo e-mail (Enter mantém): ");
        String senha = ler("Nova senha (Enter mantém): ");
        usuarioController.atualizarPerfil(logado, nome, email, senha);
        System.out.println("Perfil atualizado.");
    }

    /* ---------------------------- RF03/RF04 ---------------------------- */
    private void menuReceptor(Receptor receptor) {
        while (true) {
            System.out.println("\n--- MENU RECEPTOR (" + receptor.getNome() + ") ---");
            System.out.println("1) Buscar ofertas por raio");
            System.out.println("2) Reservar oferta");
            System.out.println("3) Minhas reservas / cancelar");
            System.out.println("0) Sair");
            String opcao = ler("Opção: ");
            if (opcao == null) {
                return;
            }
            try {
                switch (opcao) {
                    case "1": consultarPorRaio(); break;
                    case "2": reservar(receptor); break;
                    case "3": minhasReservas(receptor); break;
                    case "0": return;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RuntimeException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    private void consultarPorRaio() {
        System.out.println("\n--- BUSCA POR LOCALIZAÇÃO (RF03) ---");
        float latitude = lerFloat("Sua latitude: ");
        float longitude = lerFloat("Sua longitude: ");
        float raio = lerFloat("Raio em km: ");
        List<OfertaDTO> ofertas = ofertaController.buscarPorRaio(latitude, longitude, raio);
        if (ofertas.isEmpty()) {
            System.out.println("Nenhuma oferta disponível no raio informado.");
        } else {
            System.out.println("\nID       | ALIMENTO           |   PESO   | VALIDADE         | STATUS     | DIST.  | ENDEREÇO | DOADOR");
            ofertas.forEach(System.out::println);
        }
    }

    private void reservar(Receptor receptor) {
        String id = ler("Id da oferta: ");
        Reserva reserva = reservaController.reservar(receptor, UUID.fromString(id));
        System.out.println("Reserva criada: " + reserva.getId() + " | status " + reserva.getStatus()
                + " | reservas ativas: " + reservaController.reservasAtivas(receptor) + "/"
                + receptor.getLimiteReservasAtivas());
    }

    private void minhasReservas(Receptor receptor) {
        System.out.println("\n--- MINHAS RESERVAS ---");
        List<Reserva> reservas = reservaController.minhasReservas(receptor);
        if (reservas.isEmpty()) {
            System.out.println("Nenhuma reserva.");
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Reserva r : reservas) {
            System.out.printf("%s | %-10s | %-18s | %6.2f kg | %s%n",
                    r.getId().toString().substring(0, 8), r.getStatus(), r.getOferta().getAlimento(),
                    r.getOferta().getPeso(), fmt.format(r.getDataReserva()));
        }
        String cancelar = ler("Cancelar alguma reserva? informe o id (Enter para voltar): ");
        if (cancelar != null && !cancelar.trim().isEmpty()) {
            reservaController.cancelar(UUID.fromString(cancelar.trim()));
            System.out.println("Reserva cancelada e oferta liberada.");
        }
    }

    /* ---------------------------- RF05 ---------------------------- */
    private void menuVoluntario(Voluntario voluntario) {
        while (true) {
            System.out.println("\n--- MENU VOLUNTÁRIO (" + voluntario.getNome() + ") ---");
            System.out.println("1) Ver solicitações de retirada");
            System.out.println("2) Assumir retirada");
            System.out.println("3) Registrar coleta");
            System.out.println("4) Registrar entrega");
            System.out.println("5) Minhas retiradas");
            System.out.println("0) Sair");
            String opcao = ler("Opção: ");
            if (opcao == null) {
                return;
            }
            try {
                switch (opcao) {
                    case "1": listarSolicitacoes(); break;
                    case "2": assumir(voluntario); break;
                    case "3": retiradaController.registrarColeta(UUID.fromString(ler("Id da retirada: ")));
                              System.out.println("Coleta registrada."); break;
                    case "4": retiradaController.registrarEntrega(UUID.fromString(ler("Id da retirada: ")));
                              System.out.println("Entrega registrada. Reserva concluída e indicador gerado."); break;
                    case "5": minhasRetiradas(voluntario); break;
                    case "0": return;
                    default: System.out.println("Opção inválida.");
                }
            } catch (RuntimeException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    private void listarSolicitacoes() {
        System.out.println("\n--- SOLICITAÇÕES DE RETIRADA (reservas ativas) ---");
        List<Reserva> solicitacoes = retiradaController.solicitacoes();
        if (solicitacoes.isEmpty()) {
            System.out.println("Nenhuma solicitação pendente.");
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Reserva r : solicitacoes) {
            System.out.printf("%s | %-18s | %6.2f kg | %s | receptor: %s%n",
                    r.getId().toString().substring(0, 8), r.getOferta().getAlimento(), r.getOferta().getPeso(),
                    fmt.format(r.getDataReserva()), r.getReceptor().getNome());
        }
    }

    private void assumir(Voluntario voluntario) {
        String id = ler("Id da reserva: ");
        Retirada retirada = retiradaController.assumir(voluntario, UUID.fromString(id));
        System.out.println("Retirada assumida. Id da retirada: " + retirada.getId());
    }

    private void minhasRetiradas(Voluntario voluntario) {
        System.out.println("\n--- MINHAS RETIRADAS ---");
        List<Retirada> retiradas = retiradaController.minhasRetiradas(voluntario);
        if (retiradas.isEmpty()) {
            System.out.println("Nenhuma retirada.");
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Retirada t : retiradas) {
            System.out.printf("%s | %-12s | %-18s | coleta: %s | entrega: %s%n",
                    t.getId().toString().substring(0, 8), t.getStatus(), t.getReserva().getOferta().getAlimento(),
                    t.getDataColeta() == null ? "-" : fmt.format(t.getDataColeta()),
                    t.getDataEntrega() == null ? "-" : fmt.format(t.getDataEntrega()));
        }
    }

    /* ---------------------------- RF06 ---------------------------- */
    private void relatorios() {
        System.out.println("\n--- RELATÓRIOS ---");
        System.out.println("1) Histórico de doações concluídas");
        System.out.println("2) Indicadores de impacto");
        String opcao = ler("Opção: ");
        try {
            if ("1".equals(opcao)) {
                List<HistoricoDTO> historico = relatorioController.historico();
                System.out.println("\nOFERTA       | ALIMENTO        |   PESO   | DOADOR -> RECEPTOR | VOLUNTÁRIO | ENTREGA");
                if (historico.isEmpty()) {
                    System.out.println("Nenhuma doação concluída ainda.");
                }
                historico.forEach(System.out::println);
            } else if ("2".equals(opcao)) {
                System.out.println("\n" + relatorioController.resumoImpacto());
            }
        } catch (RuntimeException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    /* ---------------------------- entrada ---------------------------- */
    private String ler(String mensagem) {
        System.out.print(mensagem);
        if (!in.hasNextLine()) {
            return null;
        }
        return in.nextLine().trim();
    }

    private float lerFloat(String mensagem) {
        String valor = ler(mensagem);
        if (valor == null || valor.isEmpty()) {
            return 0f;
        }
        return Float.parseFloat(valor.replace(",", "."));
    }

    private Date lerData(String mensagem) {
        String valor = ler(mensagem);
        if (valor == null || valor.trim().isEmpty()) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 3);
            System.out.println("(usando validade padrão: 3 dias a partir de agora)");
            return c.getTime();
        }
        return parseData(valor);
    }

    private Date parseData(String valor) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(valor);
        } catch (Exception e) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(valor);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Data inválida. Use dd/MM/yyyy HH:mm");
            }
        }
    }
}
