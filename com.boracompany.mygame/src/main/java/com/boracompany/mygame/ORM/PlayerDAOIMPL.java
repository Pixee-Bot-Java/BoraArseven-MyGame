package com.boracompany.mygame.ORM;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import com.boracompany.mygame.Model.Player;

public class PlayerDAOIMPL implements PlayerDAO {

    private EntityManagerFactory emf;

    public PlayerDAOIMPL(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Player> getAllPlayers() {
        EntityManager em = emf.createEntityManager();
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
        EntityManager em = emf.createEntityManager();
        Player player = null;
        try {
            player = em.find(Player.class, ID);
        } finally {
            em.close();
        }
        return player;
    }

    @Override
    public void updatePlayer(Player player) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(player);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deletePlayer(Player player) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            Player managedPlayer = em.find(Player.class, player.getId());
            if (managedPlayer != null) {
                em.remove(managedPlayer);
            }
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}