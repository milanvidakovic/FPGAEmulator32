package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class MOV_B_REG_XX extends Instruction {
	public MOV_B_REG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument8();
		super.setAssembler("mov.b " + this.sdest + ", 0x%02x");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = (byte)this.argument;
		ctx.pc.val += 4;
	}
}
