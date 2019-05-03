package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class IN_REG_XX extends Instruction {
	public IN_REG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument();
		this.argument = memory[(this.addr + 2) / 2];
		super.setAssembler("in " + this.sdest + ", [0x%04x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = ctx.fromPort(this.argument);
		ctx.pc.val += 4;
	}
}
