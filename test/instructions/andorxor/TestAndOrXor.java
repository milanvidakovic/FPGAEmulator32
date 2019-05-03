package instructions.andorxor;

import org.junit.Test;

import emulator.engine.CpuContext;
import emulator.src.Instruction;
import emulator.src.alu.ALU_W_REGX_REGY;
import emulator.src.cmpinv.INV_W_REG;


public class TestAndOrXor {

	@Test
	public void testAND_A_B() {
		CpuContext ctx = new CpuContext();
		int src = 0, dest = 1;
		ALU_W_REGX_REGY n = new ALU_W_REGX_REGY(new short[2], 1, src, dest, Instruction.AND_S);
		ctx.getReg(src).val = 1;
		ctx.getReg(dest).val = 2;
		n.exec(ctx);
		assert (ctx.getReg(dest).val == 0);
		assert ((ctx.f.val & 8) != 0);
		assert ((ctx.f.val & 1) == 1);
		ctx.getReg(src).val = 1;
		ctx.getReg(dest).val = 3;
		n.exec(ctx);
		assert (ctx.getReg(dest).val == 1);
		assert ((ctx.f.val & 8) != 0);
		assert ((ctx.f.val & 1) == 0);
		ctx.getReg(src).val = -1;
		ctx.getReg(dest).val = (short) 0xffff;
		n.exec(ctx);
		assert (ctx.getReg(dest).val == -1);
		assert ((ctx.f.val & 8) == 0);
		assert ((ctx.f.val & 1) == 1);

	}
	
	@Test
	public void testNEG_A() {
		CpuContext ctx = new CpuContext();
		int src = 0;
		INV_W_REG n = new INV_W_REG(new short[2], 1, src, 0);
		ctx.getReg(src).val = 1;
		n.exec(ctx);
		assert (ctx.getReg(src).val == (short)-2);
		assert ((ctx.f.val & 8) == 0);
		n.exec(ctx);
		assert (ctx.getReg(src).val == 1);
		assert ((ctx.f.val & 8) != 0);
	}

}
