package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INC_MREG_XX extends Instruction {
	public INC_MREG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		this.setArgument32();
		super.setAssembler("inc [" + this.sdest + " + 0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old = ctx.memory[fix(ctx.getReg(this.dest).val + this.argument) / 2];
		long a = (old & 0xffffffffL);
		long b = 1;
		long res = a + b;

		ctx.memory[fix(ctx.getReg(this.dest).val + this.argument) / 2] = (short)res;
		markFlags(res, (int)res, ctx);
		markOverflow(old, 1, (int)res, ctx);

		ctx.pc.val += 6;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val + this.argument), (int)res);
	}
}
