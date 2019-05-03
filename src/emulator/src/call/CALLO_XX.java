package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLO_XX extends CALL_XX {
	public CALLO_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("callo 0x%08x");
		super.isJump = true;
	}
	
	@Override
	public void exec(CpuContext ctx) {
		if ((ctx.f.val & 0x4) == 1) {
			super.exec(ctx);
		}
	}
}
