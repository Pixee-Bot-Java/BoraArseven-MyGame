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

import java.util.List;

import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.ORM.HibernateUtil;
import com.boracompany.mygame.ORM.PlayerDAOIMPL;

import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerDAOImpIT {

	// Normally I don't suppress warnings, but I think it there is a bug.
	@SuppressWarnings("resource")
	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private EntityManagerFactory emf;
	private PlayerDAOIMPL playerDAO;

	@BeforeAll
	public void setUp() {
		// Set system properties for HibernateUtil
		System.setProperty("DB_URL", postgreSQLContainer.getJdbcUrl());
		System.setProperty("DB_USERNAME", postgreSQLContainer.getUsername());
		System.setProperty("DB_PASSWORD", postgreSQLContainer.getPassword());

		emf = HibernateUtil.getEntityManagerFactory();
		playerDAO = new PlayerDAOIMPL(emf);
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

	// player class does not need to be tested since the class is model class (no logic)
	@Test
    public void testgetAllPlayers() {
        EntityManager em = emf.createEntityManager();
        
        // Create
        em.getTransaction().begin();
        Player player = new Player();
        player.setName("John Doe");
        em.persist(player);
        em.getTransaction().commit();
        em.close();
        
        // Retrieve
        List<Player> players = playerDAO.getAllPlayers();
        
        // Assert
        assertNotNull(players);
        assertFalse(players.isEmpty());
        
        boolean found = false;
        for (Player p : players) {
            if (p.getName().equals("John Doe")) {
                found = true;
                break;
            }
        }
        
        assertTrue(found, "Player John Doe should be found in the list of all players.");
    }

}
