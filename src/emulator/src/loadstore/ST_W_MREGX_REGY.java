package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_W_MREGX_REGY extends Instruction {
	public ST_W_MREGX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("st.w [" + this.sdest + "], " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		setMemContent(ctx, fix(ctx.getReg(this.dest).val) / 2, ctx.getReg(this.src).val, fix(ctx.getReg(this.dest).val));
		
		ctx.pc.val += 2;
		updateViewer32(ctx, fix(ctx.getReg(this.dest).val), ctx.getReg(this.src).val);
	}
}
