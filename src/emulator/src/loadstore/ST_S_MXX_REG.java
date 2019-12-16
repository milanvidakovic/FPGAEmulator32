package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_S_MXX_REG extends Instruction {
	public ST_S_MXX_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("st.s [0x%08x]" + ", " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		setMemContent(ctx, fix(this.argument) / 2, (short) ctx.getReg(this.src).val, fix(this.argument));
		
		ctx.pc.val += 6;
		updateViewer(ctx, fix(this.argument), ctx.getReg(this.src).val);
	}
}
