package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ALU_S_REG_XX extends Instruction {
	int type;

	public ALU_S_REG_XX(short[] memory, int addr, int src, int dest, int type) {
		super(memory, addr, src, dest);
		super.setArgument();
		super.setAssembler(Instruction.getTypeStr(type) + this.sdest + ", 0x%04x");
		this.type = type;
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = 0;
		switch (type) {
		case ADD_S: res = ctx.getReg(this.dest).val + this.argument; break;
		case SUB_S: res = ctx.getReg(this.dest).val - this.argument; break;
		case AND_S: res = ctx.getReg(this.dest).val & this.argument; break;
		case OR_S : res = ctx.getReg(this.dest).val | this.argument; break;
		case XOR_S: res = ctx.getReg(this.dest).val ^ this.argument; break;
		case SHL_S: res = ctx.getReg(this.dest).val << this.argument; break;
		case SHR_S: res = ctx.getReg(this.dest).val >>> this.argument; break;
		case MUL_S:	res = ctx.getReg(this.dest).val * this.argument; 
					ctx.h.val = (int)((res & 0xffffffff00000000L) >> 32);
					break;
		case DIV_S: 	res = ctx.getReg(this.dest).val / this.argument; 
					ctx.h.val = (int)(ctx.getReg(this.dest).val % this.argument);
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, this.argument, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 4;
	}
}
