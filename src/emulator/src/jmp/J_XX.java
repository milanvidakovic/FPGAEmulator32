package emulator.src.jmp;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class J_XX extends Instruction {
	public J_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("j 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.pc.val = this.argument;
	}
}
