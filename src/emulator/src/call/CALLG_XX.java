package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLG_XX extends CALL_XX {
	public CALLG_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("callp 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		if (((ctx.f.val & 0x8) == 1) && ((ctx.f.val & 0x1) == 0)) {
			super.exec(ctx);
		}
	}
}
