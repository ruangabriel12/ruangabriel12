package br.com.foodlink.service;

import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.repository.UsuarioRepository;
import br.com.foodlink.util.HashUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RF01 — cadastro e autenticação de Doador, Receptor e Voluntário (+ CRUD de usuários). */
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Doador cadastrarDoador(String nome, String email, String documento, String senha) {
        validar(nome, email, documento, senha);
        Doador d = new Doador(UUID.randomUUID(), nome, email, documento, HashUtil.hash(senha));
        repository.salvar(d);
        return d;
    }

    public Receptor cadastrarReceptor(String nome, String email, String documento, String senha) {
        validar(nome, email, documento, senha);
        Receptor r = new Receptor(UUID.randomUUID(), nome, email, documento, HashUtil.hash(senha));
        repository.salvar(r);
        return r;
    }

    public Voluntario cadastrarVoluntario(String nome, String email, String documento, String senha) {
        validar(nome, email, documento, senha);
        Voluntario v = new Voluntario(UUID.randomUUID(), nome, email, documento, HashUtil.hash(senha));
        repository.salvar(v);
        return v;
    }

    /** Autenticação por e-mail e senha (usa o hash SHA-256). */
    public Optional<Usuario> autenticar(String email, String senha) {
        return repository.buscarPorEmail(email)
                .filter(u -> u.autenticar(HashUtil.hash(senha)));
    }

    public List<Usuario> listar() {
        return repository.listar();
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    /** UPDATE — atualiza nome, e-mail e (opcionalmente) senha. */
    public void atualizarPerfil(UUID id, String nome, String email, String novaSenha) {
        Usuario u = repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (nome != null && !nome.trim().isEmpty()) {
            u.setNome(nome.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            u.setEmail(email.trim());
        }
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            u.setSenhaHash(HashUtil.hash(novaSenha));
        }
        u.atualizarPerfil();
        repository.atualizar(u);
    }

    /** DELETE */
    public void excluir(UUID id) {
        repository.excluir(id);
    }

    private void validar(String nome, String email, String documento, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o documento.");
        }
        if (senha == null || senha.length() < 4) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 4 caracteres.");
        }
        if (repository.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("Já existe usuário com este e-mail.");
        }
    }
}
