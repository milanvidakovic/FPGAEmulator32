package emulator.util;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class MyFileChooser extends JFileChooser {
	private static final long serialVersionUID = -1800134221248401193L;
	JDialog created;
	
	@SuppressWarnings({ "restriction"})
	@Override
    protected JDialog createDialog(Component parent)
            throws HeadlessException {
        JDialog dlg = super.createDialog(parent);
        
//        GraphicsConfiguration conf = parent.getGraphicsConfiguration();
//		GraphicsDevice device = conf.getDevice();
//		double _scale = device.getDisplayMode().getWidth() / (double) device.getDefaultConfiguration().getBounds().width;
//        dlg.setBounds(100, 100, 800, 600);
        
        double scale = 1;//WindowUtil.getMainWindowScale();

//		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length == 1)
//			scale = 1;
		
        dlg.setBounds((int)(parent.getX() * scale) + 50, 
        		(int)(parent.getY() * scale) + 50, 
        		(int)(parent.getWidth() / 2), 
        		(int)(parent.getHeight() / 2));
      
        return dlg;
    }
}
