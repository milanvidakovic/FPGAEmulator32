package emulator.framebuffer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class SpriteDef {
	public int x;
	public int y;
	public Color transparentColor;
	public Image img;
	Graphics2D gr;
	public int spriteAddr;
	
	public SpriteDef() {
		img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		gr = ((BufferedImage)img).createGraphics();
	}
}
