package emulator.util;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Arrays;

public class WindowUtil {

//	@SuppressWarnings("restriction")
	public static void setBounds(int x, int y, int width, int height, Window frame, String displayId) {
		GraphicsConfiguration conf = frame.getGraphicsConfiguration();
		Rectangle r = conf.getBounds();
		Rectangle r2 = WindowUtil.getBounds(frame);

		double scale = getMainWindowScale();
		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length > 1) {
			if (outside(x, width, (int)(r.x * scale), (int)(r.width*scale))) {
				 // if x coordinate of the frame goes beyond its own display
				 // we will reset it
				x = r2.x + 10;
			}
			if (outside(y, height, (int)(r.y * scale), (int)(r.height*scale))) {
				 // if y coordinate of the frame goes beyond its own display
				 // we will reset it
				y = r2.y + 10;
			}
			frame.setBounds((int)(x/scale), (int)(y/scale), (int)(width/scale), (int)(height/scale));
		} else {
			if (outside((int)(x/scale), width, (int)(r.x ), (int)(r.width))) {
				 // if x coordinate of the frame goes beyond its own display
				 // we will reset it
				x = r2.x + 10;
			}
			if (outside((int)(y/scale), height, (int)(r.y ), (int)(r.height))) {
				 // if y coordinate of the frame goes beyond its own display
				 // we will reset it
				y = r2.y + 10;
			}
			frame.setBounds((int) (x / scale), (int) (y / scale),
				(int) (width / scale), (int) (height / scale));
		}
	}
	
	private static boolean outside(int x, int w, int rx, int rw) {
		if (x < rx ||  x > (rx + rw))
			return true;
		return false;
	}
	
	public static String getDisplayId(Window frame) {
		return frame.getGraphicsConfiguration().getDevice().getIDstring();
	}
	
	@SuppressWarnings("restriction")
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
		System.out.println("##### BLAST #### Could not find graphics configuration " + displayId);
		return ge.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	public static Rectangle getBounds(Window frame) {
		GraphicsConfiguration conf = frame.getGraphicsConfiguration();
		Rectangle r = conf.getBounds();
		double _scale = getWindowScale(frame);
		//_scale = 2.25;
		r.x = (int) (frame.getX() * _scale);
		r.y = (int) (frame.getY() * _scale);
		r.width = (int) (frame.getWidth() * _scale);
		r.height = (int) (frame.getHeight() * _scale);
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
	
	public static double getMainWindowScale() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();
		for (int j = 0; j < gds.length; j++) {
			GraphicsDevice gd = gds[j];
			Rectangle r = gd.getDefaultConfiguration().getBounds();
			if (r.x == 0) {
				// we have found the main window
				return gd.getDisplayMode().getWidth() / (double) gd.getDefaultConfiguration().getBounds().width;
			}
		}
		return 1;
	}
	
	private static GraphicsDevice getWindowDevice2(Window window) {
		String displayId = window.getGraphicsConfiguration().getDevice().getIDstring();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			//System.out.println(gd.getClass());
			if (gd.getIDstring().equals(displayId)) {
				return gd;
			}
			
		}
		System.out.println("##### BLAST #### Could not find graphics configuration " + displayId);
		return ge.getDefaultScreenDevice();
	}
	
	public static double getWindowScale(Window window) {
	    GraphicsDevice device = getWindowDevice2(window);
	    return device.getDisplayMode().getWidth() / (double) device.getDefaultConfiguration().getBounds().width;
	}

//	public static GraphicsDevice getWindowDevice(Window window) {
//	    Rectangle bounds = window.getBounds();
//	    return  Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()).stream()
//
//	            // pick devices where window located
//	            .filter(d -> d.getDefaultConfiguration().getBounds().intersects(bounds))
//
//	            // sort by biggest intersection square
//	            .sorted((f, s) -> Long.compare(//
//	                    square(f.getDefaultConfiguration().getBounds().intersection(bounds)),
//	                    square(s.getDefaultConfiguration().getBounds().intersection(bounds))))
//
//	            // use one with the biggest part of the window
//	            .reduce((f, s) -> s) //
//
//	            // fallback to default device
//	            .orElse(window.getGraphicsConfiguration().getDevice());
//	}
//
//	public static long square(Rectangle rec) {
//	    return Math.abs(rec.width * rec.height);
//	}
	
}
