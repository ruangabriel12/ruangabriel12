package br.com.foodlink.model;

import java.util.Date;
import java.util.UUID;

/**
 * «abstract» Usuario — atributos privados (-), +autenticar(senha):bool (concreto),
 * +atualizarPerfil(), +notificar() abstrato (itálico no diagrama).
 */
public abstract class Usuario {

    private UUID id;
    private String nome;
    private String email;
    private String documento;
    private String senhaHash;
    private Date dataCadastro;

    protected Usuario(UUID id, String nome, String email, String documento, String senhaHash) {
        this(id, nome, email, documento, senhaHash, new Date());
    }

    /** Usado na reconstituição a partir do banco (preserva dataCadastro). */
    protected Usuario(UUID id, String nome, String email, String documento, String senhaHash, Date dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.documento = documento;
        this.senhaHash = senhaHash;
        this.dataCadastro = dataCadastro;
    }

    /** +autenticar(senha) : bool */
    public boolean autenticar(String senha) {
        if (senha == null || this.senhaHash == null) {
            return false;
        }
        return this.senhaHash.equals(senha);
    }

    /** +atualizarPerfil() */
    public void atualizarPerfil() {
        // Validações de perfil são aplicadas pelo UsuarioService antes da persistência.
    }

    /** +notificar() — abstrato no diagrama. */
    public abstract void notificar();

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getDocumento() { return documento; }
    public String getSenhaHash() { return senhaHash; }
    public Date getDataCadastro() { return dataCadastro; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setDocumento(String documento) { this.documento = documento; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
}
