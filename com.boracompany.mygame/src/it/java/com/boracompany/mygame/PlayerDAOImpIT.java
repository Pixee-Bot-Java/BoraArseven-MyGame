package com.boracompany.mygame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
import com.boracompany.mygame.Model.PlayerBuilder;
import com.boracompany.mygame.ORM.HibernateUtil;
import com.boracompany.mygame.ORM.PlayerDAOIMPL;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerDAOImpIT {

	
	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private EntityManagerFactory emf;
	private PlayerDAOIMPL playerDAO;

	@BeforeAll
	public void setUp() {
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

	@Test
	public void testGetAllPlayers() {
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		Player player = new Player();
		player.setName("John Doe");
		em.persist(player);
		em.getTransaction().commit();
		em.close();
		List<Player> players = playerDAO.getAllPlayers();
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

	@Test
	public void testGetPlayer() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Player player = new PlayerBuilder().withName("Jane Doe").build();
		player.setName("Jane Doe");
		em.persist(player);
		em.getTransaction().commit();
		em.close();
		Player retrievedPlayer = playerDAO.getPlayer(player.getId());
		assertNotNull(retrievedPlayer);
		assertEquals("Jane Doe", retrievedPlayer.getName());
	}

	@Test
	public void testUpdatePlayer() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Player player = new Player();
		player.setName("Initial Name");
		em.persist(player);
		em.getTransaction().commit();
		em.close();
		player.setName("Updated Name");
		playerDAO.updatePlayer(player);
		Player updatedPlayer = playerDAO.getPlayer(player.getId());
		assertNotNull(updatedPlayer);
		assertEquals("Updated Name", updatedPlayer.getName());
	}

	@Test
	public void testDeletePlayer() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Player player = new Player();
		player.setName("To Be Deleted");
		em.persist(player);
		em.getTransaction().commit();
		em.close();
		playerDAO.deletePlayer(player);
		Player deletedPlayer = playerDAO.getPlayer(player.getId());
		assertNull(deletedPlayer);
	}

}
