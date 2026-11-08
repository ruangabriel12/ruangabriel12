package br.com.foodlink.repository;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistência de Usuario (herança mapeada em tabela única com discriminador "tipo").
 * Atende ao RF01 (cadastro/autenticação) e ao CRUD de usuários.
 */
public class UsuarioRepository {

    private static final String CAMPOS =
            "id, tipo, nome, email, documento, senha_hash, data_cadastro, limite_reservas_ativas";

    private final ConnectionProvider conexao;

    public UsuarioRepository(ConnectionProvider conexao) {
        this.conexao = conexao;
    }

    public static String tipoDe(Usuario u) {
        if (u instanceof Doador) return "DOADOR";
        if (u instanceof Receptor) return "RECEPTOR";
        if (u instanceof Voluntario) return "VOLUNTARIO";
        throw new IllegalArgumentException("Tipo de usuário não suportado: " + u.getClass().getSimpleName());
    }

    /** CREATE */
    public void salvar(Usuario u) {
        String sql = "INSERT INTO usuario (" + CAMPOS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, u.getId());
            ps.setString(2, tipoDe(u));
            ps.setString(3, u.getNome());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getDocumento());
            ps.setString(6, u.getSenhaHash());
            ps.setTimestamp(7, new Timestamp(u.getDataCadastro().getTime()));
            if (u instanceof Receptor) {
                ps.setInt(8, ((Receptor) u).getLimiteReservasAtivas());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao cadastrar usuário: " + e.getMessage(), e);
        }
    }

    /** READ por id */
    public Optional<Usuario> buscarPorId(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT " + CAMPOS + " FROM usuario WHERE id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
    }

    /** READ por e-mail (usado na autenticação — RF01) */
    public Optional<Usuario> buscarPorEmail(String email) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT " + CAMPOS + " FROM usuario WHERE LOWER(email) = LOWER(?)")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar usuário por e-mail: " + e.getMessage(), e);
        }
    }

    /** READ — lista todos */
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT " + CAMPOS + " FROM usuario ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar usuários: " + e.getMessage(), e);
        }
        return usuarios;
    }

    /** UPDATE */
    public void atualizar(Usuario u) {
        String sql = "UPDATE usuario SET nome = ?, email = ?, documento = ?, senha_hash = ?, "
                + "limite_reservas_ativas = ? WHERE id = ?";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getDocumento());
            ps.setString(4, u.getSenhaHash());
            if (u instanceof Receptor) {
                ps.setInt(5, ((Receptor) u).getLimiteReservasAtivas());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setObject(6, u.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Usuário não encontrado para atualização.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
    }

    /** DELETE */
    public void excluir(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
            ps.setObject(1, id);
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Usuário não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir usuário: " + e.getMessage(), e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String documento = rs.getString("documento");
        String senha = rs.getString("senha_hash");
        java.util.Date data = new java.util.Date(rs.getTimestamp("data_cadastro").getTime());
        switch (rs.getString("tipo")) {
            case "DOADOR":
                return new Doador(id, nome, email, documento, senha, data);
            case "RECEPTOR":
                return new Receptor(id, nome, email, documento, senha, data, rs.getInt("limite_reservas_ativas"));
            case "VOLUNTARIO":
                return new Voluntario(id, nome, email, documento, senha, data);
            default:
                throw new PersistenciaException("Tipo de usuário desconhecido no banco.");
        }
    }
}
