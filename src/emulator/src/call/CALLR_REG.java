package emulator.src.call;

import emulator.engine.CpuContext;

public class CALLR_REG extends CALL_XX {
	public CALLR_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setAssembler("callr " + this.ssrc);
		super.isJump = true;
		this.arglen = 2;
		this.hasArgument = false;
	}
	
	@Override
	public void exec(CpuContext ctx) {
		// This is is the address where the CPU would jump. 
		// Maybe the address has not been disassembled yet.
		ctx.mdl.disassm(ctx.getReg(this.src).val);
		
		
		ctx.sp.val -= 4;
		push(ctx, fix(ctx.sp.val)/2, ctx.pc.val + 2);
		
		updateViewer32(ctx, fix(ctx.sp.val), (ctx.pc.val + 2));
		ctx.pc.val = ctx.getReg(this.src).val;
	}
}
