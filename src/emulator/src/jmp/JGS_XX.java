package emulator.src.jmp;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class JGS_XX extends Instruction {
	public JGS_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("jgs 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		// (C xor O == false) && (Z == false)
		if ((!L(ctx)) && (!Z(ctx))) {
			ctx.pc.val = this.argument;
		} else {
			ctx.pc.val += 6;
		}
	}
}
