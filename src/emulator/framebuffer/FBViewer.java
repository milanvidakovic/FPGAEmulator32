package emulator.framebuffer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.lang.reflect.Field;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import emulator.EmulatorMain;
import emulator.engine.CpuContext;
import emulator.engine.Engine;
import emulator.src.Instruction;
import emulator.util.WindowUtil;

public class FBViewer extends JFrame {
	private static final long serialVersionUID = -5500314457803056242L;
	public static final int TEXT_MODE = 0;
	public static final int GRAPHICS_MODE_320_240 = 1;
	public static final int GRAPHICS_MODE_640_480 = 2;
	protected static final int SPRITE_COUNT = 3;
	protected static final int SPRITE_DEF_START = 64;

	public FBModel memMdl;

	BufferedImage img;
	Graphics2D gr;

	private SpriteDef[] spriteDef;

	Font font = new Font(Font.MONOSPACED, Font.BOLD, 15);

	public JLabel display = new JLabel();

	public Color[] backgroundColors, foregroundColors;
	/**
	 * framebuffer mode: 1 - graphics mode, 320x240 pixels; 0 - text mode, 80x60
	 * characters
	 */
	private int mode = TEXT_MODE;
	private boolean inverse = false;
	public static int titleBarHeight = 45;

	public FBViewer(CpuContext ctx, Engine eng) {
		super();
		setTitle("Frame buffer");
		memMdl = new FBModel(ctx);
		this.backgroundColors = new Color[160 * 60];
		this.foregroundColors = new Color[160 * 60];

		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
System.out.println(e.getKeyChar());
				if (ctx.memory[Engine.IRQ2_PRESSED_ADDR/2] == 0)
					return;
				ctx.memory[24] = VkToFpga(e);
				Engine.irq2_pressed = true;
				Engine.irq2_released = false;
				try {
					synchronized (ctx.engine) { ctx.engine.wait(); }
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
//				Thread t = new Thread() {
//					@Override
//					public void run() {
//						while (Engine.irq2_pressed) {
//							try {
//								Thread.sleep(10);
//							} catch (InterruptedException e1) {
//								e1.printStackTrace();
//							}
//						}
						//try {Thread.sleep(10);} catch (InterruptedException e1) {e1.printStackTrace();}
						if (ctx.memory[Engine.IRQ2_RELEASED_ADDR/2] == 0)
							return;
						ctx.memory[24] = VkToFpga(e);
						Engine.irq2_pressed = false;
						Engine.irq2_released = true;
						try {
							synchronized (ctx.engine) { ctx.engine.wait(); }
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
//					}
//				};
//				t.start();
			}
		});

		WindowUtil.setLocation(ctx.engine.main.ini.getInt("FB", "x", 1024), ctx.engine.main.ini.getInt("FB", "y", 100),
				ctx.engine.main.ini.getInt("FB", "width", 400), ctx.engine.main.ini.getInt("FB", "height", 700), this);

		img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		gr = img.createGraphics();
		gr.setFont(this.font);

		spriteDef = new SpriteDef[SPRITE_COUNT];
		for (int i = 0; i < SPRITE_COUNT; i++) {
			spriteDef[i] = new SpriteDef();
		}

