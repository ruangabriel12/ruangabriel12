package br.com.foodlink.controller;

import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.service.UsuarioService;

import java.util.List;
import java.util.Optional;

/** Camada de controle — RF01. */
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    public Doador cadastrarDoador(String nome, String email, String documento, String senha) {
        return service.cadastrarDoador(nome, email, documento, senha);
    }

    public Receptor cadastrarReceptor(String nome, String email, String documento, String senha) {
        return service.cadastrarReceptor(nome, email, documento, senha);
    }

    public Voluntario cadastrarVoluntario(String nome, String email, String documento, String senha) {
        return service.cadastrarVoluntario(nome, email, documento, senha);
    }

    public Optional<Usuario> login(String email, String senha) {
        return service.autenticar(email, senha);
    }

    public List<Usuario> listar() {
        return service.listar();
    }

    public void atualizarPerfil(Usuario usuario, String nome, String email, String novaSenha) {
        service.atualizarPerfil(usuario.getId(), nome, email, novaSenha);
    }

    public void excluir(Usuario usuario) {
        service.excluir(usuario.getId());
    }
}
