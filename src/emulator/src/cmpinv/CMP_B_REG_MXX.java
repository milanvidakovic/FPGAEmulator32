package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_B_REG_MXX extends Instruction {
	public CMP_B_REG_MXX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("cmp" + this.sdest + ", [0x%08x]");
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;

		int fixedAddr = fix(this.argument);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (short)((ctx.memory[fixedAddr / 2] >> 8) & 0xFF);
		else
			operand = (short)((ctx.memory[fixedAddr / 2] & 255) & 0xFF);		
		
		long a = (ctx.getReg(this.dest).val & 0xffffffffL);
		long b = (-operand & 0xffffffffL);
		long res = a + b;
		
		markFlags(res, (int)res, ctx);
		markOverflow(old_a, ctx.memory[fix(this.argument) / 2], (int)res, ctx);
		ctx.pc.val += 6;
	}
}
