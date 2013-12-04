/**
 * 
 */
package cuellar;


import starjava.Agent;
import application.DemoApp;

/**
 * @author kenneth.cuellar
 *
 */
public class Elites extends Coyote {
  
	/**
	 * 
	 */
	public Elites(DemoApp app, Controller controller) {
		super(app, controller);

		setSize(5);

	}

	protected void move() {

		setHeightAboveTerrain(0);
	
        if (!fightOrFlight()) {
			if (!huntEnemyRabbits()) {
				if (shouldLookForFood()) {
			   if (!huntRabbits()) {
				
				  
						// walk around randomly since the coyote can't smell any
						// food.
						left(Math.random() * 30);
						right(Math.random() * 30);
						forward(5);
					}
				}
		    }
		}		
	}
}

	
	
