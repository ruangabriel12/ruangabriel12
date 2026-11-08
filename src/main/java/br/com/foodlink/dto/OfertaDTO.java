package br.com.foodlink.dto;

import br.com.foodlink.model.OfertaDoacao;

import java.text.SimpleDateFormat;

/** DTO de leitura de oferta, já com a distância calculada (RF03). */
public class OfertaDTO {

    private final String id;
    private final String alimento;
    private final float quantidade;
    private final float peso;
    private final String validade;
    private final String status;
    private final String endereco;
    private final String doador;
    private final double distanciaKm;

    public OfertaDTO(OfertaDoacao oferta, double distanciaKm) {
        this.id = oferta.getId().toString();
        this.alimento = oferta.getAlimento();
        this.quantidade = oferta.getQuantidade();
        this.peso = oferta.getPeso();
        this.validade = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(oferta.getDataValidade());
        this.status = oferta.getStatus().name();
        this.endereco = oferta.getEndereco() == null ? "-" : oferta.getEndereco().toString();
        this.doador = oferta.getDoador() == null ? "-" : oferta.getDoador().getNome();
        this.distanciaKm = distanciaKm;
    }

    public String getId() { return id; }
    public String getAlimento() { return alimento; }
    public float getQuantidade() { return quantidade; }
    public float getPeso() { return peso; }
    public String getValidade() { return validade; }
    public String getStatus() { return status; }
    public String getEndereco() { return endereco; }
    public String getDoador() { return doador; }
    public double getDistanciaKm() { return distanciaKm; }

    @Override
    public String toString() {
        String distancia = distanciaKm < 0 ? "    -  " : String.format("%5.1f km", distanciaKm);
        return String.format("%-10s | %-18s | %7.2f kg | val %s | %-10s | %s | %s | %s",
                id.substring(0, 8), alimento, peso, validade, status, distancia, endereco, doador);
    }
}
