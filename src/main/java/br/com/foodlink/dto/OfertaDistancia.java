package br.com.foodlink.dto;

import br.com.foodlink.model.OfertaDoacao;

/** Par (oferta, distância em km) devolvido pela busca geográfica — RF03. */
public class OfertaDistancia {

    private final OfertaDoacao oferta;
    private final double distanciaKm;

    public OfertaDistancia(OfertaDoacao oferta, double distanciaKm) {
        this.oferta = oferta;
        this.distanciaKm = distanciaKm;
    }

    public OfertaDoacao getOferta() { return oferta; }
    public double getDistanciaKm() { return distanciaKm; }
}
