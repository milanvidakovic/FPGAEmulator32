package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ALU_W_REGX_REGY extends Instruction {
	int type;
	
	public ALU_W_REGX_REGY(short[] memory, int addr, int src, int dest, int type) {
		super(memory, addr, src, dest);
		super.setAssembler(Instruction.getTypeStr(type) + this.sdest + ", " + this.ssrc);
		this.type = type;
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = 0;
		switch (type) {
		case ADD_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) + (ctx.getReg(this.src).val & 0xffffffffL); break;
		case SUB_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) + (-ctx.getReg(this.src).val & 0xffffffffL); break;
		case AND_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) & (ctx.getReg(this.src).val & 0xffffffffL); break;
		case OR_W : res = (ctx.getReg(this.dest).val & 0xffffffffL) | (ctx.getReg(this.src).val & 0xffffffffL); break;
		case XOR_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) ^ (ctx.getReg(this.src).val & 0xffffffffL); break;
		case SHL_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) << (ctx.getReg(this.src).val & 0xffffffffL); break;
		case SHR_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) >>> (ctx.getReg(this.src).val & 0xffffffffL); break;
		case MUL_W:	res = (ctx.getReg(this.dest).val & 0xffffffffL) * (ctx.getReg(this.src).val & 0xffffffffL); 
					ctx.h.val = (int)((res & 0xffffffff00000000L) >> 32);
					break;
		case DIV_W: res = ctx.getReg(this.dest).val / ctx.getReg(this.src).val; 
					ctx.h.val = ctx.getReg(this.dest).val  % ctx.getReg(this.src).val ;
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, ctx.getReg(this.src).val, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 2;
	}
}
