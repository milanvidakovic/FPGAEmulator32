package emulator.src.jmp;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class JG_XX extends Instruction {
	public JG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("jg 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		if (((ctx.f.val & 0x8) != 0) && ((ctx.f.val & 0x1) == 0)) {
			ctx.pc.val = this.argument;
		} else {
			ctx.pc.val += 6;
		}
	}
}
