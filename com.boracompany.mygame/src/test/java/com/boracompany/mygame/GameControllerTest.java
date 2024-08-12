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

// before each test, controller will be resetted.
	@BeforeEach
	void Setup() {
		controller = new GameController();
	}

	@Test
	void testDefendingPlayerisNullThrowsException() {
		Player attacker = new PlayerBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = null;
		
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker,defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	}

}
