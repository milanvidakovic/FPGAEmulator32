package emulator.src.floatingpoint;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class BLIT extends Instruction {
	public BLIT(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("blit ");
	}

	@Override
	public void exec(CpuContext ctx) {
		int _src = ctx.getReg(2).val;
		int _dest = ctx.getReg(1).val;
		int _len = ctx.getReg(3).val;
		for (int i = 0; i < _len; i+=2) {
			short content = ctx.memory[(_src + i) / 2];
			ctx.memory[(_dest + i)/2] = content;
			updateViewer(ctx, _dest + i, content);
		}
		ctx.pc.val += 2;
	}
}
