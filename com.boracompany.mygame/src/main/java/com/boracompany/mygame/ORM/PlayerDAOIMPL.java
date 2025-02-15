package com.boracompany.mygame.ORM;

import java.util.List;

import javax.annotation.Generated;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.boracompany.mygame.Model.Player;

public class PlayerDAOIMPL implements PlayerDAO {
	
	private static final Logger LOGGER = LogManager.getLogger(PlayerDAOIMPL.class);
	private EntityManagerFactory emf;
	
	private EntityManager em;
	


	@Generated("exclude-from-coverage")
	public EntityManagerFactory getEmf() {
		return emf;
	}


	public PlayerDAOIMPL(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@Override
	public List<Player> getAllPlayers() {
		em = emf.createEntityManager();
		List<Player> players = null;
		try {
			TypedQuery<Player> query = em.createQuery("SELECT p FROM Player p", Player.class);
			players = query.getResultList();
		} finally {
			em.close();
		}
		return players;
	}

	@Override
	public Player getPlayer(Long ID) {
		em = emf.createEntityManager();
		Player player = null;
		try {
			player = em.find(Player.class, ID);
		} finally {
			em.close();
		}
		return player;
	}

	@Override
	public void updatePlayer(Player player) throws IllegalStateException {
		em = emf.createEntityManager();
		EntityTransaction transaction = null;
		try {
			transaction = em.getTransaction();
			if ((transaction != null)) {
				transaction.begin();
				em.merge(player);
				transaction.commit();
			}
			// Handled NullpointerException thanks to jacoco's guidance.
			else {
				throw new IllegalStateException("Transaction is null");
			}
		} catch (RuntimeException e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			 LOGGER.error("An error occurred while trying to update player with ID {}: {}", player.getId(), e.getMessage(), e);
			throw e;
		}

		finally {
			em.close();
		}
	}

	@Override
	public void deletePlayer(Player player) {
		em = emf.createEntityManager();
		EntityTransaction transaction = null;
		try {
			transaction = em.getTransaction();
			if (transaction != null) {
				transaction.begin();
				Player managedPlayer = em.find(Player.class, player.getId());
				if (managedPlayer != null) {
					em.remove(managedPlayer);
					transaction.commit();
				} else {
					throw new IllegalStateException("Tried to delete non existing player");
				}
			}else {
				throw new IllegalStateException("Transaction is null");
			}
			
		} catch (RuntimeException e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			 LOGGER.error("An error occurred while trying to delete player with ID {}: {}", player.getId(), e.getMessage(), e);
			throw e;
		} finally {
			em.close();
		}
	}
	
}