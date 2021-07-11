package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class DEC_B_MXX extends Instruction {
	public DEC_B_MXX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		this.setArgument32();
		super.setAssembler("dec [0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int fixedAddr = fix(this.argument);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (short)((ctx.memory[fixedAddr / 2] >> 8) & 0xFF);
		else
			operand = (short)((ctx.memory[fixedAddr / 2] & 255) & 0xFF);		
		
		long a = (operand & 0xffffffffL);
		long b = (-1 & 0xffffffffL);
		long res = a + b;

		short content = ctx.memory[fixedAddr / 2];
		if ((fixedAddr & 1) == 0) {
			content &= 0x00ff; 
			content |= res << 8;
		} else {
			content &= 0xff00; 
			content |= res & 255;
		}

		ctx.memory[fixedAddr / 2] = content;

		markFlags(res, (short)res, ctx);
		markOverflow(operand, -1, (int)res, ctx);

		ctx.pc.val += 6;
		updateViewer(ctx, fix(this.argument), content);
	}
}
