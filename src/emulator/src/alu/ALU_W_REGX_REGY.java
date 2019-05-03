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
		case ADD_W: res = ctx.getReg(this.dest).val + ctx.getReg(this.src).val; break;
		case SUB_W: res = ctx.getReg(this.dest).val - ctx.getReg(this.src).val; break;
		case AND_W: res = ctx.getReg(this.dest).val & ctx.getReg(this.src).val; break;
		case OR_W : res = ctx.getReg(this.dest).val | ctx.getReg(this.src).val; break;
		case XOR_W: res = ctx.getReg(this.dest).val ^ ctx.getReg(this.src).val; break;
		case SHL_W: res = ctx.getReg(this.dest).val << ctx.getReg(this.src).val; break;
		case SHR_W: res = ctx.getReg(this.dest).val >>> ctx.getReg(this.src).val; break;
		case MUL_W:	res = ctx.getReg(this.dest).val * ctx.getReg(this.src).val; 
					ctx.h.val = (short)((res & 0xffff0000) >> 16);
					break;
		case DIV_W: 	res = ctx.getReg(this.dest).val / ctx.getReg(this.src).val; 
					ctx.h.val = (short)(ctx.getReg(this.dest).val % ctx.getReg(this.src).val);
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, ctx.getReg(this.src).val, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 2;
	}
}
