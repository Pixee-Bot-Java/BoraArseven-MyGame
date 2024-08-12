package com.boracompany.mygame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boracompany.mygame.Controller.GameController;
import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.Model.PlayerBuilder;

class GameControllerTest {
    private static final Logger LOGGER = LogManager.getLogger(GameControllerTest.class);
    GameController controller;

    // before each test, controller will be reset
    @BeforeEach
    void Setup() {
        controller = new GameController();
    }

    @Test
    void testWhenAttackingDefendingPlayerisNullThrowsException() {
        PlayerBuilder builder = new PlayerBuilder();
        Player attacker = builder.withDamage(10).withName("Attacker").withHealth(30).build();
        Player defender = null;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.attack(attacker, defender);
        });
        assertEquals("Attacker or defender is not valid", exception.getMessage());
    }

    @Test
    void testwhenAttackingAttackingPLayerisNullThrowsException() {
        PlayerBuilder builder = new PlayerBuilder();
        Player attacker = null;
        Player defender = builder.withDamage(10).withName("Defender").withHealth(30).build();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.attack(attacker, defender);
        });
        assertEquals("Attacker or defender is not valid", exception.getMessage());
    }

    @Test
    void testWhenAttackingBothPLayersareNullThrowsException() {
        Player attacker = null;
        Player defender = null;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.attack(attacker, defender);
        });
        assertEquals("Attacker or defender is not valid", exception.getMessage());
    }

    @Test
    void AttackerReducesHealthOfDefender() {
        PlayerBuilder attackerBuilder = new PlayerBuilder();
        PlayerBuilder defenderBuilder = new PlayerBuilder();

        Player attacker = attackerBuilder.withDamage(10).withName("Attacker").withHealth(30).build();
        Player defender = defenderBuilder.withDamage(10).withName("Defender").withHealth(30).build();

        controller.attack(attacker, defender);

        attacker.setDamage(5);
        controller.attack(attacker, defender);

        assertEquals(15, defender.getHealth());
    }

    @Test
    void AttackerReducesHealthOfDefenderNotMinus() {
        PlayerBuilder attackerBuilder = new PlayerBuilder();
        PlayerBuilder defenderBuilder = new PlayerBuilder();

        Player attacker = attackerBuilder.withDamage(10).withName("Attacker").withHealth(30).build();
        Player defender = defenderBuilder.withDamage(10).withName("Defender").withHealth(10).build();

        controller.attack(attacker, defender);

        attacker.setDamage(5);
        controller.attack(attacker, defender);

        assertEquals(0, defender.getHealth());
    }

    @Test
    void DefenderDiesIfHealthsmallerthanzero() {
        PlayerBuilder attackerBuilder = new PlayerBuilder();
        PlayerBuilder defenderBuilder = new PlayerBuilder();

        Player attacker = attackerBuilder.withDamage(10).withName("Attacker").withHealth(30).build();
        Player defender = defenderBuilder.withDamage(10).withName("Defender").withHealth(10).build();

        controller.attack(attacker, defender);

        attacker.setDamage(5);
        controller.attack(attacker, defender);

        assertEquals(0, defender.getHealth());
        assertEquals(false, defender.Isalive());
    }

    @Test
    void DefenderNotDiesIfHealthbiggerthanzero() {
        PlayerBuilder attackerBuilder = new PlayerBuilder();
        PlayerBuilder defenderBuilder = new PlayerBuilder();

        Player attacker = attackerBuilder.withDamage(5).withName("Attacker").withHealth(30).build();
        Player defender = defenderBuilder.withDamage(10).withName("Defender").withHealth(50).build();

        LOGGER.debug("Attacker created with damage: {}", attacker.getDamage());

        controller.attack(attacker, defender);

        LOGGER.debug("Defender's health after first attack: {}", defender.getHealth());

        attacker.setDamage(15);

        LOGGER.debug("Attacker's damage updated to: {}", attacker.getDamage());

        controller.attack(attacker, defender);

        LOGGER.debug("Defender's health after second attack: {}", defender.getHealth());

        assertEquals(30, defender.getHealth());
        assertEquals(true, defender.Isalive());
    }
}
