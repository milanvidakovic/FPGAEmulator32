package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_W_MREGX_XX_REGY extends Instruction {
	public ST_W_MREGX_XX_REGY(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("st.w [" + this.sdest + " + 0x%08x], " + this.ssrc);
	}
	@Override
	public void exec(CpuContext ctx) {
		setMemContent(ctx, (fix(ctx.getReg(this.dest).val  +  this.argument)) / 2, ctx.getReg(this.src).val, fix(ctx.getReg(this.dest).val  +  this.argument));

		ctx.pc.val += 6;
		updateViewer32(ctx, fix(ctx.getReg(this.dest).val + this.argument), ctx.getReg(this.src).val);
	}
}
