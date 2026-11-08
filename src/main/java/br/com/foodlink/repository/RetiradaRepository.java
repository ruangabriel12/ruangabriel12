package br.com.foodlink.repository;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.model.Reserva;
import br.com.foodlink.model.Retirada;
import br.com.foodlink.model.Usuario;
import br.com.foodlink.model.Voluntario;
import br.com.foodlink.model.enums.StatusRetirada;

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
 * Persistência de Retirada (RF05): consulta de solicitações, coleta e entrega.
 */
public class RetiradaRepository {

    private static final String SELECT_BASE = "SELECT t.id, t.data_coleta, t.data_entrega, t.status, "
            + "t.reserva_id, t.voluntario_id FROM retirada t ";

    private final ConnectionProvider conexao;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    public RetiradaRepository(ConnectionProvider conexao, ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository) {
        this.conexao = conexao;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** CREATE */
    public void salvar(Retirada t) {
        String sql = "INSERT INTO retirada (id, reserva_id, voluntario_id, data_coleta, data_entrega, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, t.getId());
            ps.setObject(2, t.getReserva().getId());
            ps.setObject(3, t.getVoluntario().getId());
            ps.setTimestamp(4, t.getDataColeta() == null ? null : new Timestamp(t.getDataColeta().getTime()));
            ps.setTimestamp(5, t.getDataEntrega() == null ? null : new Timestamp(t.getDataEntrega().getTime()));
            ps.setString(6, t.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar retirada: " + e.getMessage(), e);
        }
    }

    /** UPDATE (coleta/entrega) */
    public void atualizar(Retirada t) {
        String sql = "UPDATE retirada SET data_coleta = ?, data_entrega = ?, status = ? WHERE id = ?";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, t.getDataColeta() == null ? null : new Timestamp(t.getDataColeta().getTime()));
            ps.setTimestamp(2, t.getDataEntrega() == null ? null : new Timestamp(t.getDataEntrega().getTime()));
            ps.setString(3, t.getStatus().name());
            ps.setObject(4, t.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Retirada não encontrada para atualização.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar retirada: " + e.getMessage(), e);
        }
    }

    public Optional<Retirada> buscarPorId(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE t.id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar retirada: " + e.getMessage(), e);
        }
    }

    /** RF05 — solicitações de retirada ainda não assumidas (reservas ATIVAS sem retirada) */
    public List<Reserva> listarSolicitacoesPendentes() {
        String sql = "SELECT r.id, r.data_reserva, r.status, r.oferta_id, r.receptor_id FROM reserva r "
                + "LEFT JOIN retirada t ON t.reserva_id = r.id "
                + "WHERE r.status = 'ATIVA' AND t.id IS NULL ORDER BY r.data_reserva";
        List<Reserva> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID reservaId = UUID.fromString(rs.getString("id"));
                reservaRepository.buscarPorId(reservaId).ifPresent(lista::add);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar solicitações pendentes: " + e.getMessage(), e);
        }
        return lista;
    }

    /** READ — retiradas de um voluntário */
    public List<Retirada> listarPorVoluntario(UUID voluntarioId) {
        List<Retirada> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE t.voluntario_id = ? ORDER BY t.id")) {
            ps.setObject(1, voluntarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar retiradas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Retirada mapear(ResultSet rs) throws SQLException {
        Reserva reserva = reservaRepository.buscarPorId(UUID.fromString(rs.getString("reserva_id")))
                .orElseThrow(() -> new PersistenciaException("Reserva da retirada não encontrada."));
        Usuario u = usuarioRepository.buscarPorId(UUID.fromString(rs.getString("voluntario_id")))
                .orElseThrow(() -> new PersistenciaException("Voluntário da retirada não encontrado."));

        Timestamp coleta = rs.getTimestamp("data_coleta");
        Timestamp entrega = rs.getTimestamp("data_entrega");
        return new Retirada(UUID.fromString(rs.getString("id")),
                coleta == null ? null : new Date(coleta.getTime()),
                entrega == null ? null : new Date(entrega.getTime()),
                StatusRetirada.valueOf(rs.getString("status")),
                reserva, (Voluntario) u);
    }
}
