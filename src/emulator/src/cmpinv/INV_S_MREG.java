package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INV_S_MREG extends Instruction {
	public INV_S_MREG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("inv.s [" + this.sdest + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		long res = ~ctx.memory[fix(ctx.getReg(this.dest).val) / 2];
		ctx.memory[fix(ctx.getReg(this.dest).val) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), (int)res);
	}
}
