package emulator.src;

import java.util.List;

import emulator.engine.CpuContext;

public class Instruction {

	public static final int BREAK_POINT = 0;
	public static final int ADDR = 1;
	public static final int CONTENT = 2;
	public static final int ASSEMBLER = 3;

	public static final int ADD_S = 1;
	public static final int SUB_S = 2;
	public static final int AND_S = 3;
	public static final int OR_S = 4;
	public static final int XOR_S = 5;
	public static final int SHR_S = 6;
	public static final int SHL_S = 7;
	public static final int MUL_S = 8;
	public static final int DIV_S = 9;
	public static final int ADD_B = 10;
	public static final int SUB_B = 11;
	public static final int AND_B = 12;
	public static final int OR_B = 13;
	public static final int XOR_B = 14;
	public static final int SHR_B = 15;
	public static final int SHL_B = 16;
	public static final int MUL_B = 17;
	public static final int DIV_B = 18;
	public static final int ADD_W = 19;
	public static final int SUB_W = 20;
	public static final int AND_W = 21;
	public static final int OR_W = 22;
	public static final int XOR_W = 23;
	public static final int SHR_W = 24;
	public static final int SHL_W = 25;
	public static final int MUL_W = 26;
	public static final int DIV_W = 27;
	public static final int FADD = 28;
	public static final int FSUB = 29;
	public static final int FMUL = 30;
	public static final int FDIV = 31;

	public static int MANTISSA_LEN = 10;
	public static int EXPONENT_LEN = 5;
	public static int BIAS = 15;
	public static int FMAX = 65504;
	public static int FONE = 0b0011110000000000;
	public static int FNONE = 0b1011110000000000;
	public static int FZERO = 0b0000000000000000;
	public static int FNZERO = 0b1000000000000000;
	public static int FINF = 0b0111110000000000;
	public static int FNAN = 0b0111111111111111;

	public boolean breakPoint;
	public int addr;
	public String content = "";
	public short opcode;
	public int src;
	public String ssrc;
	public int dest;
	public String sdest;
	public boolean hasArgument = false;
	public int argument;
	public String assembler = "";
	public boolean isJump = false;
	public int arglen = 2;

	public short[] memory;
	/**
	 * Table row where this instruction is placed.
	 */
	public int tableLine;

	private Instruction(short[] memory, int addr) {
		this.addr = addr - 2;
		this.memory = memory;
		this.opcode = memory[this.addr / 2];
		setContent();
	}

	public Instruction(short[] memory, int addr, int src, int dest) {
		this(memory, addr);
		this.src = src;
		this.dest = dest;
		this.ssrc = getName(src);
		this.sdest = getName(dest);
	}

	private String getName(int reg) {
		switch (reg) {
		case 0:
			return "r0";
		case 1:
			return "r1";
		case 2:
			return "r2";
		case 3:
			return "r3";
		case 4:
			return "r4";
		case 5:
			return "r5";
		case 6:
			return "r6";
		case 7:
			return "r7";
		case 8:
			return "r8";
		case 9:
			return "r9";
		case 10:
			return "r10";
		case 11:
			return "r11";
		case 12:
			return "r12";
		case 13:
			return "r13";
		case 15:
			return "sp";
		case 14:
			return "h";
		}
		return "";
	}

	public void exec(CpuContext ctx) throws NotImplementedException {
		throw new NotImplementedException(this.assembler + " not implemented yet!");
	}

	public Object toCell(int col) {
		switch (col) {
		case BREAK_POINT:
			return breakPoint;
		case ADDR:
			List<String> l = CpuContext.symTable.sym.get(addr);
			if (l != null && l.size() > 0) {
				return String.format("%08X (%s)", addr, l.get(0));
			} else 
				return String.format("%08X", addr);
		case CONTENT:
			return content;
		case ASSEMBLER:
			return assembler;
		}
		return null;
	}

	public void setContent() {
		if (this.hasArgument) {
			if (this.arglen == 4)
				this.content = String.format("%04x, %08x", this.opcode, this.argument);
			else if (this.arglen == 2)
				this.content = String.format("%04x, %04x", this.opcode, this.argument);
		} else {
			this.content = String.format("%04x", this.opcode);
		}
	}

	public void setArgument8() {
		short w1 = memory[(this.addr + 2) / 2];
		this.argument = fix8(w1);
		this.hasArgument = true;
		this.arglen = 2;
	}

	public void setArgument() {
		short w1 = memory[(this.addr + 2) / 2];
		this.argument = fix(w1);
		this.hasArgument = true;
		this.arglen = 2;
	}

	public void setArgument32() {
		short w1 = memory[(this.addr + 2) / 2];
		short w2 = memory[(this.addr + 4) / 2];
		this.argument = fixInt(w1, w2);
		this.hasArgument = true;
		this.arglen = 4;
	}

