package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_S_REG_MXX extends Instruction {
	public LD_S_REG_MXX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("ld.s " + this.sdest + ", [0x%08x]");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.getReg(this.dest).val = getMemContent(ctx, this.argument / 2, this.argument);//ctx.memory[this.argument / 2] & 0xFFFF;
		ctx.pc.val += 6;
	}
}
