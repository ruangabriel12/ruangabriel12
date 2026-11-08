package br.com.foodlink.repository;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.dto.HistoricoDTO;
import br.com.foodlink.dto.OfertaDistancia;
import br.com.foodlink.model.Doador;
import br.com.foodlink.model.Endereco;
import br.com.foodlink.model.OfertaDoacao;
import br.com.foodlink.model.enums.StatusOferta;

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
 * CRUD completo da entidade principal (OfertaDoacao).
 * Inclui a busca por localização/raio (RF03) usando a fórmula de Haversine em SQL.
 */
public class OfertaRepository {

    /**
     * Distância (km) entre o ponto informado (?, ?, ?) e o endereço da oferta.
     * Haversine — funciona em PostgreSQL e em H2 (usado nos testes), sem exigir PostGIS.
     */
    private static final String EXPR_DISTANCIA =
            "(6371 * 2 * ASIN(SQRT(POWER(SIN(RADIANS(? - e.latitude) / 2), 2) "
          + "+ COS(RADIANS(?)) * COS(RADIANS(e.latitude)) * POWER(SIN(RADIANS(? - e.longitude) / 2), 2))))";

    private static final String SELECT_BASE =
            "SELECT o.id, o.alimento, o.quantidade, o.peso, o.data_validade, o.observacoes, o.status, "
          + "       d.id AS doador_id, d.nome AS doador_nome, d.email AS doador_email, "
          + "       d.documento AS doador_documento, d.senha_hash AS doador_senha, "
          + "       d.data_cadastro AS doador_data, "
          + "       e.id AS end_id, e.rua, e.cidade, e.cep, e.latitude, e.longitude "
          + "FROM oferta_doacao o "
          + "JOIN usuario d ON d.id = o.doador_id "
          + "JOIN endereco e ON e.id = o.endereco_id ";

    private final ConnectionProvider conexao;

    public OfertaRepository(ConnectionProvider conexao) {
        this.conexao = conexao;
    }

    /** CREATE */
    public void salvar(OfertaDoacao o) {
        String sql = "INSERT INTO oferta_doacao (id, doador_id, endereco_id, alimento, quantidade, peso, "
                + "data_validade, observacoes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, o.getId());
            ps.setObject(2, o.getDoador().getId());
            ps.setObject(3, o.getEndereco().getId());
            ps.setString(4, o.getAlimento());
            ps.setDouble(5, o.getQuantidade());
            ps.setDouble(6, o.getPeso());
            ps.setTimestamp(7, new Timestamp(o.getDataValidade().getTime()));
            ps.setString(8, o.getObservacoes());
            ps.setString(9, o.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar oferta: " + e.getMessage(), e);
        }
    }

