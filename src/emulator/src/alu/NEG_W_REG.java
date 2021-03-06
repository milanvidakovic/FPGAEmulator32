package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class NEG_W_REG extends Instruction {
	public NEG_W_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("neg.w " + this.ssrc);
	}
	
	@Override
	public void exec(CpuContext ctx) {
		long res = -ctx.getReg(this.src).val;
		ctx.getReg(this.src).val = (int)res;
		markFlags(res, ctx.getReg(this.src).val, ctx);
		ctx.pc.val += 2;
	}
}
