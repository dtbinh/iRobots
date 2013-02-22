package irobots.behaviour;

import irobots.vision.ColorSensor;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.subsumption.Behavior;

public class LineBehaviour implements Behavior {
	
	private Navigator nav;
	private ColorSensor color;
	
	public LineBehaviour(Navigator nav, ColorSensor color) {
		this.nav = nav;
		this.color = color;
	}

	@Override
	public boolean takeControl() {
		return color.lineDetected();
	}

	/**
	 * Turns the robot 180° and then passes control to other behaviours
	 */
	@Override
	public void action() {
		Pose p = nav.getPoseProvider().getPose();
		p.rotateUpdate(180);
		nav.clearPath();
		nav.goTo(p.getX(), p.getY(), p.getHeading());
	}

	/**
	 * Not going out of the line boundaries has the highest priority
	 * and cannot be supressed so supress does nothing.
	 */
	@Override
	public void suppress() {
		return;
	}
	
}
