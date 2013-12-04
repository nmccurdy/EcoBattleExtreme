package miller;

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

		for (int i = 0; i < 100; i++) {
			Rabbit rabbit = new Rabbit(app, this);
			app.addCollidableAgent(rabbit);
			app.addExecutable(rabbit);
		}
		
//		for (int i = 0; i < 6; i++) {
//			Coyote coyote1 = new Coyote(app, this);
//			app.addCollidableAgent(coyote1);
//			app.addExecutable(coyote1);
//		}

		for (int i = 0; i < 7; i++) {
			MotherCoyote coyote2 = new MotherCoyote(app, this);
			app.addCollidableAgent(coyote2);
			app.addExecutable(coyote2);
		}
	}
}
