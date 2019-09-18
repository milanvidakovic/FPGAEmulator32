package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_B_REGX_MREGY_XX extends Instruction {
	public CMP_B_REGX_MREGY_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("cmp.b " + this.sdest + ", [" + this.ssrc + " + 0x%08x]");
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		
		int fixedAddr = fix(ctx.getReg(this.src).val + this.argument);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (short)((ctx.memory[fixedAddr / 2] >> 8) & 0xFF);
		else
			operand = (short)((ctx.memory[fixedAddr / 2] & 255) & 0xFF);		
		
		long res = ctx.getReg(this.dest).val  - operand;
		
		markFlags(res, (int)res, ctx);
		markOverflow(old_a, operand, (int)res, ctx);
		ctx.pc.val += 6;
	}
}
