package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ALU_S_REGX_MREGY_XX extends Instruction {
	int type;
	
	public ALU_S_REGX_MREGY_XX(short[] memory, int addr, int src, int dest, int type) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler(Instruction.getTypeStr(type) + this.sdest + ", [" + this.ssrc + " + 0x%08x]");
		this.type = type;
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = 0;
		switch (type) {
		case ADD_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) + (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case SUB_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) + (-ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case AND_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) & (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case OR_S : res = (ctx.getReg(this.dest).val & 0xffffffffL) | (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case XOR_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) ^ (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case SHL_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) << (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case SHR_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) >>> (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); break;
		case MUL_S:	res = (ctx.getReg(this.dest).val & 0xffffffffL) * (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); 
					ctx.h.val = (int)((res & 0xffffffff00000000L) >> 32);
					break;
		case DIV_S: res = (ctx.getReg(this.dest).val & 0xffffffffL) / (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL); 
					ctx.h.val = (int) ((ctx.getReg(this.dest).val & 0xffffffffL) % (ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2] & 0xffffffffL));
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, ctx.memory[fix(ctx.getReg(this.src).val + this.argument) / 2], ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 6;
	}
}
