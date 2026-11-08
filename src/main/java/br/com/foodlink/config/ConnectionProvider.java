package br.com.foodlink.config;

import java.sql.Connection;
import java.sql.SQLException;

/** Fonte de conexões (permite injetar outra implementação nos testes). */
public interface ConnectionProvider {
    Connection getConexao() throws SQLException;
}
