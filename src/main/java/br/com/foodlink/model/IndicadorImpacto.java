package br.com.foodlink.model;

import java.util.Date;
import java.util.UUID;

/**
 * IndicadorImpacto — +calcularImpacto(peso) : float (público), RN05.
 * Associação «gera» com OfertaDoacao.
 */
public class IndicadorImpacto {

    private static final float FATOR_CO2_POR_KG = 2.5f;

    private UUID id;
    private float pesoTotalKg;
    private float co2EvitadoKg;
    private Date dataCalculo;

    private OfertaDoacao oferta;

    public IndicadorImpacto(UUID id, OfertaDoacao oferta) {
        this(id, oferta.getPeso(), calcularEstatico(oferta.getPeso()), new Date(), oferta);
    }

    /** Reconstituição a partir do banco. */
    public IndicadorImpacto(UUID id, float pesoTotalKg, float co2EvitadoKg, Date dataCalculo, OfertaDoacao oferta) {
        this.id = id;
        this.pesoTotalKg = pesoTotalKg;
        this.co2EvitadoKg = co2EvitadoKg;
        this.dataCalculo = dataCalculo;
        this.oferta = oferta;
    }

    /** +calcularImpacto(peso) : float */
    public float calcularImpacto(float peso) {
        return peso * FATOR_CO2_POR_KG;
    }

    private static float calcularEstatico(float peso) {
        return peso * FATOR_CO2_POR_KG;
    }

    public UUID getId() { return id; }
    public float getPesoTotalKg() { return pesoTotalKg; }
    public float getCo2EvitadoKg() { return co2EvitadoKg; }
    public Date getDataCalculo() { return dataCalculo; }
    public OfertaDoacao getOferta() { return oferta; }
}
