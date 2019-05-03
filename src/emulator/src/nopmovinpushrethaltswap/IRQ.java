package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class IRQ extends Instruction {
	public IRQ(short[]memory, int addr) {
		super(memory, addr, 0, 0);
		this.assembler = "IRQ";
		super.setContent();
	}
	
	@Override
	public void exec(CpuContext ctx) {
		ctx.pc.val += 2;
	}
}
