package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INC_W_MREG_XX extends Instruction {
	public INC_W_MREG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		this.setArgument32();
		super.setAssembler("inc.w [" + this.sdest + " + 0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old = getMemContent(ctx, fix(ctx.getReg(this.dest).val + this.argument) / 2);
		long res = old + 1;
		setMemContent(ctx, fix(ctx.getReg(this.dest).val + this.argument) / 2, (int)res);
		markFlags(res, (int)res, ctx);
		markOverflow(old, 1, (int)res, ctx);

		ctx.pc.val += 6;
		updateViewer32(ctx, fix(ctx.getReg(this.dest).val + this.argument), (int)res);
	}
}
