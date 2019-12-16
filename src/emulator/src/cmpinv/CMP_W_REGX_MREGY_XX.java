package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_W_REGX_MREGY_XX extends Instruction {
	public CMP_W_REGX_MREGY_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("cmp.w " + this.sdest + ", [" + this.ssrc + " + 0x%08x]");
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = ctx.getReg(this.dest).val - getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument));
		markFlags(res, (int)res, ctx);
		markOverflow(old_a, getMemContent(ctx, fix(ctx.getReg(this.src).val + this.argument) / 2, fix(ctx.getReg(this.src).val + this.argument)), (int)res, ctx);
		ctx.pc.val += 6;
	}
}
