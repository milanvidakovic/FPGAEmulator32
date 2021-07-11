package emulator.framebuffer;

import javax.swing.JFrame;

public interface IFBViewer {
	public void updateCell(int addr, short content);
	public void reset();
	public void setMode(int mode);
	public void setInverse(boolean b);
	public JFrame getFrame();
}
