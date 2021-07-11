package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_MXX_REG extends Instruction {
	public ST_MXX_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("st [0x%08x]" + ", " + this.ssrc);
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.memory[fix(this.argument) / 2] = (short) ctx.getReg(this.src).val;
		
		ctx.pc.val += 6;
		updateViewer(ctx, fix(this.argument), ctx.getReg(this.src).val);
	}
}
