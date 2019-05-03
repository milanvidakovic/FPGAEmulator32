package emulator.src.jmp;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class JSE_XX extends Instruction {
	public JSE_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("jse 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		if (((ctx.f.val & 0x8) == 0) || ((ctx.f.val & 0x1) == 1)) {
			ctx.pc.val = this.argument;
		} else {
			ctx.pc.val += 6;
		}
	}
}
