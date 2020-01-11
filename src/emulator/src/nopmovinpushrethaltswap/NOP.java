package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class NOP extends Instruction {
	public NOP(short[]memory, int addr) {
		super(memory, addr, 0, 0);
		this.setAssembler("nop");
		super.setContent();
	}
	
	@Override
	public void exec(CpuContext ctx) {
		ctx.pc.val += 2;
	}
}
