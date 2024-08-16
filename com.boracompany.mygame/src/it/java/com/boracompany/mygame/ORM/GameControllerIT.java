package com.boracompany.mygame.ORM;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.boracompany.mygame.Controller.GameController;
import com.boracompany.mygame.Model.GameMap;
import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.Model.PlayerBuilder;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameControllerIT {

	private static final Logger LOGGER = LogManager.getLogger(GameControllerIT.class);

	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = extracted().withDatabaseName("test").withUsername("test")
			.withPassword("test");

	private static PostgreSQLContainer<?> extracted() {
		return new PostgreSQLContainer<>("postgres:13.3");
	}

	private EntityManagerFactory emf;
	private GameMapDAO gameMapDAO;
	private PlayerDAOIMPL playerDAO;

	private GameController controller;
	private PlayerBuilder playerBuilder;

	@BeforeAll
	void setUpAll() {
		postgreSQLContainer.start();

		// Directly passing database properties
		String dbUrl = postgreSQLContainer.getJdbcUrl();
		String dbUser = postgreSQLContainer.getUsername();
		String dbPassword = postgreSQLContainer.getPassword();

		// Initialize HibernateUtil with connection properties
		HibernateUtil.initialize(dbUrl, dbUser, dbPassword);
		emf = HibernateUtil.getEntityManagerFactory();
	}

	@BeforeEach
	void setUp() {

		// Initialize DAOs with the EntityManagerFactory
		gameMapDAO = new GameMapDAO(emf);
		playerDAO = new PlayerDAOIMPL(emf);

		// Initialize the PlayerBuilder
		playerBuilder = new PlayerBuilder();

		// Spy on the GameController
		controller = spy(new GameController(playerDAO, gameMapDAO, LOGGER));

		// Reset database before each test
		resetDatabase();
	}

	
	@AfterAll
	static void tearDownAll() {
		HibernateUtil.close();
		if (postgreSQLContainer != null) {
			postgreSQLContainer.stop();
		}
	}

	void resetDatabase() {
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		try {
			transaction.begin();
			// Delete all data from tables
			em.createQuery("DELETE FROM Player").executeUpdate();
			em.createQuery("DELETE FROM GameMap").executeUpdate();
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw new RuntimeException("Failed to reset database", e);
		} finally {
			em.close();
		}
	}

	@Test
	void testAddPlayersToMapFromController() {
		// Arrange: Create a new player and a game map
		Player addedPlayer = playerBuilder.withDamage(10).withHealth(20).withName("AddedPlayer1").build();
		GameMap map = new GameMap();
		map.setName("TestMap");

		// Persist the map in the database
		gameMapDAO.save(map);

		// Act: Add the player to the map using the controller
		controller.addPlayerToMap(map.getId(), addedPlayer);

		// Assert: Access the players collection within an active transaction
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			GameMap retrievedMap = gameMapDAO.findById(map.getId());
			assertNotNull(retrievedMap);

			// Access the players collection while the session is still open
			assertFalse(retrievedMap.getPlayers().isEmpty());
			assertEquals("AddedPlayer1", retrievedMap.getPlayers().get(0).getName());
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}

		LOGGER.info("Player {} successfully added to map {}", addedPlayer.getName(), map.getName());
	}

//	@Test
//	void testRemovePlayerFromMap_PlayerExistsInMap() {
//		// Arrange: Create a new player and a game map
//		Player playerToRemove = playerBuilder.resetBuilder().withDamage(10).withHealth(20).withName("PlayerToRemove")
//				.build();
//
//		GameMap map = new GameMap();
//		map.setName("TestMap");
//
//		// Persist the map and the player in the database
//		gameMapDAO.save(map);
//
//		// After persisting, the ID should be automatically generated and assigned to
//		// the map
//		Long generatedMapId = map.getId(); // Retrieve the newly generated ID
//
//		// Ensure that the map has been assigned an ID
//		assertNotNull(generatedMapId, "Map ID should not be null after persisting");
//
//		playerDAO.updatePlayer(playerToRemove);
//
//		// Add the player to the map using the controller
//		controller.addPlayerToMap(generatedMapId, playerToRemove);
//
//		// Act: Remove the player from the map using the controller
//		controller.removePlayerFromMap(generatedMapId, playerToRemove);
//
//		// Assert: Access the players collection within an active transaction to check
//		// removal
//		EntityManager em = emf.createEntityManager();
//		EntityTransaction transaction = em.getTransaction();
//
//		try {
//			transaction.begin();
//			GameMap retrievedMap = gameMapDAO.findById(generatedMapId);
//			assertNotNull(retrievedMap, "Retrieved map should not be null");
//
//			// Access the players collection while the session is still open
//			assertTrue(retrievedMap.getPlayers().isEmpty(), "Player was not successfully removed from the map");
//			transaction.commit();
//		} catch (Exception e) {
//			if (transaction.isActive()) {
//				transaction.rollback();
//			}
//			throw e;
//		} finally {
//			em.close();
//		}
//
//		LOGGER.info("Player {} successfully removed from map {}", playerToRemove.getName(), map.getName());
//	}

}
