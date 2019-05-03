package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_W_REG_XX extends Instruction {
	public CMP_W_REG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("cmp.w " + this.sdest + ", 0x%08x");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = ctx.getReg(this.dest).val - this.argument;
		markFlags(res,(int)res, ctx);
		markOverflow(old_a, this.argument, (int)res, ctx);
		ctx.pc.val += 6;
	}
}
