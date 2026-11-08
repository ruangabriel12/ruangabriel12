package br.com.foodlink.model;

import br.com.foodlink.model.enums.StatusOferta;
import br.com.foodlink.model.enums.StatusReserva;
import br.com.foodlink.model.enums.StatusRetirada;

import java.util.Date;
import java.util.UUID;

/**
 * Reserva — +cancelar() | +confirmarRetirada()
 * Associações: Receptor («realiza»), OfertaDoacao («reservada por» 0..1) e Retirada («gera» 0..1).
 */
public class Reserva {

    private UUID id;
    private Date dataReserva;
    private StatusReserva status;

    private OfertaDoacao oferta;
    private Receptor receptor;
    private Retirada retirada;

    public Reserva(UUID id, OfertaDoacao oferta, Receptor receptor) {
        this.id = id;
        this.oferta = oferta;
        this.receptor = receptor;
        this.dataReserva = new Date();
        this.status = StatusReserva.ATIVA;
        this.oferta.setStatus(StatusOferta.RESERVADA);
    }

    /** Reconstituição a partir do banco. */
    public Reserva(UUID id, Date dataReserva, StatusReserva status, OfertaDoacao oferta, Receptor receptor) {
        this.id = id;
        this.dataReserva = dataReserva;
        this.status = status;
        this.oferta = oferta;
        this.receptor = receptor;
    }

    /** +cancelar() */
    public void cancelar() {
        if (this.status != StatusReserva.ATIVA) {
            throw new IllegalStateException("Somente reservas ATIVAS podem ser canceladas.");
        }
        if (this.retirada != null && this.retirada.getStatus() != StatusRetirada.PENDENTE) {
            throw new IllegalStateException("Retirada já iniciada: não é possível cancelar a reserva.");
        }
        this.status = StatusReserva.CANCELADA;
        this.oferta.setStatus(StatusOferta.DISPONIVEL);
    }

    /** +confirmarRetirada() */
    public void confirmarRetirada() {
        if (this.status != StatusReserva.ATIVA) {
            throw new IllegalStateException("Somente reservas ATIVAS podem ser confirmadas.");
        }
        this.status = StatusReserva.CONCLUIDA;
        this.oferta.setStatus(StatusOferta.CONCLUIDA);
    }

    public void vincularRetirada(Retirada retirada) { this.retirada = retirada; }

    public UUID getId() { return id; }
    public Date getDataReserva() { return dataReserva; }
    public StatusReserva getStatus() { return status; }
    public OfertaDoacao getOferta() { return oferta; }
    public Receptor getReceptor() { return receptor; }
    public Retirada getRetirada() { return retirada; }
}
