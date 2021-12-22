package emulator.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import emulator.framebuffer.FBViewer;
import emulator.raspbootin.Raspbootin64Client;
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
	
	public int IRQ_MASK = 0xFFFF;

	// received uart byte
	public byte uart;

	public short[] memory = new short[Engine.MEM_SIZE];

	public SrcModel mdl;
	public static SymTable symTable;
	public static DebugTable dbgTable;
	public Engine engine;
	private Raspbootin64Client raspbootin;
	PipedOutputStream serialOut;
	// Wire an input stream to the output stream, and use a buffer of 2048 bytes
	PipedInputStream serialIn;
	public int counterTrigger;

	public CpuContext() {
		this.mdl = new SrcModel(this.memory);
		this.serialOut = new PipedOutputStream();
		// Wire an input stream to the output stream, and use a buffer of 2048 bytes
		try {
			this.serialIn = new PipedInputStream(serialOut, 2048);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.raspbootin = new Raspbootin64Client(serialOut, serialIn);
		this.raspbootin.start();
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
		this.memory = new short[Engine.MEM_SIZE];
		CpuContext.symTable = new SymTable(fileName);
		CpuContext.dbgTable = new DebugTable(fileName);
		this.mdl = new SrcModel(fileName, memory);
	}

	public static final int PORT_UART_RX_BYTE = 640; // port which contains received byte via UART
	public static final int PORT_UART_TX_BUSY = 650; // port which has 1 when UART TX is busy
	public static final int PORT_MILLIS = 690; // current number of milliseconds counted so far
	public static final int PORT_MOUSE = 800; // byte from mouse
	public static final int PORT_MOUSE_STRUCT_ADDR = 810; // pointer to the mouse struct in memory (x, y, key and status fields) 

	public int mouseByte;
	public int mouse_struct_addr;
	public int fromPort(int port) {
		switch (port) {
		case PORT_UART_RX_BYTE:
			// UART byte
			try {
				int b = serialIn.read();
				return b;
			} catch (IOException e) {
				e.printStackTrace();
			}
		case PORT_UART_TX_BUSY:
			return 0;
		case PORT_MILLIS:
			return (int) (System.nanoTime() >> 20);
//			return (int) (System.currentTimeMillis());
		case PORT_MOUSE:
			if (mouseByte < 0)
				mouseByte = 256 + mouseByte;
			System.out.println("PORT MOUSE: " + mouseByte);
			return mouseByte << 16;
		case PORT_MOUSE_STRUCT_ADDR:
			return mouse_struct_addr;
		}
		return 0;
	}

	static final int PORT_UART_TX_SEND_BYTE = 660; // port for sending character via UART
	static final int PORT_VIDEO_MODE = 1280; // video mode type (0-text; 1-graphics), (write)
	static final int PORT_TIMER = 1290; // timer irq port (number of milliseconds before the irq is triggered)
	static final int VGA_TEXT_INVERSE = 1300; // if 1, then the screen is inversed (black letters on white background)

	public void toPort(int port, int value) {
		switch (port) {
		case PORT_VIDEO_MODE:
			if (value == 1)
				engine.main.fbViewer.setMode(FBViewer.GRAPHICS_MODE_320_240);
			else if (value == 2)
				engine.main.fbViewer.setMode(FBViewer.GRAPHICS_MODE_640_480);
			else
				engine.main.fbViewer.setMode(FBViewer.TEXT_MODE);
			break;
		case VGA_TEXT_INVERSE:
			if (value == 1) {
				engine.main.fbViewer.setInverse(true);
				engine.main.fbViewer.reset();
			} else {
				engine.main.fbViewer.setInverse(false);
				engine.main.fbViewer.reset();
			}
			break;
		case PORT_UART_TX_SEND_BYTE:
			try {
				serialOut.write(value);
				serialOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case PORT_TIMER:
			engine.counter = (int) (System.nanoTime() >> 20);
			this.counterTrigger = value;
			engine.counterTrigger = (int)(System.nanoTime() >> 20) + value;
			break;
		case PORT_MOUSE_STRUCT_ADDR:
			mouse_struct_addr = value;
			break;
		}
	}

	public void loadExternalProgram(String fileName) {
		FileInputStream in;
		try {
			File f = new File(fileName);
			in = new FileInputStream(f);

			byte[] buffer = new byte[(int) f.length() * 2];
			in.read(buffer);

			this.mdl.parse(buffer, 197632/2, (int) f.length());
			this.mdl.disassm(197632, (int)f.length());

			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
