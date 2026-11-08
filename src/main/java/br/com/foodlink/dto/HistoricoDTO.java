package br.com.foodlink.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

/** DTO do histórico de doações concluídas (RF06 / RN04). */
public class HistoricoDTO {

    private final String ofertaId;
    private final String alimento;
    private final double peso;
    private final String doador;
    private final String receptor;
    private final String voluntario;
    private final Date dataEntrega;

    public HistoricoDTO(String ofertaId, String alimento, double peso, String doador,
                        String receptor, String voluntario, Date dataEntrega) {
        this.ofertaId = ofertaId;
        this.alimento = alimento;
        this.peso = peso;
        this.doador = doador;
        this.receptor = receptor;
        this.voluntario = voluntario;
        this.dataEntrega = dataEntrega;
    }

    public String getOfertaId() { return ofertaId; }
    public String getAlimento() { return alimento; }
    public double getPeso() { return peso; }
    public String getDoador() { return doador; }
    public String getReceptor() { return receptor; }
    public String getVoluntario() { return voluntario; }
    public Date getDataEntrega() { return dataEntrega; }

    @Override
    public String toString() {
        String data = dataEntrega == null ? "-" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dataEntrega);
        return String.format("%-12s | %-15s | %6.2f kg | %s | %s -> %s | %s",
                ofertaId, alimento, peso, doador, receptor, voluntario, data);
    }
}
