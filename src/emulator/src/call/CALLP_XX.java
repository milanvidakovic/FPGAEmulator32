package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLP_XX extends CALL_XX {
	public CALLP_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("callp 0x%08x");
		super.isJump = true;
	}

	@Override
	public void exec(CpuContext ctx) {
		if ((ctx.f.val & 0x8) == 1) {
			super.exec(ctx);
		} else {
			ctx.pc.val += 6;
		}
	}
}
