package com.boracompany.mygame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boracompany.mygame.Controller.GameController;
import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.Model.PlayerBuilder;

class GameControllerTest {

	GameController controller;
	PlayerBuilder builder;
// before each test, controller will be resetted.
	@BeforeEach
	void Setup() {
		controller = new GameController();
		builder = new PlayerBuilder();
	}

	@Test
	void testWhenAttackingDefendingPlayerisNullThrowsException() {
		Player attacker = builder.withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = null;
		
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker,defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	
	}
	
	@Test
	void testwhenAttackingAttackingPLayerisNullThrowsException() {
		Player attacker = null;
		Player defender =  builder.withDamage(10).withName("Defender").withHealth(30).build();
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker,defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	}
	
	@Test
	void testWhenAttackingBothPLayersareNullThrowsException() {
		Player attacker = null;
		Player defender =  null;
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker,defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	}
	@Test
	void AttackerReducesHealthOfDefender() {
		Player attacker = builder.withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender =  builder.withDamage(10).withName("Defender").withHealth(30).build();
		controller.attack(attacker, defender);
		attacker.setDamage(5);
		controller.attack(attacker, defender);
		assertEquals(15, defender.getHealth());
	}
	
}
