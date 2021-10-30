package emulator.src.floatingpoint;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class PIX extends Instruction {
	public PIX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("pix ");
	}

	@Override
	public void exec(CpuContext ctx) {
		int _x = ctx.getReg(0).val; // x coordinate
		int _y = ctx.getReg(1).val; // y coordinate
		short _color = (short) ctx.getReg(2).val; // color (0-7 in 320x240x8, or 0-1 in 640x480x2)
		int _dest = ctx.getReg(3).val; // destination address (1024 for the beginning of the video fb)
		int _w = ctx.getReg(4).val; // width in bytes
		int _h = ctx.getReg(5).val; // height in pixels
		int mode = ctx.engine.getFBViewer().getMode();

		if (!(_x < 0 || _y < 0 || _x >= _w * (mode == 1? 2 : 8) || _y >= _h)) {
			int location, p;
			short xOffset, xOffsetMask, pColor, bgColor;
			if (mode == 1) {
				location = (_x >> 2) * 2 + _y * _w;
				p = _dest + location;
				xOffset = (short) ((_x % 4) * 4);
				xOffsetMask = (short) (0xF000 >> xOffset);
				pColor = (short) ((_color << (12 - xOffset)) & xOffsetMask);
			} else {
				location = (_x >> 4) * 2 + _y * _w;
				p = _dest + location;
				xOffset = (short) (_x % 16);
				xOffsetMask = (short) (0x8000 >> xOffset);
				pColor = (short) ((_color << (15 - xOffset)) & xOffsetMask);
			}
			bgColor = (short) ((~xOffsetMask) & ctx.memory[(p) / 2]);
			ctx.memory[(p) / 2] = (short) (pColor | bgColor);
			updateViewer(ctx, p, (short) (pColor | bgColor));
		}
		ctx.pc.val += 2;
	}
}
