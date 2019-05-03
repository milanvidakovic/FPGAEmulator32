package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_B_REGX_MREGY_XX extends Instruction {
	public LD_B_REGX_MREGY_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument();
		super.setAssembler("ld " + this.sdest + ", [" + this.ssrc + " + 0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int fixedAddr = fix(ctx.getReg(this.src).val + this.argument);
		if ((fixedAddr & 1) == 0)
			ctx.getReg(this.dest).val = (short) (ctx.memory[fixedAddr / 2] >> 8) & 0xFFFF;
		else
			ctx.getReg(this.dest).val = (short) (ctx.memory[fixedAddr / 2] & 255) & 0xFFFF;
		ctx.pc.val += 6;
	}
}
