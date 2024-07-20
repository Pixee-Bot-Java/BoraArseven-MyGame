package com.boracompany.mygame;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.ORM.HibernateUtil;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserTransactionIT {

	// Normally I don't suppress warnings, but I think it there is a bug.
	@SuppressWarnings("resource")
	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private EntityManagerFactory emf;

	@BeforeAll
	public void setUp() {
		// Set system properties for HibernateUtil
		System.setProperty("DB_URL", postgreSQLContainer.getJdbcUrl());
		System.setProperty("DB_USERNAME", postgreSQLContainer.getUsername());
		System.setProperty("DB_PASSWORD", postgreSQLContainer.getPassword());

		emf = HibernateUtil.getEntityManagerFactory();
	}

	@AfterAll
	public void tearDown() {
		if (emf != null) {
			emf.close();
		}
		if (postgreSQLContainer != null) {
			postgreSQLContainer.stop();
		}
	}

	@Test
	public void testCreateReadUpdateDelete() {
		EntityManager em = emf.createEntityManager();
		// Create
		em.getTransaction().begin();
		Player player = new Player();
		player.setName("John Doe");
		em.persist(player);
		em.getTransaction().commit();

	}

}