	public void setAssembler(String format) {
		if (this.hasArgument) {
			// negativan broj kao argument
			if ((this.argument & 0x80000000) != 0) {
				this.assembler = String.format(format + "      ; -%08x", this.argument, neg(this.argument));
			} else {
				List<String> l = CpuContext.symTable.sym.get(this.argument);
				if (l != null && l.size() > 0) {
					String format2 = format.replaceAll("0x", "");
					format2 = format2.replaceAll("04x", "s");
					format2 = format2.replaceAll("08x", "s");
					this.assembler = String.format(format2, l.get(0));
				} else {
					this.assembler = String.format(format, this.argument);
				}
			}
		} else {
			this.assembler = format;
		}
	}

	public static byte fix8(short w) {
		return (byte) w;
	}

	public static int fix(short w) {
		return w & 0xFFFF;
	}

	public static int fix(int w) {
		return w & 0xFFFFFFFF;
	}

	protected void markFlags(long res, int r, CpuContext ctx) {
		// Z flag
		if (r == 0) {
			ctx.f.val |= 1;
		} else {
			ctx.f.val &= 0xfffe;
		}
		// P flag
		if ((r < 0) || ((r & 0x80000000) == 1)) {
			ctx.f.val &= 0xfff7;
		} else {
			ctx.f.val |= 0x8;
		}

		// C flag
		if ((res & 0x100000000L) != 0) {
			ctx.f.val |= 2;
		} else {
			ctx.f.val &= 0xd;
		}
	}

	protected void markOverflow(int a, int b, int res, CpuContext ctx) {
		int sa = sign(a);
		int sb = sign(b);
		int sr = sign(res);
		if (sa == sb) {
			if (sa != sr) {
				ctx.f.val |= 0x4;
				return;
			}
		}
		ctx.f.val &= 0xfffb;
	}

	private int sign(int a) {
		return a & 0x80000000;
	}

	protected void updateViewer(CpuContext ctx, int addr, int content) {
		ctx.engine.updateViewer(addr, content);
	}

	protected void updateViewer32(CpuContext ctx, int addr, int content) {
		ctx.engine.updateViewer32(addr, content);
	}

	private int neg(int arg) {
		return (int) ((0x100000000L - arg) & 0xffffffff);
	}

	public static String getTypeStr(int type) {
		switch (type) {
		case ADD_S:
			return "add.s ";
		case SUB_S:
			return "sub.s ";
		case AND_S:
			return "and.s ";
		case OR_S:
			return "or.s ";
		case XOR_S:
			return "xor.s ";
		case SHL_S:
			return "shl.s ";
		case SHR_S:
			return "shr.s ";
		case MUL_S:
			return "mul.s ";
		case DIV_S:
			return "div.s ";
		case ADD_B:
			return "add.b ";
		case SUB_B:
			return "sub.b ";
		case AND_B:
			return "and.b ";
		case OR_B:
			return "or.b ";
		case XOR_B:
			return "xor.b ";
		case SHL_B:
			return "shl.b ";
		case SHR_B:
			return "shr.b ";
		case MUL_B:
			return "mul.b ";
		case DIV_B:
			return "div.b ";
		case ADD_W:
			return "add.w ";
		case SUB_W:
			return "sub.w ";
		case AND_W:
			return "and.w ";
		case OR_W:
			return "or.w ";
		case XOR_W:
			return "xor.w ";
		case SHL_W:
			return "shl.w ";
		case SHR_W:
			return "shr.w ";
		case MUL_W:
			return "mul.w ";
		case DIV_W:
			return "div.w ";
		case FADD:
			return "fadd ";
		case FSUB:
			return "fsub ";
		case FMUL:
			return "fmul ";
		case FDIV:
			return "fdiv ";
		default:
			return "UNKNOWN";
		}
	}

	private int fixInt(short w1, short w2) {
		int i1 = w1;
		if (w1 < 0) {
			i1 = w1 & 0xFFFF;
		}
		int i2 = w2;
		if (w2 < 0) {
			i2 = w2 & 0xFFFF;
		}
		return (i1 << 16) | i2;
	}

	public int getMemContent(CpuContext ctx, int addr) {
		short w1 = (short) (ctx.memory[addr]);
		short w2 = (short) (ctx.memory[addr + 1]);
		return fixInt(w1, w2);
	}

	public void setMemContent(CpuContext ctx, int addr, int val) {
		ctx.memory[addr] = (short) (val >> 16);
		ctx.memory[addr + 1] = (short) (val & 0xFFFF);
	}

	protected int pop(CpuContext ctx, int addr) {
		short c1 = ctx.memory[addr];
		short c2 = ctx.memory[addr + 1];
		return fixInt(c1, c2);
	}

	protected void push(CpuContext ctx, int addr, int content) {
		short w1 = (short) (content >> 16);
		ctx.memory[addr] = w1;
		short w2 = (short) (content & 0xFFFF);
		ctx.memory[addr + 1] = w2;
	}

}
