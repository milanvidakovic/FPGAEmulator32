package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_S_REGX_MREGY extends Instruction {
	public LD_S_REGX_MREGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("ld.s " + this.sdest + ", [" + this.ssrc + "]");
	}

	@Override
	public void exec(CpuContext ctx) {
		int address = ctx.getReg(this.src).val;
		int content = getMemContent(ctx, address / 2, address);
		ctx.getReg(this.dest).val = (content >> 16) & 0xFFFF;
		ctx.pc.val += 2;
	}
}
