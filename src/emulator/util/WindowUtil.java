package emulator.util;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;

public class WindowUtil {

	@SuppressWarnings("restriction")
	public static void setBounds(int x, int y, int w, int h, Component frame, String displayId) {
		GraphicsConfiguration conf = frame.getGraphicsConfiguration();
		GraphicsDevice gd = frame.getGraphicsConfiguration().getDevice();
		sun.awt.Win32GraphicsDevice wd = (sun.awt.Win32GraphicsDevice)gd;
		Rectangle r = conf.getBounds();
		r.width *= wd.getDefaultScaleX();
		r.height *= wd.getDefaultScaleY();
		r.x *= wd.getDefaultScaleX();
		r.y *= wd.getDefaultScaleY();
		if (outside(x, w, r.x, r.width)) {
			// if x coordinate of the frame goes beyond its own display
			// we will reset it
			x = r.x + 10;
		}
		if (outside(y, h, r.y, r.height)) {
			// if y coordinate of the frame goes beyond its own display
			// we will reset it
			y = r.y + 10;
		}
		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length > 1)
			frame.setBounds(x, y, w, h);
		else
			frame.setBounds((int) (x / wd.getDefaultScaleX()), (int) (y / wd.getDefaultScaleY()),
				(int) (w / wd.getDefaultScaleX()), (int) (h / wd.getDefaultScaleY()));
	}
	
	private static boolean outside(int x, int w, int rx, int rw) {
		int rx1, rx2;
		if (rx < 0) {
			int delta = Math.abs(rx);
			rx1 = rx + delta;
			rx2 = rx + delta + rw;
			x += delta;
		} else {
			rx1 = rx;
			rx2 = rx + rw;
		}
		if (x < rx1 || x > rx2)
			return true;
		return false;
	}
	
	public static String getDisplayId(Window frame) {
		return frame.getGraphicsConfiguration().getDevice().getIDstring();
	}

	public static GraphicsConfiguration getGraphicsConfiguration(String displayId) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			//System.out.println(gd.getClass());
			if (gd.getIDstring().equals(displayId)) {
				return gd.getDefaultConfiguration();
			}
			
		}
		return ge.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	@SuppressWarnings("restriction")
	public static Rectangle getBounds(Window frame) {
		GraphicsDevice d = frame.getGraphicsConfiguration().getDevice();
		sun.awt.Win32GraphicsDevice wd = (sun.awt.Win32GraphicsDevice)d;
		Rectangle r = new Rectangle();
		r.x = (int) (frame.getX() * wd.getDefaultScaleX());
		r.y = (int) (frame.getY() * wd.getDefaultScaleY());
		r.width = (int) (frame.getWidth() * wd.getDefaultScaleX());
		r.height = (int) (frame.getHeight() * wd.getDefaultScaleY());
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
