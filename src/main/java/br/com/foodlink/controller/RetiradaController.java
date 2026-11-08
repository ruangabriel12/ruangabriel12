package br.com.foodlink.controller;

import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Retirada;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.service.RetiradaService;

import java.util.List;
import java.util.UUID;

/** Camada de controle — RF05. */
public class RetiradaController {

    private final RetiradaService service;

    public RetiradaController(RetiradaService service) {
        this.service = service;
    }

    public List<Reserva> solicitacoes() {
        return service.listarSolicitacoes();
    }

    public Retirada assumir(Voluntario voluntario, UUID reservaId) {
        return service.assumir(voluntario, reservaId);
    }

    public void registrarColeta(UUID retiradaId) {
        service.registrarColeta(retiradaId);
    }

    public void registrarEntrega(UUID retiradaId) {
        service.registrarEntrega(retiradaId);
    }

    public List<Retirada> minhasRetiradas(Voluntario voluntario) {
        return service.listarPorVoluntario(voluntario.getId());
    }
}
