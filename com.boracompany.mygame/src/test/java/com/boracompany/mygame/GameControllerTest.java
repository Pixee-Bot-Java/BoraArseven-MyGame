package com.boracompany.mygame;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boracompany.mygame.Controller.GameController;

class GameControllerTest {

	GameController controller;

// before each test, controller will be resetted.
	@BeforeEach
	void Setup() {
		controller = new GameController();
	}

	@Test
	void test() {
		assertEquals(true, true);
	}

}
