package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class MOV_W_REGX_REGY_XX extends Instruction {
	public MOV_W_REGX_REGY_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("mov.w " + this.sdest + ", " + this.ssrc + " 0x%08x");
	}

	@Override
	public void exec(CpuContext ctx) {
		System.out.println("src: " + this.src);
		System.out.println("src.val: " + ctx.getReg(this.src).val);
		System.out.println("dest: " + ctx.getReg(this.dest).val);
		ctx.getReg(this.dest).val = ctx.getReg(this.src).val + this.argument;
		System.out.println("dest: " + ctx.getReg(this.dest).val);
		ctx.pc.val += 6;
	}
}
