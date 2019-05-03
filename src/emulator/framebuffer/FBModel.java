package emulator.framebuffer;

import javax.swing.table.AbstractTableModel;

import emulator.engine.CpuContext;
import emulator.engine.Engine;

public class FBModel extends AbstractTableModel {
	private static final long serialVersionUID = 305334635501584898L;
	
	public CpuContext ctx;
	
	public FBModel(CpuContext ctx) {
		this.ctx = ctx;
		
	}

	@Override
	public int getColumnCount() {
		return 80;
	}

	@Override
	public int getRowCount() {
		return 60;
	}

	@Override
	public String getColumnName(int col) {
		return "";
	}

	@Override
	public Object getValueAt(int row, int col) {
		int addr = Engine.VIDEO_OFFS + row*160 + col*2;
		char c = (char) (ctx.memory[addr / 2] & 0xff);
		return String.format("%c", c);
	}

	@Override
	/**
	 * Ako se ova metoda ne redefinise, koristi se default renderer/editor za
	 * celiju. To znaci da, ako je kolona tipa boolean, onda ce se u tabeli
	 * prikazati true/false, a ovako ce se za takav tip kolone pojaviti
	 * checkbox.
	 */
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		//fireTableDataChanged();
		fireTableCellUpdated(row, col);
	}
}
