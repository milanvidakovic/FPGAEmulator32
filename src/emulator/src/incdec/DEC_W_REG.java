package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class DEC_W_REG extends Instruction {
	public DEC_W_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("dec.w " + this.sdest);
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old = ctx.getReg(this.dest).val;
		long a = (old & 0xffffffffL);
		long b = (-1 & 0xffffffffL);
		long res = a + b;
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, (int)res, ctx);
		markOverflow(old, -1, (int)res, ctx);

		ctx.pc.val += 2;
	}
}
