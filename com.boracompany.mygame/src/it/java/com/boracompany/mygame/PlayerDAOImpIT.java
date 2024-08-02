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

    @SuppressWarnings("resource")
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withDatabaseName("test").withUsername("test").withPassword("test");

    // this emf will also be given in DAOImplementation class.
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

    // no need to test for players with various stats, when em persists, it handles that. I only tested if I am able to use it. Thus, I don't want to test this 3rd party library.
    @Test
    public void testGetAllPlayers() {
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

    @Test
    public void testGetPlayer() {
        EntityManager em = emf.createEntityManager();
        
        // Create
        em.getTransaction().begin();
        Player player = new Player();
        player.setName("Jane Doe");
        em.persist(player);
        em.getTransaction().commit();
        em.close();
        
        // Retrieve
        Player retrievedPlayer = playerDAO.getPlayer(player.getId());
        
        // Assert
        assertNotNull(retrievedPlayer);
        assertEquals("Jane Doe", retrievedPlayer.getName());
    }

    @Test
    public void testUpdatePlayer() {
        EntityManager em = emf.createEntityManager();
        
        // Create
        em.getTransaction().begin();
        Player player = new Player();
        player.setName("Initial Name");
        em.persist(player);
        em.getTransaction().commit();
        em.close();
        
        // Update
        player.setName("Updated Name");
        playerDAO.updatePlayer(player);
        
        // Retrieve
        Player updatedPlayer = playerDAO.getPlayer(player.getId());
        
        // Assert
        assertNotNull(updatedPlayer);
        assertEquals("Updated Name", updatedPlayer.getName());
    }

    @Test
    public void testDeletePlayer() {
        EntityManager em = emf.createEntityManager();
        
        // Create
        em.getTransaction().begin();
        Player player = new Player();
        player.setName("To Be Deleted");
        em.persist(player);
        em.getTransaction().commit();
        em.close();
        
        // Delete
        playerDAO.deletePlayer(player);
        
        // Retrieve
        Player deletedPlayer = playerDAO.getPlayer(player.getId());
        
        // Assert
        assertNull(deletedPlayer);
    }
}
