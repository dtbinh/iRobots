package irobots.vision;

import java.awt.Rectangle;
import java.util.ArrayList;

import lejos.nxt.SensorPort;
import lejos.nxt.addon.NXTCam;

/**
 * Abstracts the access to the NXTCam and provides utility functions to
 * read the objects detected by the camera.
 */
public class Camera {

	private NXTCam cam;
	
	/**
	 * The field of view in degrees of the NXTCam.
	 */
	public static final double VIEW_ANGLE = 43;
	
	/**
	 * The resolution in pixels of the NXTCam on the x-axis.
	 */
	public static final int SIZE_X = 144;
	
	/**
	 * The resolution in pixels of the NXTCam on the y-axis.
	 */
	public static final int SIZE_Y = 88;
	
	public Camera() {
		cam = new NXTCam(SensorPort.S2);
		cam.enableTracking(true);
	}
	
	/**
	 * Returns all objects which match the given colormap
	 * @param colormap The colormap id (starting at zero)
	 * @return An array of Rectangles corresponding to the detected objects
	 */
	public Rectangle[] getObjects(int colormap) {
		int n = cam.getNumberOfObjects();
		ArrayList<Rectangle> objs = new ArrayList<>(n);
		for(int i = 0; i < n; i++) {
			if(cam.getObjectColor(i) == colormap) {
				Rectangle r = cam.getRectangle(i);
				if(r.width * r.height > 20) {
					objs.add(cam.getRectangle(i));
				}
			}
		}
		
		Rectangle[] ret = new Rectangle[objs.size()];
		for(int i = 0; i < ret.length; i++) {
			ret[i] = objs.get(i);
		}
		return ret;
	}
	
