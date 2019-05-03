package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CMP_S_REG_MXX extends Instruction {
	public CMP_S_REG_MXX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("cmp.s " + this.sdest + ", [0x%08x]");
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = ctx.getReg(this.dest).val - ctx.memory[fix(this.argument) / 2];
		markFlags(res, (short)res, ctx);
		markOverflow(old_a, ctx.memory[fix(this.argument) / 2], (int)res, ctx);
		ctx.pc.val += 6;
	}
}
