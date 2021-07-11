package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_S_REG_XX extends Instruction {
	public CMP_S_REG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument();
		super.setAssembler("cmp.s " + this.sdest + ", 0x%04x");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long a = (ctx.getReg(this.dest).val & 0xffffffffL);
		long b = (-this.argument & 0xffffffffL);
		long res = a + b;
		markFlags(res,(int)res, ctx);
		markOverflow(old_a, this.argument, (int)res, ctx);
		ctx.pc.val += 4;
	}
}
