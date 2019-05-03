package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class SWAP_REGX_REGY extends Instruction {
	public SWAP_REGX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("swap " + this.sdest + ", " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		int t = ctx.getReg(this.dest).val;
		ctx.getReg(this.dest).val = ctx.getReg(this.src).val;
		ctx.getReg(this.src).val = t;
		ctx.pc.val += 2;
	}
}
