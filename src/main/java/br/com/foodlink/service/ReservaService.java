package br.com.foodlink.service;

import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.enums.StatusOferta;
import br.com.foodlink.repository.OfertaRepository;
import br.com.foodlink.repository.ReservaRepository;

import java.util.List;
import java.util.UUID;

/** RF04 — reserva de ofertas, com RN02 (limite de 3 ativas) e RN03 (exclusividade). */
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final OfertaRepository ofertaRepository;

    public ReservaService(ReservaRepository reservaRepository, OfertaRepository ofertaRepository) {
        this.reservaRepository = reservaRepository;
        this.ofertaRepository = ofertaRepository;
    }

    /** Reserva uma oferta disponível. */
    public Reserva reservar(Receptor receptor, UUID ofertaId) {
        OfertaDoacao oferta = ofertaRepository.buscarPorId(ofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));

        // RN03 — exclusividade
        if (oferta.getStatus() != StatusOferta.DISPONIVEL) {
            throw new IllegalStateException("RN03: oferta indisponível para reserva (status " + oferta.getStatus() + ").");
        }
        // RN01 — validade mínima no momento da reserva
        if (!oferta.verificarValidadeMinima()) {
            throw new IllegalStateException("RN01: oferta com menos de 2 horas de validade.");
        }
        // RN02 — limite de reservas ativas (contado no banco)
        int ativas = reservaRepository.contarAtivasPorReceptor(receptor.getId());
        if (ativas >= receptor.getLimiteReservasAtivas()) {
            throw new IllegalStateException("RN02: o receptor já possui " + ativas
                    + " reservas ativas (limite " + receptor.getLimiteReservasAtivas() + ").");
        }

        Reserva reserva = receptor.reservarOferta(oferta);
        reservaRepository.salvar(reserva);
        ofertaRepository.atualizarStatus(oferta.getId(), StatusOferta.RESERVADA);
        receptor.notificar();
        return reserva;
    }

    /** Cancela uma reserva ativa e devolve a oferta ao estoque. */
    public void cancelar(UUID reservaId) {
        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
        reserva.cancelar();
        reservaRepository.atualizarStatus(reserva);
        ofertaRepository.atualizarStatus(reserva.getOferta().getId(), StatusOferta.DISPONIVEL);
    }

    public List<Reserva> listarPorReceptor(UUID receptorId) {
        return reservaRepository.listarPorReceptor(receptorId);
    }

    /** RN02 — usado pelo menu para mostrar quantas reservas ativas o receptor ainda pode fazer. */
    public int reservasAtivas(UUID receptorId) {
        return reservaRepository.contarAtivasPorReceptor(receptorId);
    }

    public Reserva buscarPorId(UUID id) {
        return reservaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
    }

    public List<Reserva> listarAtivas() {
        return reservaRepository.listarAtivas();
    }
}
