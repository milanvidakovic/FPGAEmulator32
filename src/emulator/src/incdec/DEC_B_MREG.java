package emulator.src.incdec;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class DEC_B_MREG extends Instruction {
	public DEC_B_MREG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("dec.b [" + this.sdest + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int fixedAddr = fix(ctx.getReg(this.dest).val);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (short)(ctx.memory[fixedAddr / 2] >> 8);
		else
			operand = (short)(ctx.memory[fixedAddr / 2] & 255);		
		
		long res = operand - 1;
		
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

		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), content);
	}
}
