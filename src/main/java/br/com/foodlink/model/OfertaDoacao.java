package br.com.foodlink.model;

import br.com.foodlink.model.enums.StatusOferta;

import java.util.Date;
import java.util.UUID;

/**
 * OfertaDoacao — entidade principal do CRUD.
 * Atributos do diagrama + papéis das associações (doador, endereco).
 * +verificarValidadeMinima() : bool (RN01)
 */
public class OfertaDoacao {

    private UUID id;
    private String alimento;
    private float quantidade;
    private float peso;
    private Date dataValidade;
    private String observacoes;
    private StatusOferta status;

    private Doador doador;
    private Endereco endereco;

    public OfertaDoacao(UUID id, Doador doador, Endereco endereco, String alimento,
                        float quantidade, float peso, Date dataValidade, String observacoes) {
        this(id, doador, endereco, alimento, quantidade, peso, dataValidade, observacoes, StatusOferta.DISPONIVEL);
    }

    /** Reconstituição a partir do banco de dados. */
    public OfertaDoacao(UUID id, Doador doador, Endereco endereco, String alimento, float quantidade,
                        float peso, Date dataValidade, String observacoes, StatusOferta status) {
        this.id = id;
        this.doador = doador;
        this.endereco = endereco;
        this.alimento = alimento;
        this.quantidade = quantidade;
        this.peso = peso;
        this.dataValidade = dataValidade;
        this.observacoes = observacoes;
        this.status = status;
    }

    /** +verificarValidadeMinima() : bool — RN01: pelo menos 2 horas de validade. */
    public boolean verificarValidadeMinima() {
        final long DUAS_HORAS_MS = 2L * 60 * 60 * 1000;
        return (dataValidade.getTime() - System.currentTimeMillis()) >= DUAS_HORAS_MS;
    }

    public UUID getId() { return id; }
    public Doador getDoador() { return doador; }
    public Endereco getEndereco() { return endereco; }
    public String getAlimento() { return alimento; }
    public float getQuantidade() { return quantidade; }
    public float getPeso() { return peso; }
    public Date getDataValidade() { return dataValidade; }
    public String getObservacoes() { return observacoes; }
    public StatusOferta getStatus() { return status; }

    /* Acessores usados pela camada de serviço/repositório (o diagrama não lista getters/setters). */
    public void setAlimento(String alimento) { this.alimento = alimento; }
    public void setQuantidade(float quantidade) { this.quantidade = quantidade; }
    public void setPeso(float peso) { this.peso = peso; }
    public void setDataValidade(Date dataValidade) { this.dataValidade = dataValidade; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public void setStatus(StatusOferta status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%s | %.2f un | %.2f kg | validade %s | %s | %s",
                alimento, quantidade, peso,
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(dataValidade),
                status, endereco);
    }
}
