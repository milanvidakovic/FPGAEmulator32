package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class POP_REG extends Instruction {
	public POP_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("pop " + this.sdest);
	}

	@Override
	public void exec(CpuContext ctx) {
		int v = fix(ctx.sp.val);
		ctx.getReg(this.dest).val = pop(ctx, v / 2);
		ctx.sp.val += 4;
		ctx.pc.val += 2;
	}
}
