package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_B_REGX_MREGY extends Instruction {
	public LD_B_REGX_MREGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("ld.b " + this.sdest + ", [" + this.ssrc + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int fixedAddr = fix(ctx.getReg(this.src).val);
		if ((fixedAddr & 1) == 0)
			ctx.getReg(this.dest).val = (short)(ctx.memory[fixedAddr / 2] >> 8) & 0xFF;
		else
			ctx.getReg(this.dest).val = (short)(ctx.memory[fixedAddr / 2] & 255) & 0xFF;
		ctx.pc.val += 2;
	}
}
