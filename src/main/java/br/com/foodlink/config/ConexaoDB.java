package br.com.foodlink.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexão com o PostgreSQL.
 * Precedência: variáveis de ambiente (DB_URL, DB_USER, DB_PASSWORD) > db.properties > padrão local.
 */
public final class ConexaoDB implements ConnectionProvider {

    private static final Properties PROPS = carregar();

    private ConexaoDB() { }

    private static Properties carregar() {
        Properties p = new Properties();
        try (InputStream in = ConexaoDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception e) {
            System.err.println("Aviso: não foi possível ler db.properties (" + e.getMessage() + ")");
        }
        return p;
    }

    public static String getUrl() {
        return System.getenv().getOrDefault("DB_URL", PROPS.getProperty("db.url", "jdbc:postgresql://localhost:5432/foodlink"));
    }

    public static String getUsuario() {
        return System.getenv().getOrDefault("DB_USER", PROPS.getProperty("db.user", "postgres"));
    }

    public static String getSenha() {
        return System.getenv().getOrDefault("DB_PASSWORD", PROPS.getProperty("db.password", "postgres"));
    }

    @Override
    public Connection getConexao() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUsuario(), getSenha());
    }

    /** Instância compartilhada (padrão usado pela aplicação). */
    public static ConexaoDB instancia() {
        return Holder.INSTANCIA;
    }

    private static final class Holder {
        private static final ConexaoDB INSTANCIA = new ConexaoDB();
    }
}
