package emulator.engine;

import emulator.framebuffer.FBViewer;
import emulator.registers.FlagsRegister;
import emulator.registers.Register;
import emulator.src.SrcModel;

/**
 * CPU context. Holds all registers, flags and memory.
 */
public class CpuContext {
	public Register r0 = new Register("r00", this);
	public Register r1 = new Register("r01", this);
	public Register r2 = new Register("r02", this);
	public Register r3 = new Register("r03", this);
	public Register r4 = new Register("r04", this);
	public Register r5 = new Register("r05", this);
	public Register r6 = new Register("r06", this);
	public Register r7 = new Register("r07", this);
	public Register r8 = new Register("r08", this);
	public Register r9 = new Register("r09", this);
	public Register r10 = new Register("r10", this);
	public Register r11 = new Register("r11", this);
	public Register r12 = new Register("r12", this);
	public Register r13 = new Register("r13", this);

	public Register pc = new Register("PC", this);
	public Register sp = new Register("SP", this);
	public Register h = new Register("H", this);
	public FlagsRegister f = new FlagsRegister("F", this);

	// received uart byte
	public byte uart;

	public short[] memory = new short[100000];

	public SrcModel mdl;
	public Engine engine;

	public CpuContext() {
		this.mdl = new SrcModel(this.memory);
	}

	public void reset() {
		r0.val = 0;
		r1.val = 0;
		r2.val = 0;
		r3.val = 0;
		r4.val = 0;
		r5.val = 0;
		r6.val = 0;
		r7.val = 0;
		r8.val = 0;
		r9.val = 0;
		r10.val = 0;
		r11.val = 0;
		r12.val = 0;
		r13.val = 0;
		pc.val = 0;
		sp.val = 0;
		h.val = 0;
		f.val = 0;
		/*
		 * for (int i = 0; i < this.memory.length; i++) { this.memory[i] = 0; }
		 */
		if (this.engine != null && this.engine.main.memViewer != null) {
			engine.main.memViewer.display.setText("                ");
			engine.main.sfViewer.display.setText("                ");
		}
	}

	public Register getReg(int reg) {
		switch (reg) {
		case 0:
			return r0;
		case 1:
			return r1;
		case 2:
			return r2;
		case 3:
			return r3;
		case 4:
			return r4;
		case 5:
			return r5;
		case 6:
			return r6;
		case 7:
			return r7;
		case 8:
			return r8;
		case 9:
			return r9;
		case 10:
			return r10;
		case 11:
			return r11;
		case 12:
			return r12;
		case 13:
			return r13;
		case 15:
			return sp;
		case 14:
			return h;
		}
		return null;
	}

	public void load(String fileName) {
		memory = new short[100000];
		this.mdl = new SrcModel(fileName, memory);
	}

	public int fromPort(int port) {
		switch (port) {
		case 64: {
			// UART byte
			return uart;
		}
		case 69: {
			return (int) (System.nanoTime() >> 20);
		}
		}
		return 0;
	}

	public void toPort(int port, int value) {
		switch (port) {
		case 128:
			if (value == 1)
				engine.main.fbViewer.setMode(FBViewer.GRAPHICS_MODE_320_240);
			else
				engine.main.fbViewer.setMode(FBViewer.TEXT_MODE);
			break;
		}
	}
}
