package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLC_XX extends CALL_XX {
	public CALLC_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("callc 0x%08x");
		super.isJump = true;
	}
	
	@Override
	public void exec(CpuContext ctx) {
		if ((ctx.f.val & 0x2) == 1) {
			super.exec(ctx);
		}  else {
			ctx.pc.val += 6;
		}
	}
}
