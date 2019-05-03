package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INV_S_MREG_XX extends Instruction {
	public INV_S_MREG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		this.setArgument32();
		super.setAssembler("inv.s [" + this.sdest + " + 0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		long res = ~ctx.memory[fix(ctx.getReg(this.dest).val + this.argument) / 2];
		ctx.memory[fix(ctx.getReg(this.dest).val + this.argument) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		ctx.pc.val += 6;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val + this.argument), (int)res);
	}
}
