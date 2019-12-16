package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_W_REGX_MREGY extends Instruction {
	public LD_W_REGX_MREGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("ld.w " + this.sdest + ", [" + this.ssrc + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = getMemContent(ctx, fix(ctx.getReg(this.src).val) / 2, fix(ctx.getReg(this.src).val));
		ctx.pc.val += 2;
	}
}