    /** UPDATE dos dados da oferta */
    public void atualizar(OfertaDoacao o) {
        String sql = "UPDATE oferta_doacao SET alimento = ?, quantidade = ?, peso = ?, data_validade = ?, "
                + "observacoes = ?, status = ? WHERE id = ?";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, o.getAlimento());
            ps.setDouble(2, o.getQuantidade());
            ps.setDouble(3, o.getPeso());
            ps.setTimestamp(4, new Timestamp(o.getDataValidade().getTime()));
            ps.setString(5, o.getObservacoes());
            ps.setString(6, o.getStatus().name());
            ps.setObject(7, o.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Oferta não encontrada para atualização.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar oferta: " + e.getMessage(), e);
        }
    }

    public void atualizarStatus(UUID ofertaId, StatusOferta status) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("UPDATE oferta_doacao SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setObject(2, ofertaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar status da oferta: " + e.getMessage(), e);
        }
    }

    /** DELETE */
    public void excluir(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM oferta_doacao WHERE id = ?")) {
            ps.setObject(1, id);
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("Oferta não encontrada para exclusão.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir oferta: " + e.getMessage(), e);
        }
    }

    /** READ por id */
    public Optional<OfertaDoacao> buscarPorId(UUID id) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE o.id = ?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar oferta: " + e.getMessage(), e);
        }
    }

    /** READ — todas as ofertas disponíveis (sem filtro geográfico) */
    public List<OfertaDistancia> listarDisponiveis() {
        List<OfertaDistancia> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE o.status = ? ORDER BY o.data_validade")) {
            ps.setString(1, StatusOferta.DISPONIVEL.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OfertaDistancia(mapear(rs), -1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar ofertas disponíveis: " + e.getMessage(), e);
        }
        return lista;
    }

    /** READ — busca por localização e raio de distância (RF03) */
    public List<OfertaDistancia> buscarPorRaio(double latitude, double longitude, double raioKm) {
        String sql = "SELECT * FROM ("
                + "  SELECT o.id, o.alimento, o.quantidade, o.peso, o.data_validade, o.observacoes, o.status, "
                + "         d.id AS doador_id, d.nome AS doador_nome, d.email AS doador_email, "
                + "         d.documento AS doador_documento, d.senha_hash AS doador_senha, "
                + "         d.data_cadastro AS doador_data, "
                + "         e.id AS end_id, e.rua, e.cidade, e.cep, e.latitude, e.longitude, "
                +         EXPR_DISTANCIA + " AS distancia_km "
                + "  FROM oferta_doacao o "
                + "  JOIN usuario d ON d.id = o.doador_id "
                + "  JOIN endereco e ON e.id = o.endereco_id "
                + "  WHERE o.status = ?"
                + ") t WHERE t.distancia_km <= ? ORDER BY t.distancia_km";
        List<OfertaDistancia> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, latitude);
            ps.setDouble(3, longitude);
            ps.setString(4, StatusOferta.DISPONIVEL.name());
            ps.setDouble(5, raioKm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OfertaDistancia(mapear(rs), rs.getDouble("distancia_km")));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro na busca por raio: " + e.getMessage(), e);
        }
        return lista;
    }

    /** READ — ofertas de um doador (para o menu do doador) */
    public List<OfertaDistancia> listarPorDoador(UUID doadorId) {
        List<OfertaDistancia> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE o.doador_id = ? ORDER BY o.data_validade")) {
            ps.setObject(1, doadorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OfertaDistancia(mapear(rs), -1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar ofertas do doador: " + e.getMessage(), e);
        }
        return lista;
    }

    /** READ — histórico de doações concluídas (RF06 / RN04) */
    public List<HistoricoDTO> listarHistorico() {
        String sql = "SELECT o.id, o.alimento, o.peso, d.nome AS doador, r.nome AS receptor, "
                + "       v.nome AS voluntario, t.data_entrega "
                + "FROM retirada t "
                + "JOIN reserva rs ON rs.id = t.reserva_id "
                + "JOIN oferta_doacao o ON o.id = rs.oferta_id "
                + "JOIN usuario d ON d.id = o.doador_id "
                + "JOIN usuario r ON r.id = rs.receptor_id "
                + "JOIN usuario v ON v.id = t.voluntario_id "
                + "WHERE t.status = 'ENTREGUE' "
                + "ORDER BY t.data_entrega DESC";
        List<HistoricoDTO> lista = new ArrayList<>();
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp entrega = rs.getTimestamp("data_entrega");
                lista.add(new HistoricoDTO(rs.getString("id"), rs.getString("alimento"), rs.getDouble("peso"),
                        rs.getString("doador"), rs.getString("receptor"), rs.getString("voluntario"),
                        entrega == null ? null : new java.util.Date(entrega.getTime())));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar histórico: " + e.getMessage(), e);
        }
        return lista;
    }

    private OfertaDoacao mapear(ResultSet rs) throws SQLException {
        Doador doador = new Doador(
                UUID.fromString(rs.getString("doador_id")),
                rs.getString("doador_nome"),
                rs.getString("doador_email"),
                rs.getString("doador_documento"),
                rs.getString("doador_senha"),
                new java.util.Date(rs.getTimestamp("doador_data").getTime()));

        Endereco endereco = new Endereco(
                UUID.fromString(rs.getString("end_id")),
                rs.getString("rua"),
                rs.getString("cidade"),
                rs.getString("cep"),
                rs.getFloat("latitude"),
                rs.getFloat("longitude"));

        return new OfertaDoacao(
                UUID.fromString(rs.getString("id")),
                doador,
                endereco,
                rs.getString("alimento"),
                rs.getFloat("quantidade"),
                rs.getFloat("peso"),
                new java.util.Date(rs.getTimestamp("data_validade").getTime()),
                rs.getString("observacoes"),
                StatusOferta.valueOf(rs.getString("status")));
    }
}
