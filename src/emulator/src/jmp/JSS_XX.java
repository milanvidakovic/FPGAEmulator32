package emulator.src.jmp;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class JSS_XX extends Instruction {
	public JSS_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("jss 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		// (C xor O == true)
		if (L(ctx)) {
			ctx.pc.val = this.argument;
		} else {
			ctx.pc.val += 6;
		}
	}
}
