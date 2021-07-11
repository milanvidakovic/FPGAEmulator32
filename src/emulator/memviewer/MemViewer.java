package emulator.memviewer;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import emulator.EmulatorMain;
import emulator.engine.CpuContext;
import emulator.engine.Engine;
import emulator.util.WindowUtil;

/**
 * Memory viewer. Content updated when instruction
 * writes something in the memory.
 */
public class MemViewer extends JFrame {
	private static final long serialVersionUID = -5500314457803056242L;
	public JTable tblMem;
	public MemModel memMdl;
	public JScrollPane src;
	
	public MouseListener popupListener;

	public JLabel display = new JLabel();

	public MemViewer(GraphicsConfiguration conf, CpuContext ctx, Engine eng, String catName) {
		super(conf);
		if (catName.equals("MemViewer")) {
			setTitle("Memory");
		} else {
			setTitle("Stack frame & write");
		}
		memMdl = new MemModel(ctx);
		tblMem = new JTable(memMdl);
		src = new JScrollPane(tblMem);
		getContentPane().add(src, BorderLayout.CENTER);

		JPanel pDisp = new JPanel();
		display.setFont(display.getFont().deriveFont(16f));
		display.setText("                ");
		pDisp.add(display);
		getContentPane().add(pDisp, BorderLayout.SOUTH);

		// popup menu
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Go to address");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If we choose the "Go to address" option from the
				// popup menu over some instruction
				// this code will just go to that location
				// (won't set the break point)
				String s = JOptionPane.showInputDialog("Enter address:");
				if (s != null) {
					// get the argument of that instruction
					// and look at it as an address
					int addr;
					if (s.startsWith("0x"))
						addr = Integer.parseInt(s.substring(2), 16);
					else
						addr = Integer.parseInt(s);
					updateCell(addr, ctx.memory[addr/2]);
					updateCell(addr - 2, ctx.memory[addr/2 - 1]);
					updateCell(addr - 4, ctx.memory[addr/2 - 2]);
					updateCell(addr - 6, ctx.memory[addr/2 - 3]);
					updateCell(addr + 2, ctx.memory[addr/2 + 1]);
					updateCell(addr + 4, ctx.memory[addr/2 + 2]);
					updateCell(addr + 6, ctx.memory[addr/2 + 3]);
					addr /= 8;
					tblMem.setRowSelectionInterval(addr, addr);
					tblMem.scrollRectToVisible(tblMem.getCellRect(addr, 0, true));
				}
			}
		});
		popup.add(menuItem);
		popupListener = new PopupListener(popup);
		tblMem.addMouseListener(popupListener);

		WindowUtil.setBounds(ctx.engine.main.ini.getInt(catName, "x", 1024), ctx.engine.main.ini.getInt(catName, "y", 100), 
				ctx.engine.main.ini.getInt(catName, "width", 400), ctx.engine.main.ini.getInt(catName, "height", 700), 
				this, ctx.engine.main.ini.getString(catName, "display", "\\Display0"));

		setVisible(true);
	}

	public void updateCell(int addr, short content) {
		if (addr < 0) return;
		int row = addr / 8;
		int col = (addr/2) % 4;
		if (EmulatorMain.DEBUG)
			System.out.println("CHANGED MEMORY LOCATION CONTENT at addr: " + String.format("0x%04x", addr) + ", to: " + String.format("0x%04x", content));
		memMdl.setValueAt(content, row, col);
	}

	public void updateCell32(int addr, int content) {
		int row = addr / 8;
		int col = (addr/2) % 4;
		if (EmulatorMain.DEBUG) {
			System.out.println("CHANGED MEMORY LOCATIONS CONTENT at addrs: " + String.format("0x%04x 0x%04x", addr, addr + 2) + ", to: " + String.format("0x%08x", content));
			System.out.println("row: " + row + ", col: " + col);
		}
		memMdl.setValueAt((short)(content >> 16), row, col);
		if (col <= 2)
			memMdl.setValueAt((short)(content & 0xFFFF), row, col + 1);
		else
			memMdl.setValueAt((short)(content & 0xFFFF), row+1, 0);			
	}

}

class PopupListener extends MouseAdapter {
	JPopupMenu popup;

	PopupListener(JPopupMenu popupMenu) {
		popup = popupMenu;
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}

