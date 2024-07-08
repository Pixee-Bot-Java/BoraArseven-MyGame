package com.boracompany.mygame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;

public class HibernateUtil {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        // Load environment variables or configure manually
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.URL, "jdbc:postgresql://postgresdb:5432/" + System.getenv("POSTGRES_DB"));
        properties.put(AvailableSettings.USER, System.getenv("POSTGRES_USER"));
        properties.put(AvailableSettings.PASS, System.getenv("POSTGRES_PASSWORD"));
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(AvailableSettings.HBM2DDL_AUTO, "update");
        properties.put(AvailableSettings.SHOW_SQL, "true");

        // Check and create database if it doesn't exist
        createDatabaseIfNotExists(properties);

        entityManagerFactory = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(createPersistenceUnitInfo(), properties);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public static void close() {
        entityManagerFactory.close();
    }

    private static PersistenceUnitInfo createPersistenceUnitInfo() {
        return new HibernatePersistenceUnitInfo("com.boracompany.mygame", User.class);
    }

    private static void createDatabaseIfNotExists(Map<String, Object> properties) {
        String url = (String) properties.get(AvailableSettings.URL);
        String user = (String) properties.get(AvailableSettings.USER);
        String password = (String) properties.get(AvailableSettings.PASS);
        String databaseName = url.substring(url.lastIndexOf('/') + 1);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Check if the database exists
            if (!databaseExists(conn, databaseName)) {
                // Create the database if it doesn't exist
                createDatabase(conn, databaseName);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database: " + databaseName, e);
        }
    }

    private static boolean databaseExists(Connection conn, String databaseName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'");
            return stmt.getResultSet().next();
        }
    }

    private static void createDatabase(Connection conn, String databaseName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE " + databaseName);
        }
    }
}