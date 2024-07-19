package com.boracompany.mygame.ORM;

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

import com.boracompany.mygame.Model.Player;

public class HibernateUtil {

	private static final EntityManagerFactory entityManagerFactory;

	static {
		// Load system properties
		Map<String, Object> properties = new HashMap<>();
		String dbUrl = System.getProperty("DB_URL");
		String dbUser = System.getProperty("DB_USERNAME");
		String dbPassword = System.getProperty("DB_PASSWORD");

		if (dbUrl == null || dbUser == null || dbPassword == null) {
			throw new RuntimeException("Database connection properties not set");
		}

		// Extract the database name from the URL
		String cleanDbUrl = dbUrl.split("\\?")[0]; // Remove query parameters
		String databaseName = cleanDbUrl.substring(cleanDbUrl.lastIndexOf('/') + 1);
		String baseDbUrl = cleanDbUrl.substring(0, cleanDbUrl.lastIndexOf('/')) + "/postgres";

		// Check and create database if it doesn't exist
		createDatabaseIfNotExists(baseDbUrl, dbUser, dbPassword, databaseName);

		properties.put(AvailableSettings.URL, dbUrl);
		properties.put(AvailableSettings.USER, dbUser);
		properties.put(AvailableSettings.PASS, dbPassword);
		properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
		properties.put(AvailableSettings.HBM2DDL_AUTO, "update");
		properties.put(AvailableSettings.SHOW_SQL, "true");

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
		return new HibernatePersistenceUnitInfo("com.boracompany.mygame", Player.class);
	}

	private static void createDatabaseIfNotExists(String baseDbUrl, String user, String password, String databaseName) {
		try (Connection conn = DriverManager.getConnection(baseDbUrl, user, password)) {
			if (!databaseExists(conn, databaseName)) {
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
