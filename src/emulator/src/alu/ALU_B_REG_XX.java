package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ALU_B_REG_XX extends Instruction {
	int type;

	public ALU_B_REG_XX(short[] memory, int addr, int src, int dest, int type) {
		super(memory, addr, src, dest);
		super.setArgument8();
		super.setAssembler(Instruction.getTypeStr(type) + this.sdest + ", 0x%02x");
		this.type = type;
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = 0;
		switch (type) {
		case ADD_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) + (this.argument & 0xffffffffL)); break;
		case SUB_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) + (-this.argument & 0xffffffffL)); break;
		case AND_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) & (this.argument & 0xffffffffL)); break;
		case OR_B : res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) | (this.argument & 0xffffffffL)); break;
		case XOR_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) ^ (this.argument & 0xffffffffL)); break;
		case SHL_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) << (this.argument & 0xffffffffL)); break;
		case SHR_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) >>> (this.argument & 0xffffffffL)); break;
		case MUL_B:	res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) * (this.argument & 0xffffffffL)); 
					ctx.h.val = (byte)((res & 0xffffffff00000000L) >> 32);
					break;
		case DIV_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) / (this.argument & 0xffffffffL)); 
					ctx.h.val = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) % (this.argument & 0xffffffffL));
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, this.argument, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 4;
	}
}
