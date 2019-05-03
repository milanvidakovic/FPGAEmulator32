package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class PUSH_XX extends Instruction {
	public PUSH_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("push 0x%08x");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.sp.val -= 4;
		int v = fix(ctx.sp.val);
		push(ctx, v/2, this.argument);
		ctx.pc.val += 6;
		updateViewer32(ctx, v, this.argument);
	}
}
