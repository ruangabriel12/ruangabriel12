package br.com.foodlink.controller;

import br.com.foodlink.dto.OfertaDTO;
import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Endereco;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.service.OfertaService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/** Camada de controle — RF02, RF03 e CRUD da entidade principal. */
public class OfertaController {

    private final OfertaService service;

    public OfertaController(OfertaService service) {
        this.service = service;
    }

    public OfertaDoacao publicar(Doador doador, Endereco endereco, String alimento, float quantidade,
                                 float peso, Date dataValidade, String observacoes) {
        return service.cadastrar(doador, endereco, alimento, quantidade, peso, dataValidade, observacoes);
    }

    public void editar(UUID ofertaId, String alimento, float quantidade, float peso,
                       Date dataValidade, String observacoes) {
        service.atualizar(ofertaId, alimento, quantidade, peso, dataValidade, observacoes);
    }

    public void excluir(UUID ofertaId) {
        service.excluir(ofertaId);
    }

    public List<OfertaDTO> listarDisponiveis() {
        return service.listarDisponiveis();
    }

    public List<OfertaDTO> buscarPorRaio(double latitude, double longitude, double raioKm) {
        return service.buscarPorRaio(latitude, longitude, raioKm);
    }

    public List<OfertaDTO> listarPorDoador(Doador doador) {
        return service.listarPorDoador(doador.getId());
    }
}
