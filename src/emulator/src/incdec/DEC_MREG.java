package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class DEC_MREG extends Instruction {
	public DEC_MREG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("dec [" + this.sdest + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old = ctx.memory[fix(ctx.getReg(this.dest).val) / 2];
		long a = (old & 0xffffffffL);
		long b = (-1 & 0xffffffffL);
		long res = a + b;
		ctx.memory[fix(ctx.getReg(this.dest).val) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		markOverflow(old, -1, (int)res, ctx);

		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), (int)res);
	}
}
