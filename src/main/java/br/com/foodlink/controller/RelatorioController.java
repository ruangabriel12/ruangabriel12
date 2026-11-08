package br.com.foodlink.controller;

import br.com.foodlink.dto.HistoricoDTO;
import br.com.foodlink.dto.IndicadorResumoDTO;
import br.com.foodlink.service.IndicadorService;
import br.com.foodlink.service.OfertaService;

import java.util.List;

/** Camada de controle — RF06 (histórico e indicadores). */
public class RelatorioController {

    private final OfertaService ofertaService;
    private final IndicadorService indicadorService;

    public RelatorioController(OfertaService ofertaService, IndicadorService indicadorService) {
        this.ofertaService = ofertaService;
        this.indicadorService = indicadorService;
    }

    public List<HistoricoDTO> historico() {
        return ofertaService.listarHistorico();
    }

    public IndicadorResumoDTO resumoImpacto() {
        return indicadorService.resumo();
    }
}
