package br.com.foodlink.repository;

import br.com.foodlink.config.ConnectionProvider;
import br.com.foodlink.dto.IndicadorResumoDTO;
import br.com.foodlink.model.IndicadorImpacto;
import br.com.foodlink.model.OfertaDoacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/** Persistência dos indicadores de impacto (RF06 / RN05). */
public class IndicadorRepository {

    private final ConnectionProvider conexao;

    public IndicadorRepository(ConnectionProvider conexao) {
        this.conexao = conexao;
    }

    /** CREATE */
    public void salvar(IndicadorImpacto i) {
        String sql = "INSERT INTO indicador_impacto (id, oferta_id, peso_total_kg, co2_evitado_kg, data_calculo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, i.getId());
            ps.setObject(2, i.getOferta().getId());
            ps.setDouble(3, i.getPesoTotalKg());
            ps.setDouble(4, i.getCo2EvitadoKg());
            ps.setTimestamp(5, new Timestamp(i.getDataCalculo().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar indicador: " + e.getMessage(), e);
        }
    }

    /** READ — totais de reaproveitamento e CO2 evitado (RF06) */
    public IndicadorResumoDTO resumo() {
        String sql = "SELECT COUNT(*) AS qtd, "
                + "       COALESCE(SUM(i.peso_total_kg), 0) AS peso, "
                + "       COALESCE(SUM(i.co2_evitado_kg), 0) AS co2, "
                + "       COALESCE(SUM(o.quantidade), 0) AS quantidade "
                + "FROM indicador_impacto i JOIN oferta_doacao o ON o.id = i.oferta_id";
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new IndicadorResumoDTO(rs.getLong("qtd"), rs.getDouble("peso"),
                    rs.getDouble("quantidade"), rs.getDouble("co2"));
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao calcular resumo de indicadores: " + e.getMessage(), e);
        }
    }

    public boolean existeParaOferta(UUID ofertaId) {
        try (Connection con = conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM indicador_impacto WHERE oferta_id = ?")) {
            ps.setObject(1, ofertaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao verificar indicador: " + e.getMessage(), e);
        }
    }

    public IndicadorImpacto buscarPorOferta(OfertaDoacao oferta) {
        String sql = "SELECT id, peso_total_kg, co2_evitado_kg, data_calculo FROM indicador_impacto WHERE oferta_id = ?";
        try (Connection con = conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, oferta.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new IndicadorImpacto(UUID.fromString(rs.getString("id")),
                        rs.getFloat("peso_total_kg"), rs.getFloat("co2_evitado_kg"),
                        new java.util.Date(rs.getTimestamp("data_calculo").getTime()), oferta);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar indicador: " + e.getMessage(), e);
        }
    }
}
