package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class NEG_B_MREG extends Instruction {
	public NEG_B_MREG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("neg.b [" + this.sdest + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int fixedAddr = fix(ctx.getReg(this.dest).val);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (short) (ctx.memory[fixedAddr / 2] >> 8);
		else
			operand = (short) (ctx.memory[fixedAddr / 2] & 255);		
		
		long res = -operand;

		short content = (short)ctx.memory[fixedAddr / 2];
		if ((fixedAddr & 1) == 0) {
			content &= 0x00ff; 
			content |= res << 8;
		} else {
			content &= 0xff00; 
			content |= res & 255;
		}

		ctx.memory[fixedAddr / 2] = content;

		markFlags(res, (int)res, ctx);
		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), content);
	}
}
