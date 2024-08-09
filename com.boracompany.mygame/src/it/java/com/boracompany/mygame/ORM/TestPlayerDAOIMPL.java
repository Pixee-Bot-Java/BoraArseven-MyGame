package com.boracompany.mygame.ORM;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import com.boracompany.mygame.Model.Player;

public class TestPlayerDAOIMPL extends PlayerDAOIMPL {

    public TestPlayerDAOIMPL(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    public void updatePlayer(Player player) {
        EntityManager em = getEmf().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            if ("Should Rollback".equals(player.getName())) {
                throw new RuntimeException("Simulated Exception");
            }
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
}