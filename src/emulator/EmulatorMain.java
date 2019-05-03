package emulator;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import emulator.engine.CpuContext;
import emulator.engine.Engine;
import emulator.framebuffer.FBViewer;
import emulator.memviewer.MemViewer;
import emulator.src.Instruction;
import emulator.src.NotImplementedException;
import emulator.util.IniFile;
import emulator.util.WindowUtil;

public class EmulatorMain extends JFrame {
	private static final long serialVersionUID = 5554754132655656443L;

	public static boolean DEBUG = false;

	final JFileChooser fc = new JFileChooser();

	public JButton btnLoad = new JButton("Load");
	public JButton btnGotoStart = new JButton("");
	public JButton btnRun = new JButton("Run");
	public JButton btnStop = new JButton("Stop");
	public JButton btnStepInto = new JButton("Step into");
	public JButton btnStepOver = new JButton("Step over");
	public JButton btnReset = new JButton("Reset");
	public JButton btnExit = new JButton("Exit");
	JCheckBox chbDebug = new JCheckBox("Debug");

	public JScrollPane src;
	public JTable tblSrc;

	/**
	 * CPU context. Holds all registers, flags and memory.
	 */
	CpuContext ctx = new CpuContext();
	/**
	 * Execution engine. Capable of running in full speed, stepping over and
	 * stepping into. supports breakpoints.
	 */
	Engine eng;
	/**
	 * Memory viewer. Content updated when instruction writes something in the
	 * memory.
	 */
	public MemViewer memViewer;
	/**
	 * The same memory viewer, but focused on the stack. Used to observe stack
	 * frame. Content updated when instruction writes something in the memory.
	 */
	public MemViewer sfViewer;

	public FBViewer fbViewer;

	public MouseListener popupListener;

	/**
	 * Ini file wrapper. Configuration written and read from the ini file.
	 */
	public IniFile ini;

	private int startAddr;

