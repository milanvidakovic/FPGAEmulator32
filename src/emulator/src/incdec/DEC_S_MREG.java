package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class DEC_S_MREG extends Instruction {
	public DEC_S_MREG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("dec.s [" + this.sdest + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old = ctx.memory[fix(ctx.getReg(this.dest).val) / 2];
		long res = old - 1;
		ctx.memory[fix(ctx.getReg(this.dest).val) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		markOverflow(old, -1, (int)res, ctx);

		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), (int)res);
	}
}
