package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class LD_S_REGX_MREGY_XX extends Instruction {
	public LD_S_REGX_MREGY_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("ld.s " + this.sdest + ", [" + this.ssrc + " + 0x%08x]");
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int addr = fix(ctx.getReg(this.src).val + this.argument);
		ctx.getReg(this.dest).val = ctx.memory[addr / 2] & 0xFFFF;
		ctx.pc.val += 6; 
	}
}
