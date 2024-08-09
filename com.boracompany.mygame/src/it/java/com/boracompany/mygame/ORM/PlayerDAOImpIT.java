package com.boracompany.mygame.ORM;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.Spy;
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
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
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
	public void testUpdatePlayerWhenRollbackOnRuntimeException() {
	    // Use the custom TestPlayerDAOIMPL that throws the exception
	    TestPlayerDAOIMPL playerDAO = new TestPlayerDAOIMPL(emf);

	    // Create a player instance
	    Player player = new Player();
	    player.setName("Should Rollback");

	    // Act & Assert: Ensure that the exception is thrown and rollback happens
	    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
	        playerDAO.updatePlayer(player);
	    });

	    // Check that the exception message is correct
	    assertEquals("Simulated Exception", exception.getMessage());

	    // Optionally, you could verify that the player wasn't updated in the database
	    // Or check other side effects that you expect from the rollback
	}
	

	@Test
	public void testDeletePlayer() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Player player = new PlayerBuilder().withName("To Be Deleted").build();
		em.persist(player);
		em.getTransaction().commit();
		em.close();
		playerDAO.deletePlayer(player);
		Player deletedPlayer = playerDAO.getPlayer(player.getId());
		assertNull(deletedPlayer);
	}
	@Test
	public void testDeletePlayerRollbackWhenRuntimeException() {
	    // Use the custom TestPlayerDAOIMPL that throws the exception
		EntityManager em = emf.createEntityManager();
	    TestPlayerDAOIMPL playerDAO = new TestPlayerDAOIMPL(emf);
	    // Create a player instance
	    Player player = new PlayerBuilder().withName("Should Rollback").build();
	    em.getTransaction().begin();
		em.persist(player);
		em.getTransaction().commit();
	
	    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
	        playerDAO.deletePlayer(player);
	    });
	    Player managedPlayer = em.find(Player.class, player.getId());
assertNotNull(managedPlayer);
em.close();
	}
	@Test
	public void testUpdatePlayerDoesNotRollbackWhenTransactionIsNull() {
	    // Mock the EntityManager and EntityTransaction
	    EntityManager emMock = Mockito.mock(EntityManager.class);
	    EntityTransaction transactionMock = null;  // Explicitly set to null

	    // Mock EntityManagerFactory to return the mocked EntityManager
	    EntityManagerFactory emfMock = Mockito.mock(EntityManagerFactory.class);
	    Mockito.when(emfMock.createEntityManager()).thenReturn(emMock);

	    // Inject the mocked EntityManagerFactory into the PlayerDAOIMPL
	    PlayerDAOIMPL dao = new PlayerDAOIMPL(emfMock);

	    // Create a Player instance
	    Player player = new Player();
	    player.setName("Test Player");

	    // Mock the getTransaction to return null
	    Mockito.when(emMock.getTransaction()).thenReturn(transactionMock);

	    // Assert that the method throws a NullPointerException due to the null transaction
	    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
	        dao.updatePlayer(player);
	    });
	    assertEquals("Transaction is null", exception.getMessage());

	    // Verify that rollback was never called because transaction is null
	    // This is more about ensuring no rollback occurs, hence the transaction remains untouched.
	    // No verification on the transactionMock as it is null

	    Mockito.verify(emMock).close();
	}
}
