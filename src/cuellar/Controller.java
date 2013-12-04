package cuellar;

import java.awt.Color;

import application.DemoApp;

public class Controller extends application.Controller {

	public Controller(DemoApp app, int leftBoundary, int rightBoundary,
			int topBoundary, int bottomBoundary, Color color) {
		super(app, leftBoundary, rightBoundary, topBoundary, bottomBoundary,
				color);
		
		
		
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void setup() {
		DemoApp app = getApp();
		
		for (int i = 0; i < 109; i++) {
			Rabbit rabbit = new Rabbit(app, this);
			app.addCollidableAgent(rabbit);
			app.addExecutable(rabbit);
		}
		
		for (int i = 0; i < 10; i++) {
			Coyote coyote = new Coyote(app, this);
			app.addCollidableAgent(coyote);
			app.addExecutable(coyote);
		}
		
		Elites elites = new Elites(app, this);
		app.addCollidableAgent(elites);
		app.addExecutable(elites);
	}
}
