package br.com.foodlink.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Doador — «publica» 1 ◆—— 0..* OfertaDoacao.
 * +cadastrarOferta(dados) : OfertaDoacao | +notificar()
 */
public class Doador extends Usuario {

    private final List<OfertaDoacao> ofertasPublicadas = new ArrayList<>();

    public Doador(UUID id, String nome, String email, String documento, String senhaHash) {
        super(id, nome, email, documento, senhaHash);
    }

    public Doador(UUID id, String nome, String email, String documento, String senhaHash, Date dataCadastro) {
        super(id, nome, email, documento, senhaHash, dataCadastro);
    }

    /** +cadastrarOferta(dados) : OfertaDoacao */
    public OfertaDoacao cadastrarOferta(Endereco endereco, String alimento, float quantidade,
                                        float peso, Date dataValidade, String observacoes) {
        OfertaDoacao oferta = new OfertaDoacao(UUID.randomUUID(), this, endereco,
                alimento, quantidade, peso, dataValidade, observacoes);
        this.ofertasPublicadas.add(oferta);
        return oferta;
    }

    @Override
    public void notificar() {
        System.out.println("[Doador] " + getNome() + ", sua oferta foi registrada com sucesso.");
    }

    public List<OfertaDoacao> getOfertasPublicadas() { return ofertasPublicadas; }
}
