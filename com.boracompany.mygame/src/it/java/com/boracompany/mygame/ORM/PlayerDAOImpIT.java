package com.boracompany.mygame.ORM;

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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.Model.PlayerBuilder;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerDAOImpIT {

	@Container
	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private EntityManagerFactory emf;
	private PlayerDAOIMPL playerDAO;

	@BeforeAll
	void setUp() {
		// Directly passing database properties
		String dbUrl = postgreSQLContainer.getJdbcUrl();
		String dbUser = postgreSQLContainer.getUsername();
		String dbPassword = postgreSQLContainer.getPassword();

		// Initialize HibernateUtil with connection properties
		HibernateUtil.initialize(dbUrl, dbUser, dbPassword);
		emf = HibernateUtil.getEntityManagerFactory();
		playerDAO = new PlayerDAOIMPL(emf);
	}

	@AfterAll
	void tearDown() {
		HibernateUtil.close();
		if (postgreSQLContainer != null) {
			postgreSQLContainer.stop();
		}
	}

	@Test
	void testGetAllPlayers() {
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
	void testGetPlayer() {
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
	void testUpdatePlayer() {
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
	void testUpdatePlayerWhenRollbackOnRuntimeException() {
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
		assertEquals("Failed to update player due to an unexpected error.", exception.getMessage());

	}

	@Test
	void testUpdatePlayerTriggersRollbackOnRuntimeException() {
		// Mock the EntityManager and EntityTransaction
		EntityManager emMock = Mockito.mock(EntityManager.class);
		EntityTransaction transactionMock = Mockito.mock(EntityTransaction.class);

		// Mock EntityManagerFactory to return the mocked EntityManager
		EntityManagerFactory emfMock = Mockito.mock(EntityManagerFactory.class);
		Mockito.when(emfMock.createEntityManager()).thenReturn(emMock);

		// Ensure the EntityManager returns a non-null transaction
		Mockito.when(emMock.getTransaction()).thenReturn(transactionMock);

		// Ensure the transaction is active
		Mockito.when(transactionMock.isActive()).thenReturn(true);

		// Inject the mocked EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(emfMock);

		// Create a Player instance
		Player player = new Player();
		player.setName("Test Player");

		// Simulate a RuntimeException during the merge operation
		Mockito.doThrow(new RuntimeException("Failed to update player due to an unexpected error.")).when(emMock)
				.merge(Mockito.any(Player.class));

		// Assert that the RuntimeException is thrown when updatePlayer is called
		assertThrows(RuntimeException.class, () -> {
			dao.updatePlayer(player);
		});

		// Verify that rollback was called on the transaction
		Mockito.verify(transactionMock).rollback();

		// Verify that the EntityManager was closed
		Mockito.verify(emMock).close();
	}

	@Test
	void testUpdatePlayerDoesNotRollbackWhenTransactionIsNull() {
		// Spy the real EntityManagerFactory
		EntityManagerFactory emfSpy = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(emfSpy.createEntityManager());

		// Create a spy of the real EntityTransaction, but do not attach it yet
		// We will simulate a null transaction in the next step

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(emfSpy.createEntityManager()).thenReturn(emSpy);

		// Simulate the EntityManager returning a null transaction
		Mockito.when(emSpy.getTransaction()).thenReturn(null);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(emfSpy);

		// Create a Player instance
		Player player = new Player();
		player.setName("Test Player");

		// Act & Assert: Ensure that an IllegalStateException is thrown due to the null
		// transaction
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			dao.updatePlayer(player);
		});
		assertEquals("Transaction is null", exception.getMessage());

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

	@Test
	void testUpdatePlayerCommitsTransactionSuccessfully() {
		// Spy the real EntityManagerFactory
		EntityManagerFactory emfSpy = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(emfSpy.createEntityManager());

		// Create a spy of the real EntityTransaction
		EntityTransaction transactionSpy = Mockito.spy(emSpy.getTransaction());

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(emfSpy.createEntityManager()).thenReturn(emSpy);

		// Ensure the spied EntityManager returns the spied transaction
		Mockito.when(emSpy.getTransaction()).thenReturn(transactionSpy);

		// Ensure the transaction is not active at the start of the test
		Mockito.when(transactionSpy.isActive()).thenReturn(false);

		// Create a Player instance
		Player player = new Player();
		player.setName("Test Player");
		player.setId(1L); // Assuming Player has an ID field

		// Mock the find method to return the player
		Mockito.when(emSpy.find(Player.class, player.getId())).thenReturn(player);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(emfSpy);

		// Call the method to update the player
		dao.updatePlayer(player);

		// Verify that the find method was called
		Mockito.verify(emSpy).find(Player.class, player.getId());

		// Verify that the transaction was begun and committed
		Mockito.verify(transactionSpy).begin();
		Mockito.verify(transactionSpy).commit();

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

	@Test
	public void testUpdatePlayerWhenTransactionIsNotActive() {
		// Spy the real EntityManagerFactory
		EntityManagerFactory emfSpy = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(emfSpy.createEntityManager());

		// Create a spy of the real EntityTransaction
		EntityTransaction transactionSpy = Mockito.spy(emSpy.getTransaction());

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(emfSpy.createEntityManager()).thenReturn(emSpy);

		// Ensure the spied EntityManager returns the spied transaction
		Mockito.when(emSpy.getTransaction()).thenReturn(transactionSpy);

		// Simulate a non-active transaction
		Mockito.when(transactionSpy.isActive()).thenReturn(false);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(emfSpy);

		// Create a Player instance
		Player player = new Player();
		player.setName("Test Player");

		// Simulate a RuntimeException during the merge operation
		Mockito.doThrow(new RuntimeException("Failed to update player due to an unexpected error.")).when(emSpy)
				.merge(Mockito.any(Player.class));

		// Act & Assert: Ensure that the RuntimeException is thrown
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			dao.updatePlayer(player);
		});
		assertEquals("Failed to update player due to an unexpected error.", exception.getMessage());

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
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

		assertThrows(RuntimeException.class, () -> {
			playerDAO.deletePlayer(player);
		});
		Player managedPlayer = em.find(Player.class, player.getId());
		assertNotNull(managedPlayer);
		em.close();
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
	public void testDeletePlayerDoesNotRollbackWhenTransactionIsNull() {
		// Create a spy of the real EntityManagerFactory
		EntityManagerFactory spiedEmf = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(spiedEmf.createEntityManager());

		// Create a spy of the real EntityTransaction
		EntityTransaction transactionSpy = Mockito.spy(emSpy.getTransaction());

		// Ensure the spied EntityManager returns the spied transaction
		Mockito.when(emSpy.getTransaction()).thenReturn(transactionSpy);

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(spiedEmf.createEntityManager()).thenReturn(emSpy);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(spiedEmf);

		// Create and persist a Player instance using the actual database
		Player player = new PlayerBuilder().withName("Test Player").build();
		emSpy.getTransaction().begin();
		emSpy.persist(player);
		emSpy.getTransaction().commit();

		// Ensure the player was persisted by finding it
		Player persistedPlayer = emSpy.find(Player.class, player.getId());
		assertNotNull(persistedPlayer);

		// Now simulate the scenario where transaction is null
		Mockito.when(emSpy.getTransaction()).thenReturn(null);

		// Attempt to delete the player
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			dao.deletePlayer(player);
		});
		assertEquals("Transaction is null", exception.getMessage());

		// Verify that rollback was never called because transaction is null
		Mockito.verify(transactionSpy, Mockito.never()).rollback();

		// Verify that the remove operation was never called due to the null transaction
		Mockito.verify(emSpy, Mockito.never()).remove(Mockito.any(Player.class));

		// No need to find the player again here because the EntityManager might be
		// closed already.

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

	@Test
	public void testDeletePlayerTriggersRollbackOnRuntimeException() {
		// Create a spy of the real EntityManagerFactory
		EntityManagerFactory spiedEmf = Mockito.spy(emf);

		// Spy on the real EntityManagerdo
		EntityManager emSpy = Mockito.spy(spiedEmf.createEntityManager());

		// Spy on the EntityTransaction to observe its behavior
		EntityTransaction transactionSpy = Mockito.spy(emSpy.getTransaction());

		// Make the spied EntityManager return the spied transaction
		Mockito.when(emSpy.getTransaction()).thenReturn(transactionSpy);

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(spiedEmf.createEntityManager()).thenReturn(emSpy);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL playerDAOwithspiedEmf = new PlayerDAOIMPL(spiedEmf);

		// Create a player instance
		Player player = new Player();
		player.setName("Test Player");
		player.setId(1L); // Assuming an ID is needed

		// Simulate finding the player
		Mockito.when(emSpy.find(Player.class, player.getId())).thenReturn(player);

		// Simulate a RuntimeException during the remove operation
		Mockito.doThrow(new RuntimeException("Simulated Exception")).when(emSpy).remove(Mockito.any(Player.class));

		// Act & Assert: Ensure that the exception is thrown and rollback happens
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			playerDAOwithspiedEmf.deletePlayer(player);
		});

		// Check that the exception message is correct
		assertEquals("Simulated Exception", exception.getMessage());

		// Verify that rollback was called on the transaction
		Mockito.verify(transactionSpy).rollback();

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

	@Test
	public void testDeletePlayerThrowsExceptionForNonExistingPlayer() {
		// Create a spy of the real EntityManagerFactory
		EntityManagerFactory spiedEmf = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(spiedEmf.createEntityManager());

		// Create a spy of the real EntityTransaction
		EntityTransaction transactionSpy = Mockito.spy(emSpy.getTransaction());

		// Ensure the spied EntityManager returns the spied transaction
		Mockito.when(emSpy.getTransaction()).thenReturn(transactionSpy);

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(spiedEmf.createEntityManager()).thenReturn(emSpy);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(spiedEmf);

		// Create a Player instance
		Player player = new Player();
		player.setId(1L); // Assuming an ID is needed

		// Simulate that the player does not exist in the database
		Mockito.when(emSpy.find(Player.class, player.getId())).thenReturn(null);

		// Attempt to delete the player and expect an IllegalStateException
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			dao.deletePlayer(player);
		});

		// Verify the exception message
		assertEquals("Tried to delete non existing player", exception.getMessage());

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

	@Test
	public void testDeletePlayerThrowsExceptionWhenTransactionIsNull() {
		// Create a spy of the real EntityManagerFactory
		EntityManagerFactory spiedEmf = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(spiedEmf.createEntityManager());

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(spiedEmf.createEntityManager()).thenReturn(emSpy);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL dao = new PlayerDAOIMPL(spiedEmf);

		// Create a Player instance
		Player player = new Player();
		player.setId(1L); // Assuming an ID is needed

		// Simulate finding the player (assuming the player exists)
		Mockito.when(emSpy.find(Player.class, player.getId())).thenReturn(player);

		// Simulate the scenario where transaction is null
		Mockito.when(emSpy.getTransaction()).thenReturn(null);

		// Attempt to delete the player and expect an IllegalStateException
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			dao.deletePlayer(player);
		});

		// Verify the exception message
		assertEquals("Transaction is null", exception.getMessage());

		// Verify that the remove operation was never called due to the null transaction
		Mockito.verify(emSpy, Mockito.never()).remove(Mockito.any(Player.class));

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

	@Test
	public void testDeletePlayerTriggersRollbackWhenTransactionIsActive() {
		// Create a spy of the real EntityManagerFactory
		EntityManagerFactory spiedEmf = Mockito.spy(emf);

		// Create a spy of the real EntityManager
		EntityManager emSpy = Mockito.spy(spiedEmf.createEntityManager());

		// Create a spy of the real EntityTransaction
		EntityTransaction transactionSpy = Mockito.spy(emSpy.getTransaction());

		// Ensure the spied EntityManagerFactory returns the spied EntityManager
		Mockito.when(spiedEmf.createEntityManager()).thenReturn(emSpy);
		Mockito.when(emSpy.getTransaction()).thenReturn(transactionSpy);

		// Inject the spied EntityManagerFactory into the PlayerDAOIMPL
		PlayerDAOIMPL playerDAO = new PlayerDAOIMPL(spiedEmf);

		// Create and persist a Player instance using the actual database
		Player player = new PlayerBuilder().withName("Test Player").build();

		// Persist the player to the database
		emSpy.getTransaction().begin();
		emSpy.persist(player);
		emSpy.getTransaction().commit();

		// Ensure the player was actually persisted by finding it
		Player persistedPlayer = emSpy.find(Player.class, player.getId());
		assertNotNull(persistedPlayer);

		// Now, simulate the scenario where an exception is thrown during deletion
		Mockito.when(transactionSpy.isActive()).thenReturn(true);

		// Simulate a RuntimeException during the remove operation
		Mockito.doThrow(new RuntimeException("Simulated Exception")).when(emSpy).remove(Mockito.any(Player.class));

		// Act & Assert: Ensure that the exception is thrown and rollback happens
		assertThrows(RuntimeException.class, () -> {
			playerDAO.deletePlayer(player);
		});

		// Verify that rollback was called on the transaction
		Mockito.verify(transactionSpy).rollback();

		// Verify that the EntityManager was closed
		Mockito.verify(emSpy).close();
	}

}
