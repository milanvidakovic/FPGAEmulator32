package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ALU_W_REGX_MREGY_XX extends Instruction {
	int type;
	
	public ALU_W_REGX_MREGY_XX(short[] memory, int addr, int src, int dest, int type) {
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
		case ADD_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) + (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case SUB_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) + (-getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case AND_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) & (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case OR_W : res = (ctx.getReg(this.dest).val & 0xffffffffL) | (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case XOR_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) ^ (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case SHL_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) << (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case SHR_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) >>> (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); break;
		case MUL_W:	res = (ctx.getReg(this.dest).val & 0xffffffffL) * (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); 
					ctx.h.val = (int)((res & 0xffffffff00000000L) >> 32);
					break;
		case DIV_W: res = (ctx.getReg(this.dest).val & 0xffffffffL) / (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)) & 0xffffffffL); 
					ctx.h.val = (int)((ctx.getReg(this.dest).val & 0xffffffffL) % (getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument))) & 0xffffffffL);
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)), ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 6;
	}
}
