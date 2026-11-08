package br.com.foodlink.controller;

import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.service.ReservaService;

import java.util.List;
import java.util.UUID;

/** Camada de controle — RF04. */
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    public Reserva reservar(Receptor receptor, UUID ofertaId) {
        return service.reservar(receptor, ofertaId);
    }

    public void cancelar(UUID reservaId) {
        service.cancelar(reservaId);
    }

    public List<Reserva> minhasReservas(Receptor receptor) {
        return service.listarPorReceptor(receptor.getId());
    }

    public int reservasAtivas(Receptor receptor) {
        return service.reservasAtivas(receptor.getId());
    }
}
