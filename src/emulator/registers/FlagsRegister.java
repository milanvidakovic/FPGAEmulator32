package emulator.registers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class FlagsRegister extends JLabel {
	private static final long serialVersionUID = -7236250344841037158L;
	
	public String name;
	public int val;

	public FlagsRegister(String name, CpuContext ctx) {
		this.name = name;
		this.setFont(this.getFont().deriveFont(20f));
		update();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				String v = JOptionPane.showInputDialog("Enter new value");
				if (v != null) {
					if (v.startsWith("0x")) {
						FlagsRegister.this.val = Integer.parseInt(v.substring(2), 16);
					} else {
						FlagsRegister.this.val = Integer.parseInt(v);
					}
					@SuppressWarnings("static-access")
					Instruction i = ctx.mdl.addr_instr[ctx.pc.val];
					ctx.engine.refreshUI(i);
				}
			}
		});
	}
	
	public void update() {
		setText(format());
		setToolTipText(String.format("%s: %d", this.name, this.val));
	}
	
	private String format() {
		String retVal = "POCZ:";
		if ((this.val & 0x8) != 0) 
			retVal += "1";
		else retVal += "0";
		if ((this.val & 0x4) != 0) 
			retVal += "1";
		else retVal += "0";
		if ((this.val & 0x2) != 0) 
			retVal += "1";
		else retVal += "0";
		if ((this.val & 0x1) != 0) 
			retVal += "1";
		else retVal += "0";
		return retVal;
	}
}
