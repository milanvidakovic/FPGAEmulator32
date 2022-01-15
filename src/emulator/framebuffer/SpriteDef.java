package emulator.framebuffer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import emulator.engine.CpuContext;

public class SpriteDef {
	public int x;
	public int y;
	public Color transparentColor;
	public Image img;
	public int spriteAddr;
	Graphics2D gr;
	Color[][] buff;
	
	public SpriteDef() {
		buff = new Color[16][16];
	}

	public void fillBuff(CpuContext ctx, IFBViewer fb) {
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				int addr = this.spriteAddr + i * 8 + j * 2;
				short content = ctx.memory[addr / 2];
				buff[j*4 + 0][i] = fb.getColor((int) ((content >> 12) & 7));
				buff[j*4 + 1][i] = fb.getColor((int) ((content >> 8) & 7));
				buff[j*4 + 2][i] = fb.getColor((int) ((content >> 4) & 7));
				buff[j*4 + 3][i] = fb.getColor((int) ((content) & 7));
			}
		}		
	}
}
