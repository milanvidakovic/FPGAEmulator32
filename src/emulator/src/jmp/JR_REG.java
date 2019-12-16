package emulator.src.jmp;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class JR_REG extends Instruction {
	public JR_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("jr " + this.ssrc);
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.pc.val = ctx.getReg(this.src).val;
	}
}
