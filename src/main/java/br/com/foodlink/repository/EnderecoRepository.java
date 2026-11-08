package br.com.foodlink.repository;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.model.Endereco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/** CRUD de Endereco (associação «possui» com OfertaDoacao). */
public class EnderecoRepository {

    private final ConnectionProvider conexao;

    public EnderecoRepository(ConnectionProvider conexao) {
        this.conexao = conexao;
    }

    public void salvar(Endereco e) {
        String sql = "INSERT INTO endereco (id, rua, cidade, cep, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, e.getId());
            ps.setString(2, e.getRua());
            ps.setString(3, e.getCidade());
            ps.setString(4, e.getCep());
            ps.setDouble(5, e.getLatitude());
            ps.setDouble(6, e.getLongitude());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenciaException("Erro ao salvar endereço: " + ex.getMessage(), ex);
        }
    }

    public Optional<Endereco> buscarPorId(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, rua, cidade, cep, latitude, longitude FROM endereco WHERE id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Endereco(UUID.fromString(rs.getString("id")), rs.getString("rua"),
                            rs.getString("cidade"), rs.getString("cep"),
                            rs.getFloat("latitude"), rs.getFloat("longitude")));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenciaException("Erro ao buscar endereço: " + ex.getMessage(), ex);
        }
    }

    public void atualizar(Endereco e) {
        String sql = "UPDATE endereco SET rua = ?, cidade = ?, cep = ?, latitude = ?, longitude = ? WHERE id = ?";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getRua());
            ps.setString(2, e.getCidade());
            ps.setString(3, e.getCep());
            ps.setDouble(4, e.getLatitude());
            ps.setDouble(5, e.getLongitude());
            ps.setObject(6, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenciaException("Erro ao atualizar endereço: " + ex.getMessage(), ex);
        }
    }

    public void excluir(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM endereco WHERE id = ?")) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenciaException("Erro ao excluir endereço: " + ex.getMessage(), ex);
        }
    }
}
