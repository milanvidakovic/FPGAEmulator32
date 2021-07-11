package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_S_REGX_MREGY extends Instruction {
	public CMP_S_REGX_MREGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("cmp.s " + this.sdest + ", [" + this.ssrc + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long a = (ctx.getReg(this.dest).val & 0xffffffffL);
		long b = (-ctx.memory[fix(ctx.getReg(this.src).val) / 2] & 0xffffffffL); 
		long res = a + b;
		markFlags(res, (int)res, ctx);
		markOverflow(old_a, ctx.memory[fix(ctx.getReg(this.src).val) / 2], (int)res, ctx);
		ctx.pc.val += 2;
	}
}
