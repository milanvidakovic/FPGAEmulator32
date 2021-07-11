package emulator.util;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
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
        GraphicsConfiguration conf = parent.getGraphicsConfiguration();
		GraphicsDevice gd = conf.getDevice();
		sun.awt.Win32GraphicsDevice wd = (sun.awt.Win32GraphicsDevice)gd;
        //dlg.setBounds(100, 100, 800, 600); 

		
        dlg.setBounds((int)(parent.getX() * wd.getDefaultScaleX()) + 50, 
        		(int)(parent.getY() * wd.getDefaultScaleY()) + 50, 
        		(int)(parent.getWidth() * wd.getDefaultScaleX()) / 2, 
        		(int)(parent.getHeight() * wd.getDefaultScaleY()) / 2);
      
        return dlg;
    }
}
