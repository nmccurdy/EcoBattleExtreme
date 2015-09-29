package application;

import java.awt.Color;

import starjava.Application;

public abstract class Controller {

	private DemoApp app;
	
	private int leftBoundary, rightBoundary, topBoundary, bottomBoundary;
	private Color color;
	
	public Controller(DemoApp app, int leftBoundary, int rightBoundary,
			int topBoundary, int bottomBoundary, Color color) {
		super();
		this.app = app;
		this.leftBoundary = leftBoundary;
		this.rightBoundary = rightBoundary;
		this.topBoundary = topBoundary;
		this.bottomBoundary = bottomBoundary;
		this.color = color;
	}

	public int getLeftBoundary() {
		return leftBoundary;
	}

	public void setLeftBoundary(int leftBoundary) {
		this.leftBoundary = leftBoundary;
	}

	public int getRightBoundary() {
		return rightBoundary;
	}

	public void setRightBoundary(int rightBoundary) {
		this.rightBoundary = rightBoundary;
	}

	public int getTopBoundary() {
		return topBoundary;
	}

	public void setTopBoundary(int topBoundary) {
		this.topBoundary = topBoundary;
	}

	public int getBottomBoundary() {
		return bottomBoundary;
	}

	public void setBottomBoundary(int bottomBoundary) {
		this.bottomBoundary = bottomBoundary;
	}

	public Color getColor() {
		return color;
	}

	public DemoApp getApp() {
		return app;
	}

	public abstract void setup();
	
	public abstract void execute();
}
