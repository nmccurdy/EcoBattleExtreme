package neilChallengeHard;

import java.awt.Color;

import application.DemoApp;

public class Controller extends application.Controller {

	private int numDogs = 10;

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

//		for (int i = 0; i < 100; i++) {
//			Rabbit rabbit = new Rabbit(app, this);
//			app.addCollidableAgent(rabbit);
//			app.addExecutable(rabbit);
//		}
//
//		for (int i = 0; i < 20; i++) {
//			Coyote coyote = new Coyote(app, this);
//			app.addCollidableAgent(coyote);
//			app.addExecutable(coyote);
//		}

		// create one queen

		
		
		for (int y = 0; y < 10; y++) {
			for (int x = 0; x < 4; x++) {
				QueenRabbit queen = new QueenRabbit(app, this, numDogs, -45 + x * 10,
						-45 + y * 10);
				app.addCollidableAgent(queen);
				app.addExecutable(queen);

				for (int dogNum = 0; dogNum < numDogs; dogNum++) {

					GuardDog dog = new GuardDog(app, this, queen, dogNum);
					app.addCollidableAgent(dog);
					app.addExecutable(dog);

				}
			}
		}
		// create guard dogs surrounding the queen
		//
		// double initialHeading;
		// if (leftSide) {
		// initialHeading = 270;
		// } else {
		// initialHeading = 270;
		// }

	}

	public int getNumDogs() {
		return numDogs;

	}
}
