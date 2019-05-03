package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLSE_XX extends CALL_XX {
	public CALLSE_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("callse 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		if (((ctx.f.val & 0x8) == 0) || ((ctx.f.val & 0x1) == 1)) {
			super.exec(ctx);
		}
	}
}
