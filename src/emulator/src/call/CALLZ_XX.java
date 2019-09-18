package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLZ_XX extends CALL_XX {
	public CALLZ_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("callz 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		if ((ctx.f.val & 1) == 1) {
			super.exec(ctx);
		} else {
			ctx.pc.val += 6;
		}
	}
}
