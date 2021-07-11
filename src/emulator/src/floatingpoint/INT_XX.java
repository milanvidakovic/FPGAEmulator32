package emulator.src.floatingpoint;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INT_XX extends Instruction {
	public INT_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument();
		super.setAssembler("int 0x%04x");
		super.isJump = true;
	}

	@SuppressWarnings("static-access")
	@Override
	public void exec(CpuContext ctx) {
		// Push flags
		ctx.sp.val -= 2;
		ctx.memory[Instruction.fix(ctx.sp.val) / 2] = (short)(ctx.f.val);
		// Push PC
		ctx.sp.val -= 4;
		ctx.memory[Instruction.fix(ctx.sp.val ) / 2] = (short)((ctx.pc.val + 4) >> 16);
		ctx.memory[Instruction.fix(ctx.sp.val + 2) / 2] = (short)((ctx.pc.val + 4) & 0xFFFF);

		ctx.pc.val = this.argument;
		
		Instruction instr = ctx.mdl.getInstruction(ctx.memory, this.argument);
		instr.setContent();
		ctx.mdl.lines.set(this.argument, instr);
		instr.tableLine = this.argument;
		ctx.mdl.addr_instr[this.argument] = instr;

	}
}
