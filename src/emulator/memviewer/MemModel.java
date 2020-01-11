package emulator.memviewer;

import javax.swing.table.AbstractTableModel;

import emulator.engine.CpuContext;
import emulator.engine.Engine;

public class MemModel extends AbstractTableModel {
	private static final long serialVersionUID = 305334635501584898L;
	
	public String[] columnNames = { "Addr", "0-1", "2-3", "4-5", "6-7"};
	public int[][] grid = new int[Engine.MEM_SIZE / 4][5];

	private CpuContext ctx;
	
	public MemModel(CpuContext ctx) {
		this.ctx = ctx;
		
		int addr = 0;
		for (int i = 0; i < (Engine.MEM_SIZE/4); i++) {
			for (int j = 0; j < 5; j++) {
				if (j == 0) {
					grid[i][j] = (addr * 2);
				} else {
					grid[i][j] = ctx.memory[addr];
					addr++;
				}
			}
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return grid.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return String.format("%05x", grid[row][col]);
		return String.format("%04x", grid[row][col]);
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
		// Prva kolona ne moze da se menja
		if (col < 1) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		short s;
		/*
		if (value instanceof String) {
			col--;
			s = Short.parseShort(value.toString(), 16);
			ctx.memory[row * 8 + col] = s;
			ctx.mdl.reset();
			ctx.mdl.disassm();
			ctx.mdl.fireTableDataChanged();
			Instruction i = ctx.mdl.addr_instr[ctx.pc.val];
			ctx.engine.main.tblSrc.setRowSelectionInterval(i.tableLine, i.tableLine);
			ctx.engine.main.tblSrc.scrollRectToVisible(ctx.engine.main.tblSrc.getCellRect(i.tableLine, 0, true));
		} else*/ {
			s = (short)value;
		}
		grid[row][col + 1] = s;
		fireTableCellUpdated(row, col+1);
		fireTableDataChanged();
	}
}