	public EmulatorMain() {
		ini = new IniFile("emulator.ini");

		JPanel registers = new JPanel();
		registers.setLayout(new GridLayout(3, 4));

		registers.add(ctx.getReg(0));
		registers.add(ctx.getReg(1));
		registers.add(ctx.getReg(2));
		registers.add(ctx.getReg(3));
		registers.add(ctx.getReg(4));
		registers.add(ctx.getReg(5));
		registers.add(ctx.getReg(6));
		registers.add(ctx.getReg(7));
		registers.add(ctx.getReg(8));
		registers.add(ctx.getReg(9));
		registers.add(ctx.getReg(10));
		registers.add(ctx.getReg(11));
		registers.add(ctx.getReg(12));
		registers.add(ctx.getReg(13));
		registers.add(ctx.pc);
		registers.add(ctx.sp);
		registers.add(ctx.h);
		registers.add(ctx.f);

		getContentPane().add(registers, BorderLayout.NORTH);

		tblSrc = new JTable(ctx.mdl);
		src = new JScrollPane(tblSrc);
		getContentPane().add(src, BorderLayout.CENTER);

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
				Instruction i = ctx.mdl.lines.get(tblSrc.getSelectedRow());
				if (i != null) {
					// get the argument of that instruction
					// and look at it as an address
					int addr = i.argument;
					// try to find an instruction at that address
					i = ctx.mdl.addr_instr[addr];
					if (i != null) {
						// if there is indeed an instruction
						// obtain the row in the source table
						int row = i.tableLine;
						// select that row
						tblSrc.setRowSelectionInterval(row, row);
						tblSrc.scrollRectToVisible(tblSrc.getCellRect(row, 0, true));
					}
				}
			}
		});
		popup.add(menuItem);
		popupListener = new PopupListener(popup);

		JPanel commands = new JPanel();
		commands.add(btnLoad);
		btnLoad.addActionListener(e -> loadProg());
		commands.add(btnGotoStart);
		this.startAddr = ini.getInt("general", "startAddr", 0xB000);
		btnGotoStart.setText("" + startAddr);
		btnGotoStart.addActionListener(e -> eng.gotoAddr(startAddr));
		btnGotoStart.setEnabled(false);
		commands.add(btnRun);
		btnRun.setToolTipText("F8");
		btnRun.setEnabled(false);
		btnRun.addActionListener(e -> eng.run());
		commands.add(btnStop);
		btnStop.addActionListener(e -> eng.stop());
		btnStop.setToolTipText("ESC");
		btnStop.setEnabled(false);
		int debugEnabled = ini.getInt("general", "debug", 1);
		EmulatorMain.DEBUG = debugEnabled == 1;
		chbDebug.setSelected(DEBUG);
		chbDebug.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (chbDebug.isSelected())
					EmulatorMain.DEBUG = true;
				else
					EmulatorMain.DEBUG = false;
			}
		});
		commands.add(chbDebug);
		commands.add(Box.createHorizontalStrut(50));

		commands.add(btnStepOver);
		btnStepOver.setEnabled(false);
		btnStepOver.setToolTipText("F10");
		btnStepOver.addActionListener(e -> {
			try {
				eng.stepOver();
			} catch (NotImplementedException e1) {
				e1.printStackTrace();
			}
		});
		commands.add(btnStepInto);
		btnStepInto.setEnabled(false);
		btnStepInto.setToolTipText("F11");
		btnStepInto.addActionListener(e -> {
			try {
				eng.stepInto();
			} catch (NotImplementedException e1) {
				e1.printStackTrace();
			}
		});

		commands.add(Box.createHorizontalStrut(50));
		commands.add(btnReset);
		btnReset.addActionListener(e -> {
			eng.reset();
		});
		btnReset.setToolTipText("Ctrl+R");
		btnReset.setEnabled(false);
		commands.add(Box.createHorizontalStrut(10));
		commands.add(btnExit);
		btnExit.addActionListener(e -> {
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		});
		getContentPane().add(commands, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveSettings();
			}
		});

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_RELEASED) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_F8:
						btnRun.doClick();
						e.consume();
						break;
					case KeyEvent.VK_F10:
						btnStepOver.doClick();
						e.consume();
						break;
					case KeyEvent.VK_F11:
						btnStepInto.doClick();
						e.consume();
						break;
					case KeyEvent.VK_ESCAPE:
						btnStop.doClick();
						e.consume();
						break;
					case KeyEvent.VK_R:
						if (e.isControlDown()) {
							btnReset.doClick();
							e.consume();
						}
						break;
					}
				}
				return false;
			}
		});
		WindowUtil.setLocation(ini.getInt("general", "x", 100), ini.getInt("general", "y", 100),
				ini.getInt("general", "width", 800), ini.getInt("general", "height", 600), this);

		//pack();
		FBViewer.titleBarHeight = 55;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void saveSettings() {
		if (memViewer != null) {
			ini.setInt("MemViewer", "width", memViewer.getWidth());
			ini.setInt("MemViewer", "height", memViewer.getHeight());
			ini.setInt("MemViewer", "x", memViewer.getX());
			ini.setInt("MemViewer", "y", memViewer.getY());
		}
		if (sfViewer != null) {
			ini.setInt("SfViewer", "width", sfViewer.getWidth());
			ini.setInt("SfViewer", "height", sfViewer.getHeight());
			ini.setInt("SfViewer", "x", sfViewer.getX());
			ini.setInt("SfViewer", "y", sfViewer.getY());
		}
		if (fbViewer != null) {
			ini.setInt("FB", "width", fbViewer.getWidth());
			ini.setInt("FB", "height", fbViewer.getHeight());
			ini.setInt("FB", "x", fbViewer.getX());
			ini.setInt("FB", "y", fbViewer.getY());
		}
		ini.setInt("general", "width", getWidth());
		ini.setInt("general", "height", getHeight());
		ini.setInt("general", "x", getX());
		ini.setInt("general", "y", getY());
		ini.setInt("general", "debug", DEBUG ? 1 : 0);
		ini.saveINI();
	}

	/**
	 * Loading machine code into the memory.
	 */
	private void loadProg() {
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				if (f.getName().endsWith(".bin"))
					return true;
				return false;
			}

			@Override
			public String getDescription() {
				return "Binary executables";
			}
		});
		fc.setCurrentDirectory(new File(ini.getString("general", "startDir", ".")));
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (eng != null)
				eng.halt();
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			File file = fc.getSelectedFile();
			ctx.load(file.getAbsolutePath());
			setTitle(file.getName());
			ini.setString("general", "startDir", file.getAbsolutePath());
			ini.saveINI();
			eng = new Engine(ctx, this);
			if (memViewer != null) {
				memViewer.dispose();
			}
			memViewer = new MemViewer(ctx, eng, "MemViewer");

			if (sfViewer != null) {
				sfViewer.dispose();
			}
			sfViewer = new MemViewer(ctx, eng, "SfViewer");

			if (fbViewer != null) {
				fbViewer.dispose();
			}
			fbViewer = new FBViewer(ctx, eng);

			eng.setMemViewer(memViewer);
			eng.setSfViewer(sfViewer);
			eng.setFBViewer(fbViewer);

			src.remove(tblSrc);
			tblSrc = new JTable(ctx.mdl);
			src.getViewport().add(tblSrc);
			src.revalidate();
			src.repaint();
			tblSrc.setRowSelectionInterval(0, 0);
			tblSrc.addMouseListener(popupListener);

			btnGotoStart.setEnabled(true);
			btnRun.setEnabled(true);
			btnStepOver.setEnabled(true);
			btnStepInto.setEnabled(true);
			btnReset.setEnabled(true);
			btnStop.setEnabled(true);
			eng.gotoAddr(startAddr);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

	public static void main(String[] args) {
		new EmulatorMain();

	}

}
