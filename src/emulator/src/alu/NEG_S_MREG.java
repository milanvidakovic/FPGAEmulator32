package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class NEG_S_MREG extends Instruction {
	public NEG_S_MREG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("neg.s [" + this.sdest + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		long res = -ctx.memory[fix(ctx.getReg(this.dest).val) / 2];
		ctx.memory[fix(ctx.getReg(this.dest).val) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), (int)res);
	}
}
