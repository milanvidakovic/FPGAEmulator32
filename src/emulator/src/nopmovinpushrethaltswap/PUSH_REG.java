package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class PUSH_REG extends Instruction {
	public PUSH_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("push " + this.sdest);
	}
	
	@Override
	public void exec(CpuContext ctx) {
		ctx.sp.val -= 4;
		int v = fix(ctx.sp.val);
		push(ctx, v/2, ctx.getReg(this.dest).val);
		ctx.pc.val += 2;
		updateViewer32(ctx, v, ctx.getReg(this.dest).val);
	}
}
