package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class MOV_W_REG_XX extends Instruction {
	public MOV_W_REG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("mov.w " + this.sdest + ", 0x%08x");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = this.argument;
		ctx.pc.val += 6;
	}
}
