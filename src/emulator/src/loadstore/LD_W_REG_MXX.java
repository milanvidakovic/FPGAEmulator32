package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_W_REG_MXX extends Instruction {
	public LD_W_REG_MXX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("ld.w " + this.sdest + ", [0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = getMemContent(ctx, fix(this.argument / 2));
		ctx.pc.val += 6;
	}
}
