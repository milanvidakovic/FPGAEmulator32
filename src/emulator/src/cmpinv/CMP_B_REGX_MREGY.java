package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_B_REGX_MREGY extends Instruction {
	public CMP_B_REGX_MREGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("cmp " + this.sdest + ", [" + this.ssrc + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;

		int fixedAddr = fix(ctx.getReg(this.src).val);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (short)((ctx.memory[fixedAddr / 2] >> 8) & 0xFF);
		else
			operand = (short)((ctx.memory[fixedAddr / 2] & 255) & 0xFF);		
		
		long a = (ctx.getReg(this.dest).val & 0xffffffffL);
		long b = (-operand & 0xffffffffL);
		long res = a + b;
		
		markFlags(res, (short)res, ctx);
		markOverflow(old_a, ctx.memory[fix(ctx.getReg(this.src).val) / 2], (int)res, ctx);
		ctx.pc.val += 2;
	}
}
