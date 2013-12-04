package torusworld;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import starjava.Agent;
import starjava.Application;
import starlogoc.StarLogo;

public class Turtles {

	private HashSet<Integer> oldLive; // the live turtles from last the frame
	// a mapping between who numbers and Mobiles
	private java.util.concurrent.ConcurrentHashMap<Integer, Mobile> turtleHash;
	private Set<Mobile> currentTurtles; // the set of live turtles on the active
										// terrain

	// private TurtleManager tm; // convienence object since tm is a part of
	// StarLogo
	private StarLogo sl;

	private int whoNumberForCamera;

	public Turtles(StarLogo sl) {
		this.sl = sl;

		turtleHash = new java.util.concurrent.ConcurrentHashMap<Integer, Mobile>();
		currentTurtles = new HashSet<Mobile>();

		whoNumberForCamera = -1;
	}

	public synchronized Iterator<Mobile> getTurtleIterator() {
		Set<Mobile> copy = ((Set) ((HashSet) currentTurtles).clone());
		return copy.iterator();// turtleHash.values().iterator();
	}

	// returns the smallest who number larger than the current one
	// (or the smallest one if this one is the largest).
	static final int WHO_INF = 65536;

	public synchronized int getNextWho(int who) {
		if (turtleHash.size() == 0)
			return -1;

		int min = WHO_INF, minbigger = WHO_INF;
		for (Integer i : turtleHash.keySet())
			if (turtleHash.get(i).shown
					&& currentTurtles.contains(turtleHash.get(i))) {
				int val = i.intValue();
				if (val < min)
					min = val;
				if (val > who && val < minbigger)
					minbigger = val;
			}
		return minbigger == WHO_INF ? min : minbigger;
	}

	public synchronized int getPrevWho(int who) {
		if (turtleHash.size() == 0)
			return -1;

		int max = -WHO_INF, maxsmaller = -WHO_INF;
		for (Integer i : turtleHash.keySet())
			if (turtleHash.get(i).shown
					&& currentTurtles.contains(turtleHash.get(i))) {
				int val = i.intValue();
				if (val > max)
					max = val;
				if (val < who && val > maxsmaller)
					maxsmaller = val;
			}
		return maxsmaller == -WHO_INF ? max : maxsmaller;
	}

	public synchronized int size() {
		return turtleHash.size();
	}

	public synchronized int numTurtles() {
		return turtleHash.size();
	}

	public synchronized boolean isInTurtleHash(int who) {
		return turtleHash.containsKey(new Integer(who));
	}

	public synchronized Mobile getTurtleWho(int who) {
		return turtleHash.get(new Integer(who));
	}

	public int whoNumberForCamera() {
		return whoNumberForCamera;
	}

	public synchronized void addTurtle(Agent agent) {
		Mobile mobile = new Mobile(agent.getWho(), agent);
		mobile.initFromVM();
		turtleHash.put(mobile.who, mobile);
		currentTurtles.add(mobile);
	}

	public synchronized void removeTurtle(Agent agent) {
		int who = agent.getWho();
		Mobile mobile = turtleHash.get(who);
		if (mobile != null) {
			turtleHash.remove(mobile.who);
			currentTurtles.remove(mobile);
		}
	}

	public synchronized void updateLiveTurtles(boolean init) {
		for (Mobile mobile : currentTurtles) {
			if (init) {
				mobile.initFromVM();
			} else {
				mobile.updateFromVM();
			}
		}
	}
}
