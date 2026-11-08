package br.com.foodlink.model;

import java.util.Date;
import java.util.UUID;

/**
 * Receptor — -int limiteReservasAtivas (RN02: 3), +reservarOferta(oferta) : Reserva, +notificar().
 * A contagem de reservas ativas é verificada no ReservaService consultando o banco.
 */
public class Receptor extends Usuario {

    private int limiteReservasAtivas = 3;

    public Receptor(UUID id, String nome, String email, String documento, String senhaHash) {
        super(id, nome, email, documento, senhaHash);
    }

    public Receptor(UUID id, String nome, String email, String documento, String senhaHash,
                    Date dataCadastro, int limiteReservasAtivas) {
        super(id, nome, email, documento, senhaHash, dataCadastro);
        this.limiteReservasAtivas = limiteReservasAtivas;
    }

    /** +reservarOferta(oferta) : Reserva */
    public Reserva reservarOferta(OfertaDoacao oferta) {
        if (oferta == null) {
            throw new IllegalArgumentException("Oferta não informada.");
        }
        return new Reserva(UUID.randomUUID(), oferta, this);
    }

    @Override
    public void notificar() {
        System.out.println("[Receptor] " + getNome() + ", reserva efetuada com sucesso.");
    }

    public int getLimiteReservasAtivas() { return limiteReservasAtivas; }
}
