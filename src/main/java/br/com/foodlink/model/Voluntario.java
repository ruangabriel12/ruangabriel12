package br.com.foodlink.model;

import java.util.Date;
import java.util.UUID;

/**
 * Voluntario — «executa» 1 ◆—— 0..* Retirada.
 * +assumirRetirada(reserva) : Retirada | +registrarEntrega(retirada) | +notificar()
 */
public class Voluntario extends Usuario {

    public Voluntario(UUID id, String nome, String email, String documento, String senhaHash) {
        super(id, nome, email, documento, senhaHash);
    }

    public Voluntario(UUID id, String nome, String email, String documento, String senhaHash, Date dataCadastro) {
        super(id, nome, email, documento, senhaHash, dataCadastro);
    }

    /** +assumirRetirada(reserva) : Retirada */
    public Retirada assumirRetirada(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva não informada.");
        }
        return new Retirada(UUID.randomUUID(), reserva, this);
    }

    /** +registrarEntrega(retirada) */
    public void registrarEntrega(Retirada retirada) {
        if (retirada == null) {
            throw new IllegalArgumentException("Retirada não informada.");
        }
        retirada.registrarEntrega();
    }

    @Override
    public void notificar() {
        System.out.println("[Voluntario] " + getNome() + ", você tem uma retirada atribuída.");
    }
}
