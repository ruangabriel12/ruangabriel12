package br.com.foodlink.service;

import br.com.foodlink.dto.IndicadorResumoDTO;
import br.com.foodlink.model.IndicadorImpacto;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.repository.IndicadorRepository;

import java.util.UUID;

/** RF06 / RN05 — indicadores de reaproveitamento e CO2 evitado. */
public class IndicadorService {

    private final IndicadorRepository repository;

    public IndicadorService(IndicadorRepository repository) {
        this.repository = repository;
    }

    /** Gera (uma única vez) o indicador da oferta concluída — multiplicidade 1:1 do diagrama. */
    public IndicadorImpacto gerarPara(OfertaDoacao oferta) {
        if (repository.existeParaOferta(oferta.getId())) {
            return repository.buscarPorOferta(oferta);
        }
        IndicadorImpacto indicador = new IndicadorImpacto(UUID.randomUUID(), oferta);
        repository.salvar(indicador);
        return indicador;
    }

    public IndicadorResumoDTO resumo() {
        return repository.resumo();
    }
}
