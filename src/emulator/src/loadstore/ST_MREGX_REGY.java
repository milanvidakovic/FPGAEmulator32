package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_MREGX_REGY extends Instruction {
	public ST_MREGX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("st [" + this.sdest + "], " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.memory[fix(ctx.getReg(this.dest).val) / 2] = (short) ctx.getReg(this.src).val;
		
		ctx.pc.val += 2;
		updateViewer(ctx, fix(ctx.getReg(this.dest).val), ctx.getReg(this.src).val);
	}
}
