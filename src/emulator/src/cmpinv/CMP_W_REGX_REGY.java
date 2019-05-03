package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_W_REGX_REGY extends Instruction {
	public CMP_W_REGX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("cmp.w " + this.sdest + ", " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = ctx.getReg(this.dest).val - ctx.getReg(this.src).val;
		markFlags(res, (int)res, ctx);
		markOverflow(old_a, ctx.getReg(this.src).val, (int)res, ctx);
		ctx.pc.val += 2;
	}
}
