package emulator.src.call;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class CALL_XX extends Instruction {
	public CALL_XX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setArgument32();
		super.setAssembler("call 0x%08x");
		super.isJump = true;
	}
	
	@Override
	public void exec(CpuContext ctx) {
		ctx.sp.val -= 4;
		push(ctx, fix(ctx.sp.val)/2, ctx.pc.val + 6);
		
		updateViewer32(ctx, fix(ctx.sp.val), (ctx.pc.val + 6));
		ctx.pc.val = this.argument;
	}
}