	/**
	 * Checks if an object of the given color has been detected.
	 * @param colormap The colormap which should be checked.
	 * @return returns true if at least one object has bee detected. False otherwise
	 */
	public boolean objectDetected(int colormap) {
		int n = cam.getNumberOfObjects();
		for(int i = 0; i < n; i++) {
			if(cam.getObjectColor(i) == colormap) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a robot has been detected by the camera.
	 * <p>This method returns true when an object has been
	 * detected that belongs to one of the colormaps defined
	 * for the robots (0,1,2)</p>
	 */
	public boolean robotDetected() {
		int n = cam.getNumberOfObjects();
		for(int i = 0; i < n; i++) {
			switch(cam.getObjectColor(i)) {
			case 0:
			case 1:
			case 2:
				return true;
			}
		}
		return false;
	}
	
	/*public Robot detectRobot(int colormap) {
		if(!objectDetected(colormap))
			return null;

		Rectangle[] objs = getObjects(colormap);
		
		if(objs.length == 0) {
			return null;
		}
		
		Graphics g = new Graphics();
		g.clear();
		
		for(Rectangle o : objs) {
			g.drawRect(camToScreenX(o.x), camToScreenY(o.y), camToScreenX(o.width), camToScreenY(o.height));
			//g.drawRect(o.x, o.y, o.width, o.height);
		}
		
		g.drawString(""+colormap, 49, 63, Graphics.BOTTOM | Graphics.HCENTER);
		
		Rectangle obj = objs[0];
		Robot rob = null;
		
		if(objs.length > 1) {
			obj = mergeObjects(objs);
		}

		rob = new Robot();
		rob.setLocation(rob.pointAt((float)getObjectDistance(obj, 10.0, 4.7), (float)getObjectAngle(objs[0])));
		rob.setHeading((float)(Robot.me.getHeading()+getObjectAngle(obj)));
		return rob;
	}*/
	
	/**
	 * <p>Attempts to detect the distance and angle to the
	 * robot with the given colormap. If the robot is not
	 * seen by the camera this method returns null.</p>
	 * 
	 * <p>This method just checks a single robot with the given
	 * id. In order to check for all robots use detectRobots()</p>
	 * @param colormap The colormap(=robot id) of the robot to detect.
	 * @return The detected robot or null if the robot is not seen by the camera.
	 */
	public DetectedObject detectRobot(int colormap) {
		if(!objectDetected(colormap))
			return null;

		Rectangle[] objs = getObjects(colormap);
		
		if(objs.length == 0) {
			return null;
		}
		
		/*Graphics g = new Graphics();
		g.clear();
		
		for(Rectangle o : objs) {
			g.drawRect(camToScreenX(o.x), camToScreenY(o.y), camToScreenX(o.width), camToScreenY(o.height));
			//g.drawRect(o.x, o.y, o.width, o.height);
		}
		
		g.drawString(""+colormap, 49, 63, Graphics.BOTTOM | Graphics.HCENTER);*/
		
		Rectangle obj = objs[0];
		
		if(objs.length > 1) {
			obj = mergeObjects(objs);
		}
		
		DetectedObject rob = new DetectedObject(obj, colormap);
		rob.setAngle((float)getObjectAngle(obj));
		rob.setDistance((float)getObjectDistance(obj, 20, 10));
		
		return rob;
	}
	
	/**
	 * Returns the NXTCam object which can be used to interface
	 * with the NXTCam directly.
	 */
	public NXTCam getCamera() {
		return cam;
	}
	
	/**
	 * Returns a single rectangle that has been detected by the
	 * camera which corresponds to the robot with the given id.
	 */
	public Rectangle getRobotRectangle(int colormap) {
		Rectangle[] objs = getObjects(colormap);
		
		if (objs.length == 0)
			return null;
		
		if (objs.length == 1)
			return objs[0];
		
		return mergeObjects(objs);
	}
	
	/**
	 * Returns a single rectangle for the robot which
	 */
	public Rectangle getNearestRobotRectangle() {
		Rectangle[] robs = new Rectangle[3];
		robs[0] = getRobotRectangle(0);
		robs[1] = getRobotRectangle(1);
		robs[2] = getRobotRectangle(2);
		double distance = Float.MAX_VALUE;
		Rectangle ret = null;
		
		for(Rectangle r : robs) {
			if(r != null && getObjectDistance(r, 10, 5) < distance) {
				ret = r;
				distance = getObjectDistance(r, 15, 10);
			}
		}
		
		return ret;
	}
	
	/**
	 * This method can be used to find out which of the
	 * detected objects is closest to the camera. If the
	 * given does not contain any detected objects (the
	 * array is empty or all entries are null) this method
	 * returns -1.
	 * @param objs The detected objects for which the closest
	 * should be determined.
	 * @return The index in the given array of detected objects
	 * which belongs to the object which is closes to the camera
	 */
	public int getNearestId(DetectedObject[] objs) {
		float distance = Float.MAX_VALUE;
		int id = -1;
		
		for(int i = 0; i < objs.length; i++) {
			if(objs[i] != null && objs[i].getDistance() < distance) {
				distance = objs[i].getDistance();
				id = i;
			}
		}
		
		return id;
	}
	
	/**
	 * <p>Checks all robots if they can be detected and returns
	 * an array containing the information about the detected
	 * robots. The index in the array equals the id of the
	 * robot. If a robot is not seen by the camera its entry
	 * in the array is null.
	 */
	public DetectedObject[] detectRobots() {
		DetectedObject[] r = new DetectedObject[3];
		r[0] = detectRobot(0);
		r[1] = detectRobot(1);
		r[2] = detectRobot(2);
		
		return r;
	}
	
	/**
	 * Utility function which calculates the angle to a certain detected object,
	 * relative to the direction the robot is facing.
	 * @param obj The object to which the angle should be calculated
	 * @return The angle to the given object in degrees.
	 */
	public static double getObjectAngle(Rectangle obj) {
		double x = obj.getCenterX() - SIZE_X/2;
		return (x / (SIZE_X / 2)) * (VIEW_ANGLE / 2); 
	}
	
	
	/**
	 * Returns the distance to the given object.
	 * @param obj The object to which the distance should be measured
	 * @param size The horizontal size of the object in cm
	 * @return The distance to the given object in cm
	 */
	public static double getObjectDistance(Rectangle obj, double width, double height) {
		/*normalize the detected object dimensions
		 * the bigger normalized value will be used for the distance
		 * because it is less likely to be reduced in size resulting
		 * from the object being rotated away from the camera
		 */
		double normW = obj.width / width;
		double normH = obj.height / height;
		
		double psize = 0;	//pixel size of the object
		double rsize = 0;	//realworld size of the object
		double res = 0;		//camera resolution in the used axis
		
		if (normW < normH) {
			psize = obj.height;
			rsize = height;
			res = SIZE_Y;
		} else {
			psize = obj.width;
			rsize = width;
			res = SIZE_X;
		}
		
		return -(rsize * res) / (psize * 2 * Math.tan(VIEW_ANGLE / 2));
	}
	
	/**
	 * Merges the given rectangles into a single rectangle by
	 * filtering out noise and merging the remaining rectangles
	 * into one.
	 * @param objs The rectangles to merge.
	 */
	public static Rectangle mergeObjects(Rectangle[] objs) {
		Rectangle r = objs[0];
		float size = r.height * r.width;
		
		for(int i = 1; i < objs.length; i++) {
			if(objs[i].height * objs[i].width > size) {
				r = objs[i];
				size = r.height * r.width;
			}
		}
		
		Rectangle bigr = new Rectangle(r.x-r.width/2, r.y-r.height/2, r.width*2, r.height*2);
		
		for(int i = 0; i < objs.length; i++) {
			if(objs[i] == null)
				continue;
			
			if(r.intersects(objs[i]) || bigr.intersects(objs[i])) {
				if(objs[i].x < r.x) r.x = objs[i].x;
				if(objs[i].y < r.y) r.y = objs[i].y;
				if(objs[i].width+objs[i].x > r.x + r.width)
					r.width = objs[i].width+objs[i].x-r.x;
				if(objs[i].height+objs[i].y > r.y + r.height)
					r.height = objs[i].height+objs[i].y-r.y;
				bigr = new Rectangle(r.x-r.width/2, r.y-r.height/2, r.width*2, r.height*2);
				objs[i] = null;
				i = 0;
			}
		}
		
		return r;
	}
	
	/**
	 * converts the x coordinate from the camera in order to
	 * display rectangles from the camera on the NXT screen.
	 * @param x The x coordinate in the camera coordinate system.
	 * @return The x coordinate in the NXT screen coordinate system.
	 */
	public static int camToScreenX(double x) {
		return (int) ((x/144.0)*100);
	}
	
	/**
	 * converts the y coordinate from the camera in order to
	 * display rectangles from the camera on the NXT screen.
	 * @param y The y coordinate in the camera coordinate system.
	 * @return The y coordinate in the NXT screen coordinate system.
	 */
	public static int camToScreenY(double y) {
		return (int) ((y/88.0)*63);
	}
}
