package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_MREGX_XX_REGY extends Instruction {
	public ST_MREGX_XX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("st [" + this.sdest + " + 0x%08x], " + this.ssrc);
	}
	@Override
	public void exec(CpuContext ctx) {
		ctx.memory[fix(ctx.getReg(this.dest).val  +  this.argument) / 2] = (short) ctx.getReg(this.src).val;

		ctx.pc.val += 6;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val + this.argument), ctx.getReg(this.src).val);
	}
}
