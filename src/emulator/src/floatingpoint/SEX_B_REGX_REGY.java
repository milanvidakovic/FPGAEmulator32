package emulator.src.floatingpoint;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class SEX_B_REGX_REGY extends Instruction {
	public SEX_B_REGX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("sex.b " + this.sdest + ", " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		byte b = (byte)ctx.getReg(this.src).val;
//		if (b > 0)
			ctx.getReg(this.dest).val = b;
//		else
//			ctx.getReg(this.dest).val = ctx.getReg(this.src).val - 256;
		ctx.pc.val += 2;
	}
}
