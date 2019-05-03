package emulator.src.cmpinv;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class INV_W_REG extends Instruction {
	public INV_W_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("inv.w " + this.sdest);
	}
	
	@Override
	public void exec(CpuContext ctx) {
		long res = ~ctx.getReg(this.dest).val;
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 2;
	}
}