		Timer t = new Timer(120, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (FBViewer.this.mode == GRAPHICS_MODE_320_240) {
					for (int i = 0; i < SPRITE_COUNT; i++) {
						if (ctx.memory[(SPRITE_DEF_START + i * 8) / 2] != 0) {
							int oldaddr = spriteDef[i].spriteAddr;
							int oldx = spriteDef[i].x;
							int oldy = spriteDef[i].y;
							spriteDef[i].spriteAddr = Instruction.fix(ctx.memory[(SPRITE_DEF_START + i * 8) / 2]);
							spriteDef[i].x = ctx.memory[(SPRITE_DEF_START + i * 8 + 2) / 2];
							spriteDef[i].y = ctx.memory[(SPRITE_DEF_START + i * 8 + 4) / 2];
							spriteDef[i].transparentColor = getColor(ctx.memory[(SPRITE_DEF_START + i * 8 + 6) / 2]);
							
							if (oldaddr != spriteDef[i].spriteAddr) {
								fillSprite(spriteDef[i], ctx);
								spriteDef[i].img = makeColorTransparent(spriteDef[i].img, spriteDef[i].transparentColor);
							}
							
							if (oldx != spriteDef[i].x || oldy != spriteDef[i].y) {
								if (FBViewer.this.getGraphics() != null)
									FBViewer.this.getGraphics().drawImage(img, 
										oldx * 2 + 8, oldy * 2 + titleBarHeight,
										oldx * 2 + 8 + 32, oldy * 2 + titleBarHeight + 32,
										oldx * 2 + 8, oldy * 2 + titleBarHeight,
										oldx * 2 + 8 + 32, oldy * 2 + titleBarHeight + 32,
										null);
							}
							if (oldaddr != spriteDef[i].spriteAddr || oldx != spriteDef[i].x || oldy != spriteDef[i].y) {
								if (FBViewer.this.getGraphics() != null)
									FBViewer.this.getGraphics().drawImage(spriteDef[i].img,
											spriteDef[i].x * 2 + 8, spriteDef[i].y * 2 + titleBarHeight, null);
							}
						}
					}
				}
			}
		});
		t.start();
		setVisible(true);
	}

	public static Image makeColorTransparent(Image im, final Color color) {
	    ImageFilter filter = new RGBImageFilter() {

	        // the color we are looking for... Alpha bits are set to opaque
	        public int markerRGB = color.getRGB() | 0xFF000000;

	        public final int filterRGB(int x, int y, int rgb) {
	            if ((rgb | 0xFF000000) == markerRGB) {
	                // Mark the alpha bits as zero - transparent
	                return 0x00FFFFFF & rgb;
	            } else {
	                // nothing to do
	                return rgb;
	            }
	        }
	    };

	    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
	    return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	protected void fillSprite(SpriteDef sp, CpuContext ctx) {
		Color p1, p2, p3, p4;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				int addr = sp.spriteAddr + i * 8 + j*2;
				short content = ctx.memory[addr / 2];
				p1 = getColor((short) ((content >> 12) & 7));
				p2 = getColor((short) ((content >> 8) & 7));
				p3 = getColor((short) ((content >> 4) & 7));
				p4 = getColor((short) ((content) & 7));
				Insets pixel = new Insets(i, j * 4, 0, 0);
				drawSpritePixels(sp.gr, p1, p2, p3, p4, pixel);
			}
		}
	}
	
	private void drawSpritePixels(Graphics2D gr, Color p1, Color p2, Color p3, Color p4, Insets pixel) {
		gr.setColor(p1);
		gr.fillRect(pixel.left * 2 , pixel.top * 2, 2, 2);
		gr.setColor(p2);
		gr.fillRect(pixel.left * 2 + 2, pixel.top * 2, 2, 2);
		gr.setColor(p3);
		gr.fillRect(pixel.left * 2 + 4, pixel.top * 2, 2, 2);
		gr.setColor(p4);
		gr.fillRect(pixel.left * 2 + 6, pixel.top * 2, 2, 2);
	}

	final public static Integer getScancodeFromKeyEvent(final KeyEvent keyEvent) {

		Integer ret;
		Field field;

		try {
			field = KeyEvent.class.getDeclaredField("scancode");
		} catch (NoSuchFieldException nsfe) {
			System.err.println(
					"ATTENTION! The KeyEvent object does not have a field named \"scancode\"! (Which is kinda weird.)");
			nsfe.printStackTrace();
			return null;
		}

		try {
			field.setAccessible(true);
		} catch (SecurityException se) {
			System.err.println(
					"ATTENTION! Changing the accessibility of the KeyEvent class' field \"scancode\" caused a security exception!");
			se.printStackTrace();
			return null;
		}

		try {
			ret = (int) field.getLong(keyEvent);
		} catch (IllegalAccessException iae) {
			System.err.println("ATTENTION! It is not allowed to read the field \"scancode\" of the KeyEvent instance!");
			iae.printStackTrace();
			return null;
		}

		return ret;
	}

	protected short VkToFpga(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int extKeyCode = e.getExtendedKeyCode();
		int scanCode = getScancodeFromKeyEvent(e);
		if (EmulatorMain.DEBUG)
			System.out.println("KeyCode: " + keyCode + ", extKeyCode: " + extKeyCode + ", scanCode: " + scanCode
					+ ", event: " + e);

		if (keyCode == 0 && extKeyCode == 0 && scanCode == 41) {
//			case KeyEvent.VK_BACKQUOTE		:	return 96   ; `
			return 96;
		} else if (keyCode == 0 && extKeyCode == 16777569 && scanCode == 26) {
//			case KeyEvent.VK_BRACE_LEFT		:		return 91   ;  [
			return 91;
		} else if (keyCode == 0 && extKeyCode == 16777489 && scanCode == 27) {
//			case KeyEvent.VK_BRACE_RIGHT		:	return 93   ;	]
			return 93;
		} else if (keyCode == 0 && extKeyCode == 16777485 && scanCode == 39) {
//			case KeyEvent.VK_SEMICOLON			:	return 59   ;	;
			return 59;
		} else if (keyCode == 0 && extKeyCode == 16777479 && scanCode == 40) {
//		case KeyEvent.VK_QUOTE					:	return 39   ;	'
			return 39;
		} else if (keyCode == 0 && extKeyCode == 16777598 && scanCode == 43) {
//			case KeyEvent.VK_BACK_SLASH		:		return 92   ;  \
			return 92;
		} else if (keyCode == 45 && extKeyCode == 45 && scanCode == 53) {
//			case KeyEvent.VK_SLASH		:		return 47   ;  /
			return 47;
		} else if (keyCode == KeyEvent.VK_QUOTE && scanCode == 12) {
			return 45;
		}

		if (keyCode == KeyEvent.VK_SHIFT && e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT) {
//		case KeyEvent.VK_LEFT_SHIFT		:		return 501  ;
			return 501;
		} else if (keyCode == KeyEvent.VK_SHIFT && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
//		case KeyEvent.VK_RIGHT_SHIFT		:	return 502  ;
			return 502;
		} else if (keyCode == KeyEvent.VK_ALT && e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT) {
//		case KeyEvent.VK_LEFT_ALT			:		return 401  ;
			return 401;
		} else if (keyCode == KeyEvent.VK_ALT && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
//		case KeyEvent.VK_RIGHT_ALT			:	return 402  ;
			return 402;
		} else if (keyCode == KeyEvent.VK_CONTROL && e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT) {
//		case KeyEvent.VK_LEFT_CONTROL	:		return 601  ;
			return 601;
		} else if (keyCode == KeyEvent.VK_CONTROL && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
//		case KeyEvent.VK_RIGHT_CONTROL	:	return 602  ;
			return 602;
		} else if (keyCode == KeyEvent.VK_WINDOWS && e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT) {
//		case KeyEvent.VK_LEFT_WINDOWS	:		return 1001 ;
			return 1001;
		} else if (keyCode == KeyEvent.VK_WINDOWS && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
//		case KeyEvent.VK_RIGHT_WINDOWS	:	return 1002 ;
			return 1002;
		}

		if (keyCode == KeyEvent.VK_ENTER && extKeyCode == KeyEvent.VK_ENTER
				&& e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) {
//		case KeyEvent.VK_NUMPAD_ENTER 	:	return 5013 ;
			return 5013;
		}

		switch (keyCode) {
		case KeyEvent.VK_0:
			return 48;
		case KeyEvent.VK_1:
			return 49;
		case KeyEvent.VK_2:
			return 50;
		case KeyEvent.VK_3:
			return 51;
		case KeyEvent.VK_4:
			return 52;
		case KeyEvent.VK_5:
			return 53;
		case KeyEvent.VK_6:
			return 54;
		case KeyEvent.VK_7:
			return 55;
		case KeyEvent.VK_8:
			return 56;
		case KeyEvent.VK_9:
			return 57;

		case KeyEvent.VK_SPACE:
			return 32;
		case KeyEvent.VK_A:
			return 65;
		case KeyEvent.VK_B:
			return 66;
		case KeyEvent.VK_C:
			return 67;
		case KeyEvent.VK_D:
			return 68;
		case KeyEvent.VK_E:
			return 69;
		case KeyEvent.VK_F:
			return 70;
		case KeyEvent.VK_G:
			return 71;
		case KeyEvent.VK_H:
			return 72;
		case KeyEvent.VK_I:
			return 73;
		case KeyEvent.VK_J:
			return 74;
		case KeyEvent.VK_K:
			return 75;
		case KeyEvent.VK_L:
			return 76;
		case KeyEvent.VK_M:
			return 77;
		case KeyEvent.VK_N:
			return 78;
		case KeyEvent.VK_O:
			return 79;
		case KeyEvent.VK_P:
			return 80;
		case KeyEvent.VK_Q:
			return 81;
		case KeyEvent.VK_R:
			return 82;
		case KeyEvent.VK_S:
			return 83;
		case KeyEvent.VK_T:
			return 84;
		case KeyEvent.VK_U:
			return 85;
		case KeyEvent.VK_V:
			return 86;
		case KeyEvent.VK_W:
			return 87;
		case KeyEvent.VK_X:
			return 88;
		case KeyEvent.VK_Y:
			return 89;
		case KeyEvent.VK_Z:
			return 90;

		case KeyEvent.VK_BACK_QUOTE:
			return 96;
		case KeyEvent.VK_SLASH:
			return 47;
		case KeyEvent.VK_BACK_SLASH:
			return 92;
		case KeyEvent.VK_OPEN_BRACKET:
			return 91;
		case KeyEvent.VK_CLOSE_BRACKET:
			return 93;
		case KeyEvent.VK_EQUALS:
			return 61;
		case KeyEvent.VK_PLUS:
			return 61;
		case KeyEvent.VK_MINUS:
			return 45;
		case KeyEvent.VK_SEMICOLON:
			return 59;
		case KeyEvent.VK_PERIOD:
			return 46;
		case KeyEvent.VK_COMMA:
			return 44;
		case KeyEvent.VK_LESS:
			return 60;

		case KeyEvent.VK_F1:
			return 301;
		case KeyEvent.VK_F2:
			return 302;
		case KeyEvent.VK_F3:
			return 303;
		case KeyEvent.VK_F4:
			return 304;
		case KeyEvent.VK_F5:
			return 305;
		case KeyEvent.VK_F6:
			return 306;
		case KeyEvent.VK_F7:
			return 307;
		case KeyEvent.VK_F8:
			return 308;
		case KeyEvent.VK_F9:
			return 309;
		case KeyEvent.VK_F10:
			return 310;
		case KeyEvent.VK_F11:
			return 311;
		case KeyEvent.VK_F12:
			return 312;
		case KeyEvent.VK_CAPS_LOCK:
			return 800;
		case KeyEvent.VK_NUM_LOCK:
			return 801;
		case KeyEvent.VK_SCROLL_LOCK:
			return 802;

		case KeyEvent.VK_CONTEXT_MENU:
			return 2000;

		case KeyEvent.VK_TAB:
			return 9;
		case KeyEvent.VK_ENTER:
			return 13;
		case KeyEvent.VK_ESCAPE:
			return 701;
		case KeyEvent.VK_BACK_SPACE:
			return 700;

		case KeyEvent.VK_RIGHT:
			return 4003;
		case KeyEvent.VK_LEFT:
			return 4001;
		case KeyEvent.VK_UP:
			return 4000;
		case KeyEvent.VK_DOWN:
			return 4002;

		case KeyEvent.VK_PAGE_UP:
			return 3002;
		case KeyEvent.VK_PAGE_DOWN:
			return 3005;
		case KeyEvent.VK_HOME:
			return 3001;
		case KeyEvent.VK_END:
			return 3004;
		case KeyEvent.VK_INSERT:
			return 3000;
		case KeyEvent.VK_DELETE:
			return 3003;

		case KeyEvent.VK_NUMPAD0:
			return 5048;
		case KeyEvent.VK_NUMPAD1:
			return 5049;
		case KeyEvent.VK_NUMPAD2:
			return 5050;
		case KeyEvent.VK_NUMPAD3:
			return 5051;
		case KeyEvent.VK_NUMPAD4:
			return 5052;
		case KeyEvent.VK_NUMPAD5:
			return 5053;
		case KeyEvent.VK_NUMPAD6:
			return 5054;
		case KeyEvent.VK_NUMPAD7:
			return 5055;
		case KeyEvent.VK_NUMPAD8:
			return 5056;
		case KeyEvent.VK_NUMPAD9:
			return 5057;
		case KeyEvent.VK_ADD:
			return 5043;
		case KeyEvent.VK_SUBTRACT:
			return 5045;
		case KeyEvent.VK_DIVIDE:
			return 5047;
		case KeyEvent.VK_MULTIPLY:
			return 5042;
		case KeyEvent.VK_DECIMAL:
			return 5046;

		case KeyEvent.VK_PRINTSCREEN:
			return 10000;
		}
		return 0;
	}

	public void updateCell(int addr, short content) {
		if (this.mode == TEXT_MODE) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + 160 * 60)) {
				int row = (addr - Engine.VIDEO_OFFS) / 160;
				Color foregroundColor = getTextColor((short) (~((content >> 12) & 7)));
				Color backgroundColor = getTextColor((short) ((content >> 8) & 7));
				int fixed_addr = (addr & 0xfffffffe) - Engine.VIDEO_OFFS;
				int col = (fixed_addr % 160) / 2;
				int c = (int) (content & 0xff);

				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.setFont(this.font);

				drawChar(g2, addr, row, col, c, foregroundColor, backgroundColor);
				drawChar(gr, addr, row, col, c, foregroundColor, backgroundColor);

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + col + ", " + row + "): " + String.format("%c", c));
					System.out.println("Foreground Color: " + this.foregroundColors[addr - Engine.VIDEO_OFFS]);
					System.out.println("Background Color: " + this.backgroundColors[addr - Engine.VIDEO_OFFS]);
				}
			}
		} else if (this.mode == GRAPHICS_MODE_320_240) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + (320 * 240) / 2)) {
				Color p1, p2, p3, p4;
				if ((addr & 1) == 0) {
					p1 = getColor((short) ((content >> 12) & 7));
					p2 = getColor((short) ((content >> 8) & 7));
					p3 = getColor((short) ((content >> 4) & 7));
					p4 = getColor((short) ((content) & 7));
				} else {
					addr -= 1;
					p1 = getColor((short) ((content >> 12) & 7));
					p2 = getColor((short) ((content >> 8) & 7));
					p3 = getColor((short) ((content >> 4) & 7));
					p4 = getColor((short) ((content) & 7));
				}
				Insets pixel = getCoordinate(addr);

				Graphics2D g2 = (Graphics2D) getGraphics();
				// draw on the actual screen
				drawPixels(g2, p1, p2, p3, p4, pixel);
				// draw in the memory so the paint method can redraw everything
				drawPixels(gr, p1, p2, p3, p4, pixel);

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + (pixel.left + 0) + ", " + (pixel.top) + "): " + p1);
					System.out.println("(" + (pixel.left + 1) + ", " + (pixel.top) + "): " + p2);
					System.out.println("(" + (pixel.left + 2) + ", " + (pixel.top) + "): " + p3);
					System.out.println("(" + (pixel.left + 3) + ", " + (pixel.top) + "): " + p4);
				}
			}
		} else if (this.mode == GRAPHICS_MODE_640_480) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + (640 * 480) / 8)) {
				Color p1, p2, p3, p4, p5, p6, p7, p8;
				Color p9, p10, p11, p12, p13, p14, p15, p16;
				p1 = getColor2(content & 0x8000);
				p2 = getColor2(content & 0x4000);
				p3 = getColor2(content & 0x2000);
				p4 = getColor2(content & 0x1000);
				p5 = getColor2(content & 0x0800);
				p6 = getColor2(content & 0x0400);
				p7 = getColor2(content & 0x0200);
				p8 = getColor2(content & 0x0100);
				p9 = getColor2(content & 0x0080);
				p10 = getColor2(content & 0x0040);
				p11 = getColor2(content & 0x0020);
				p12 = getColor2(content & 0x0010);
				p13 = getColor2(content & 0x0008);
				p14 = getColor2(content & 0x0004);
				p15 = getColor2(content & 0x0002);
				p16 = getColor2(content &  1);
				Insets pixel = getCoordinate(addr);

				Graphics2D g2 = (Graphics2D) getGraphics();
				// draw on the actual screen
				drawPixels2(g2, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, pixel);
				// draw in the memory so the paint method can redraw everything
				drawPixels2(gr, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, pixel);

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + (pixel.left + 0) + ", " + (pixel.top) + "): " + p1);
					System.out.println("(" + (pixel.left + 1) + ", " + (pixel.top) + "): " + p2);
					System.out.println("(" + (pixel.left + 2) + ", " + (pixel.top) + "): " + p3);
					System.out.println("(" + (pixel.left + 3) + ", " + (pixel.top) + "): " + p4);
					System.out.println("(" + (pixel.left + 4) + ", " + (pixel.top) + "): " + p5);
					System.out.println("(" + (pixel.left + 5) + ", " + (pixel.top) + "): " + p6);
					System.out.println("(" + (pixel.left + 6) + ", " + (pixel.top) + "): " + p7);
					System.out.println("(" + (pixel.left + 7) + ", " + (pixel.top) + "): " + p8);
					System.out.println("(" + (pixel.left + 8) + ", " + (pixel.top) + "): " + p9);
					System.out.println("(" + (pixel.left + 9) + ", " + (pixel.top) + "): " + p10);
					System.out.println("(" + (pixel.left + 10) + ", " + (pixel.top) + "): " + p11);
					System.out.println("(" + (pixel.left + 11) + ", " + (pixel.top) + "): " + p12);
					System.out.println("(" + (pixel.left + 12) + ", " + (pixel.top) + "): " + p13);
					System.out.println("(" + (pixel.left + 13) + ", " + (pixel.top) + "): " + p14);
					System.out.println("(" + (pixel.left + 14) + ", " + (pixel.top) + "): " + p15);
					System.out.println("(" + (pixel.left + 15) + ", " + (pixel.top) + "): " + p16);
				}
			}
		}

	}

	private void drawPixels2(Graphics2D gr, Color p1, Color p2, Color p3, Color p4, Color p5, Color p6, Color p7, Color p8, 
			Color p9, Color p10, Color p11, Color p12, Color p13, Color p14, Color p15, Color p16,
			Insets pixel) {
		gr.setColor(p1);
		gr.fillRect(pixel.left *2 + 8, pixel.top   *2 + titleBarHeight, 2, 2);
		gr.setColor(p2);
		gr.fillRect(pixel.left *2 + 10, pixel.top   *2 + titleBarHeight, 2, 2);
		gr.setColor(p3);
		gr.fillRect(pixel.left *2 + 12, pixel.top  *2 + titleBarHeight, 2, 2);
		gr.setColor(p4);
		gr.fillRect(pixel.left *2 + 14, pixel.top  *2 + titleBarHeight, 2, 2);		
		gr.setColor(p5);
		gr.fillRect(pixel.left *2 + 16, pixel.top  *2 + titleBarHeight, 2, 2);
		gr.setColor(p6);
		gr.fillRect(pixel.left *2 + 18, pixel.top  *2 + titleBarHeight, 2, 2);
		gr.setColor(p7);
		gr.fillRect(pixel.left *2 + 20, pixel.top  *2 + titleBarHeight, 2, 2);
		gr.setColor(p8);
		gr.fillRect(pixel.left *2 + 22, pixel.top  *2 + titleBarHeight, 2, 2);		
		gr.setColor(p9);
		gr.fillRect(pixel.left *2 + 24, pixel.top  *2  + titleBarHeight, 2, 2);
		gr.setColor(p10);
		gr.fillRect(pixel.left *2 + 26, pixel.top  *2  + titleBarHeight, 2, 2);
		gr.setColor(p11);
		gr.fillRect(pixel.left *2 + 28, pixel.top *2 + titleBarHeight, 2, 2);
		gr.setColor(p12);
		gr.fillRect(pixel.left *2 + 30, pixel.top *2  + titleBarHeight, 2, 2);		
		gr.setColor(p13);
		gr.fillRect(pixel.left *2 + 32, pixel.top *2  + titleBarHeight, 2, 2);
		gr.setColor(p14);
		gr.fillRect(pixel.left *2 + 34, pixel.top *2  + titleBarHeight, 2, 2);
		gr.setColor(p15);
		gr.fillRect(pixel.left *2 + 36, pixel.top *2  + titleBarHeight, 2, 2);
		gr.setColor(p16);
		gr.fillRect(pixel.left *2 + 38, pixel.top *2  + titleBarHeight, 2, 2);		
	}

	private Color getColor2(int s) {
		if (inverse) {
			if (s == 0)
				return Color.WHITE;
			else
				return Color.BLACK;
		} else {
			if (s == 0)
				return Color.BLACK;
			else
				return Color.WHITE;
		}
	}

	private void drawPixels(Graphics2D gr, Color p1, Color p2, Color p3, Color p4, Insets pixel) {
		gr.setColor(p1);
		gr.fillRect(pixel.left * 2 + 8, pixel.top * 2 + titleBarHeight, 2, 2);
		gr.setColor(p2);
		gr.fillRect(pixel.left * 2 + 10, pixel.top * 2 + titleBarHeight, 2, 2);
		gr.setColor(p3);
		gr.fillRect(pixel.left * 2 + 12, pixel.top * 2 + titleBarHeight, 2, 2);
		gr.setColor(p4);
		gr.fillRect(pixel.left * 2 + 14, pixel.top * 2 + titleBarHeight, 2, 2);
	}

	private void drawChar(Graphics2D g2, int addr, int row, int col, int c, Color foregroundColor, Color backgroundColor) {
		g2.setColor(backgroundColor);
		g2.fillRect(10 + col * 12, titleBarHeight - 5 + row * 12, 12, 13);

		g2.setColor(foregroundColor);
		if (c != 32)
			g2.drawString("" + String.format("%c", c), 10 + col * 12, titleBarHeight + 5 + row * 12);
	}

	private Insets getCoordinate(int addr) {
		if (mode == GRAPHICS_MODE_320_240) {
			int start = addr - Engine.VIDEO_OFFS;
			int row = start / 160;
			int col = start % 160;
			return new Insets(row, col * 2, 0, 0);
		} else if (mode == GRAPHICS_MODE_640_480) {
			int start = (addr & 0xFFFE) - Engine.VIDEO_OFFS;
			int row = start / 80;
			int col = start % 80;
			return new Insets(row, col * 8, 0, 0);
		}
		return new Insets(0, 0, 0, 0);
	}

	private Color getTextColor(short col) {
		int c = col & 7;
		switch (c) {
		case 0:
			if (this.inverse)
				return Color.white;
			else
				return Color.black;
		case 1:
			if (this.inverse)
				return new Color(0, 255, 255);
			else
				return Color.red;
		case 2:
			if (this.inverse)
				return new Color(255, 0, 255);
			else
				return Color.green;
		case 3:
			if (this.inverse)
				return new Color(0, 0, 255);
			else
				return new Color(255, 255, 0);
		case 4:
			if (this.inverse)
				return new Color(255, 255, 0);
			else
				return Color.blue;
		case 5:
			if (this.inverse)
				return new Color(0, 255, 0);
			else
				return new Color(255, 0, 255);
		case 6:
			if (this.inverse)
				return new Color(255, 0, 0);
			else
				return new Color(0, 255, 255);
		case 7:
			if (this.inverse)
				return Color.black;
			else
				return Color.white;
		default:
			return Color.black;
		}
	}

	private Color getColor(short col) {
		int c = col & 7;
		switch (c) {
		case 0:
			return Color.black;
		case 1:
			return Color.blue;
		case 2:
			return Color.green;
		case 3:
			return new Color(0, 255, 255);
		case 4:
			return Color.red;
		case 5:
			return new Color(255, 0, 255);
		case 6:
			return new Color(255, 255, 0);
		case 7:
			return Color.white;
		default:
			return Color.black;
		}
	}

	public void reset() {
		memMdl.fireTableDataChanged();
		this.backgroundColors = new Color[160 * 60];
		this.foregroundColors = new Color[160 * 60];

		Graphics g = getGraphics();
		if (this.inverse)
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.BLACK);
		
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

//		if (this.inverse)
//			gr.setColor(Color.WHITE);
//		else
//			gr.setColor(Color.BLACK);
//		gr.fillRect(0, 0, this.getWidth(), this.getHeight());

//		this.mode = TEXT_MODE;
	}

	@Override
	public void paint(Graphics g) {
		if (EmulatorMain.DEBUG)
			System.out.println("REPAINT");
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(img, null, 0, 0);
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setInverse(boolean b) {
		this.inverse = b;
	}

}
