package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class DEC_S_MXX extends Instruction {
	public DEC_S_MXX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		this.setArgument32();
		super.setAssembler("dec.s [0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old = ctx.memory[fix(this.argument) / 2];
		long a = (old & 0xffffffffL);
		long b = (-1 & 0xffffffffL);
		long res = a + b;
		ctx.memory[fix(this.argument) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		markOverflow(old, -1, (int)res, ctx);

		ctx.pc.val += 6;
		updateViewer(ctx, fix(this.argument), (int)res);
	}
}
