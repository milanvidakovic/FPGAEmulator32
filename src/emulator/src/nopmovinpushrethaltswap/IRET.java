package emulator.src.nopmovinpushrethaltswap;

import emulator.engine.CpuContext;
import emulator.engine.Engine;
import emulator.raspbootin.Raspbootin64Client;
import emulator.src.Instruction;

public class IRET extends Instruction {
	public IRET(short[] memory, int addr, int src, int dest) {
		super(memory, addr, 0, 0);
		super.setAssembler("iret");
	}

	@Override
	public void exec(CpuContext ctx) {
		ctx.pc.val = pop(ctx, fix(ctx.sp.val) / 2);
		ctx.sp.val += 4;
		ctx.f.val = ctx.memory[fix(ctx.sp.val) / 2];
		ctx.sp.val += 2;
		Engine.irq0 = false;
		Engine.irq2_pressed = false;
		Engine.irq2_released = false;
		Engine.inIrq = false;
		ctx.engine.unblock();
//		synchronized (Raspbootin64Client.sync) {Raspbootin64Client.sync.notify();}
		Raspbootin64Client.unblock();
	}
}
