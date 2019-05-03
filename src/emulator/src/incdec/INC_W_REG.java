package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INC_W_REG extends Instruction {
	public INC_W_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("inc.w " + this.sdest);
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old = ctx.getReg(this.dest).val;
		long res = old + 1;
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old, 1, (int)res, ctx);

		ctx.pc.val += 2;
	}
}
