package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class IRQ extends Instruction {
	public IRQ(short[]memory, int addr, int dest, int src) {
		super(memory, addr, src, dest);
		this.assembler = String.format("IRQ %d, %d", dest, src);
		//super.setContent();
	}
	
	@Override
	public void exec(CpuContext ctx) {
		if (this.src == 1) {
			ctx.IRQ_MASK |= 1 << this.dest;
		} else {
			ctx.IRQ_MASK &= ~(1 << this.dest);
		}
		ctx.pc.val += 2;
	}
}
