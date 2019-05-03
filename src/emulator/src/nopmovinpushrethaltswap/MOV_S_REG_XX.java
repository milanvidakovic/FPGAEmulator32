package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class MOV_S_REG_XX extends Instruction {
	public MOV_S_REG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument();
		super.setAssembler("mov.s " + this.sdest + ", 0x%04x");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = this.argument;
		ctx.pc.val += 4;
	}
}
