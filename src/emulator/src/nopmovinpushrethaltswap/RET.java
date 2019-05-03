package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class RET extends Instruction {
	public RET(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setAssembler("ret");
	}

	@Override
	public void exec(CpuContext ctx) {
		int addr = pop(ctx, fix(ctx.sp.val) / 2);
		ctx.sp.val += 4;
		ctx.pc.val = addr;
	}

}
