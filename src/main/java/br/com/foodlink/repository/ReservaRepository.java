package br.com.foodlink.repository;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.Receptor;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.enums.StatusReserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistência de Reserva (RF04) e verificação da RN02 (máx. 3 reservas ATIVAS).
 */
public class ReservaRepository {

    private static final String SELECT_BASE =
            "SELECT r.id, r.data_reserva, r.status, r.oferta_id, r.receptor_id FROM reserva r ";

    private final ConnectionProvider conexao;
    private final OfertaRepository ofertaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaRepository(ConnectionProvider conexao, OfertaRepository ofertaRepository,
                             UsuarioRepository usuarioRepository) {
        this.conexao = conexao;
        this.ofertaRepository = ofertaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** CREATE */
    public void salvar(Reserva r) {
        String sql = "INSERT INTO reserva (id, oferta_id, receptor_id, data_reserva, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, r.getId());
            ps.setObject(2, r.getOferta().getId());
            ps.setObject(3, r.getReceptor().getId());
            ps.setTimestamp(4, new Timestamp(r.getDataReserva().getTime()));
            ps.setString(5, r.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar reserva: " + e.getMessage(), e);
        }
    }

    /** UPDATE do status (cancelar/confirmar) */
    public void atualizarStatus(Reserva r) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("UPDATE reserva SET status = ? WHERE id = ?")) {
            ps.setString(1, r.getStatus().name());
            ps.setObject(2, r.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Reserva não encontrada para atualização.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar reserva: " + e.getMessage(), e);
        }
    }

    /** READ por id */
    public Optional<Reserva> buscarPorId(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE r.id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar reserva: " + e.getMessage(), e);
        }
    }

    /** READ — reserva ATIVA de uma oferta (RN03) */
    public Optional<Reserva> buscarAtivaPorOferta(UUID ofertaId) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE r.oferta_id = ? AND r.status = ?")) {
            ps.setObject(1, ofertaId);
            ps.setString(2, StatusReserva.ATIVA.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar reserva ativa: " + e.getMessage(), e);
        }
    }

    /** RN02 — quantidade de reservas ATIVAS do receptor */
    public int contarAtivasPorReceptor(UUID receptorId) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM reserva WHERE receptor_id = ? AND status = ?")) {
            ps.setObject(1, receptorId);
            ps.setString(2, StatusReserva.ATIVA.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao contar reservas ativas: " + e.getMessage(), e);
        }
    }

    /** READ — reservas de um receptor */
    public List<Reserva> listarPorReceptor(UUID receptorId) {
        List<Reserva> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE r.receptor_id = ? ORDER BY r.data_reserva DESC")) {
            ps.setObject(1, receptorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar reservas: " + e.getMessage(), e);
        }
        return lista;
    }

    /** READ — reservas ATIVAS (base para as solicitações de retirada — RF05) */
    public List<Reserva> listarAtivas() {
        List<Reserva> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE r.status = ? ORDER BY r.data_reserva")) {
            ps.setString(1, StatusReserva.ATIVA.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar reservas ativas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        UUID ofertaId = UUID.fromString(rs.getString("oferta_id"));
        UUID receptorId = UUID.fromString(rs.getString("receptor_id"));

        OfertaDoacao oferta = ofertaRepository.buscarPorId(ofertaId)
                .orElseThrow(() -> new PersistenciaException("Oferta da reserva não encontrada: " + ofertaId));
        Usuario u = usuarioRepository.buscarPorId(receptorId)
                .orElseThrow(() -> new PersistenciaException("Receptor da reserva não encontrado: " + receptorId));

        Date data = new Date(rs.getTimestamp("data_reserva").getTime());
        return new Reserva(UUID.fromString(rs.getString("id")), data,
                StatusReserva.valueOf(rs.getString("status")), oferta, (Receptor) u);
    }
}
