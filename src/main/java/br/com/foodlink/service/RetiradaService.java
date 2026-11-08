package br.com.foodlink.service;

import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Retirada;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.model.enums.StatusOferta;
import br.com.foodlink.repository.OfertaRepository;
import br.com.foodlink.repository.ReservaRepository;
import br.com.foodlink.repository.RetiradaRepository;

import java.util.List;
import java.util.UUID;

/** RF05 — consulta de solicitações, assunção da retirada, registro de coleta e entrega. */
public class RetiradaService {

    private final RetiradaRepository retiradaRepository;
    private final ReservaRepository reservaRepository;
    private final OfertaRepository ofertaRepository;
    private final IndicadorService indicadorService;

    public RetiradaService(RetiradaRepository retiradaRepository, ReservaRepository reservaRepository,
                           OfertaRepository ofertaRepository, IndicadorService indicadorService) {
        this.retiradaRepository = retiradaRepository;
        this.reservaRepository = reservaRepository;
        this.ofertaRepository = ofertaRepository;
        this.indicadorService = indicadorService;
    }

    /** RF05 — solicitações de retirada disponíveis (reservas ativas ainda sem retirada). */
    public List<Reserva> listarSolicitacoes() {
        return retiradaRepository.listarSolicitacoesPendentes();
    }

    /** O voluntário assume uma reserva ativa (multiplicidade Reserva 1 -> 0..1 Retirada). */
    public Retirada assumir(Voluntario voluntario, UUID reservaId) {
        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
        Retirada retirada = voluntario.assumirRetirada(reserva);
        retiradaRepository.salvar(retirada);
        reserva.vincularRetirada(retirada);
        return retirada;
    }

    /** Registra a coleta (PENDENTE -> EM_ANDAMENTO). */
    public void registrarColeta(UUID retiradaId) {
        Retirada retirada = buscar(retiradaId);
        retirada.registrarColeta();
        retiradaRepository.atualizar(retirada);
    }

    /** Registra a entrega (EM_ANDAMENTO -> ENTREGUE), conclui a reserva e gera o indicador. */
    public void registrarEntrega(UUID retiradaId) {
        Retirada retirada = buscar(retiradaId);
        retirada.registrarEntrega();                 // ENTREGUE + reserva CONCLUIDA
        retiradaRepository.atualizar(retirada);
        reservaRepository.atualizarStatus(retirada.getReserva());
        ofertaRepository.atualizarStatus(retirada.getReserva().getOferta().getId(), StatusOferta.CONCLUIDA);
        indicadorService.gerarPara(retirada.getReserva().getOferta());
    }

    public List<Retirada> listarPorVoluntario(UUID voluntarioId) {
        return retiradaRepository.listarPorVoluntario(voluntarioId);
    }

    private Retirada buscar(UUID id) {
        return retiradaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Retirada não encontrada."));
    }
}
