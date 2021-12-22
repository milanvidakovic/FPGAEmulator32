package emulator.util;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;

public class WindowUtil {

//	@SuppressWarnings("restriction")
	public static void setBounds(int x, int y, int width, int height, Component frame, String displayId) {
		GraphicsConfiguration conf = frame.getGraphicsConfiguration();
//		GraphicsDevice gd = conf.getDevice();
//		sun.awt.Win32GraphicsDevice wd = (sun.awt.Win32GraphicsDevice)gd;
		Rectangle r = conf.getBounds();
		Rectangle r2 = WindowUtil.getBounds((Window)frame);
		
//		r.x = x;
//		r.y = y;
//		r.width = width;
//		r.height = height;

//		r.width *= wd.getDefaultScaleX();
//		r.height *= wd.getDefaultScaleY();
//		r.x *= wd.getDefaultScaleX();
//		r.y *= wd.getDefaultScaleY();
//		
		if (outside(x, width, r.x, r.width)) {
			// if x coordinate of the frame goes beyond its own display
			// we will reset it
			x = r2.x + 10;
		}
		if (outside(y, height, r.y, r.height)) {
			// if y coordinate of the frame goes beyond its own display
			// we will reset it
			y = r2.y + 10;
		}
//		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length > 1)
			frame.setBounds(x, y, width, height);
//		else
//			frame.setBounds((int) (x / wd.getDefaultScaleX()), (int) (y / wd.getDefaultScaleY()),
//				(int) (w / wd.getDefaultScaleX()), (int) (h / wd.getDefaultScaleY()));
	}
	
	private static boolean outside(int x, int w, int rx, int rw) {
//		int rx1, rx2;
//		if (rx < 0) {
//			int delta = Math.abs(rx);
//			rx1 = rx + delta;
//			rx2 = rx + delta + rw;
//			x += delta;
//		} else {
//			rx1 = rx;
//			rx2 = rx + rw;
//		}
		if (x < rx || x > (rx + rw))
			return true;
		return false;
	}
	
	public static String getDisplayId(Window frame) {
		return frame.getGraphicsConfiguration().getDevice().getIDstring();
	}
	
	static float scaleX, scaleY;
	@SuppressWarnings("restriction")
	public static GraphicsConfiguration getGraphicsConfiguration(String displayId) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		scaleX = ((sun.awt.Win32GraphicsDevice)gs[0]).getDefaultScaleX();
		scaleY = ((sun.awt.Win32GraphicsDevice)gs[0]).getDefaultScaleY();
		for (int j = 1; j < gs.length; j++) {
			scaleX /=  ((sun.awt.Win32GraphicsDevice)gs[j]).getDefaultScaleX();
			scaleY /=  ((sun.awt.Win32GraphicsDevice)gs[j]).getDefaultScaleY();
		}
		
		
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			//System.out.println(gd.getClass());
			if (gd.getIDstring().equals(displayId)) {
				return gd.getDefaultConfiguration();
			}
			
		}
		System.out.println("##### BLAST #### Could not find graphics configuration " + displayId);
		return ge.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	public static Rectangle getBounds(Window frame) {
		GraphicsConfiguration conf = frame.getGraphicsConfiguration();
//		String displayId = gd.getIDstring();
//		sun.awt.Win32GraphicsDevice wd = (sun.awt.Win32GraphicsDevice)gd;
		Rectangle r = conf.getBounds();
		float _scaleX = scaleX;
		float _scaleY = scaleY;
		if (r.x == 0) {
			_scaleX = 1;
		}
		if (r.y == 0) {
			_scaleY = 1;
		}
		r.x = (int) (frame.getX() * _scaleX);
		r.y = (int) (frame.getY() * _scaleY);
		r.width = (int) (frame.getWidth() * _scaleX);
		r.height = (int) (frame.getHeight() * _scaleY);
		/*
		r.x = (int) (frame.getX() * wd.getDefaultScaleX());
		r.y = (int) (frame.getY() * wd.getDefaultScaleY());
		r.width = (int) (frame.getWidth()   * wd.getDefaultScaleX());
		r.height = (int) (frame.getHeight() * wd.getDefaultScaleY());
		*/
		return r;
	}

	public static void saveIni(Window frame, String section, IniFile ini) {
		Rectangle r = WindowUtil.getBounds(frame);
		ini.setInt(section, "width", r.width);
		ini.setInt(section, "height", r.height);
		ini.setInt(section, "x", r.x);
		ini.setInt(section, "y", r.y);
		ini.setString(section, "display", WindowUtil.getDisplayId(frame));
	}
}
