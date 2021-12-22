package emulator.framebuffer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import emulator.EmulatorMain;
import emulator.engine.CpuContext;
import emulator.engine.Engine;
import emulator.src.Instruction;
import emulator.util.WindowUtil;

public class JOGLViewer extends JFrame implements GLEventListener, IFBViewer {
	private static final long serialVersionUID = -3111280045897513233L;

	private FBModel memMdl;
	private Color[] backgroundColors;
	private Color[] foregroundColors;
	private SpriteDef[] spriteDef;

	/**
	 * framebuffer mode: 1 - graphics mode, 320x240 pixels; 0 - text mode, 80x60
	 * characters
	 */
	private int mode = TEXT_MODE;
	private boolean inverse = false;

	//private JFrame frame;

	ByteBuffer framebuffer = ByteBuffer.allocate(640 * 480 * 3 * 4);
	ByteBuffer spriteframebuffer = ByteBuffer.allocate(640 * 480 * 3 * 4);


	private CpuContext ctx;

	private void mouseIrq(short key, short x, short y, short status) {
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 0] = x;
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 1] = y;
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 2] = key;
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 3] = status;
	}

	boolean first = true;

	public JOGLViewer(GraphicsConfiguration conf, CpuContext ctx, Engine eng) {
		super(conf);
		this.ctx = ctx;
		this.memMdl = new FBModel(ctx);
		this.backgroundColors = new Color[160 * 60];
		this.foregroundColors = new Color[160 * 60];

		reverse8(font_8x8);

		spriteDef = new SpriteDef[SPRITE_COUNT];
		for (int i = 0; i < SPRITE_COUNT; i++) {
			spriteDef[i] = new SpriteDef();
		}

		// getting the capabilities object of GL2 profile
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);

		// The canvas
		final GLCanvas glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener(this);
		glcanvas.setSize(640*scale, 480*scale);
		glcanvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
//				System.out.println(e.getKeyChar());
				if (ctx.memory[Engine.IRQ2_PRESSED_ADDR / 2] == 0)
					return;
				ctx.memory[24] = VkToFpga(e);
				Engine.irq2_pressed = true;
				Engine.irq2_released = false;
				
				try {
					ctx.engine.rl.lock();
					ctx.engine.cond.await(1000, TimeUnit.MILLISECONDS);
					ctx.engine.rl.unlock();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (ctx.memory[Engine.IRQ2_RELEASED_ADDR / 2] == 0)
					return;
				ctx.memory[24] = VkToFpga(e);
				Engine.irq2_pressed = false;
				Engine.irq2_released = true;
				
				try {
					ctx.engine.rl.lock();
					ctx.engine.cond.await(1000, TimeUnit.MILLISECONDS);
					ctx.engine.rl.unlock();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		glcanvas.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				first = true;
			}
		});
		glcanvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent m) {
				if (!first) {
					setCursor(getToolkit().createCustomCursor(
							new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));
				}
			}

			@Override
			public void mouseExited(MouseEvent m) {
				setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mousePressed(MouseEvent m) {
				if (first) {
					first = false;
					setCursor(getToolkit().createCustomCursor(
							new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(), null));
				} else {
					int x = m.getX() / (scale * 2);
					int y = m.getY() / (scale * 2); // (scale == 1?2:4)
					if (x > 303)
						x = 303;
					if (y > 223)
						y = 223;
					mouseIrq((short) 9, (short) x, (short) y, (short) 1);
				}
			}

			@Override
			public void mouseReleased(MouseEvent m) {
				if (first) {
					return;
				}
				int x = m.getX() / (scale * 2);
				int y = m.getY() / (scale * 2); // (scale == 1?2:4)
				if (x > 303)
					x = 303;
				if (y > 223)
					y = 223;
				mouseIrq((short) 8, (short) x, (short) y, (short) 1);
			}
		});

		glcanvas.addMouseMotionListener(new MouseAdapter() {
			int lastX = 0;
			int lastY = 0;

			@Override
			public void mouseMoved(MouseEvent m) {
				if (first) {
					return;
				}

				int x = m.getX() / (scale * 2);
				int y = m.getY() / (scale * 2); // (scale == 1?2:4)
				if (x > 303)
					x = 303;
				if (y > 223)
					y = 223;
				short button = 0;
				if ((x != lastX) || (y != lastY)) {
					button = 0;
					if (x < lastX) {
						button += 8;
					} else {
						button += 16;
					}
					if (y < lastY) {
						button += 0;
					} else {
						button += 32;
					}

				}
				mouseIrq(button, (short) x, (short) y, (short) 1);
				//System.out.println("MOUSE MOVE " + x + ", " + y );
				lastX = x;
				lastY = y;
			}

			@Override
			public void mouseDragged(MouseEvent m) {
				if (first) {
					return;
				}
				int x = m.getX() / (scale * 2);
				int y = m.getY() / (scale * 2); // (scale == 1?2:4)
				short button = 0;
				if ((x != lastX) || (y != lastY)) {
					button = 0;
					if (x < lastX) {
						button += 8;
					} else {
						button += 16;
					}
					if (y < lastY) {
						button += 0;
					} else {
						button += 32;
					}

				}
				mouseIrq((short) (button + 1), (short) x, (short) y, (short) 1);

				lastX = x;
				lastY = y;
			}
		});

		Animator an = new Animator(glcanvas);
		an.start();

		// adding canvas to frame
		this.getContentPane().add(glcanvas);
		
		WindowUtil.setBounds(ctx.engine.main.ini.getInt("FB", "x", 1024), ctx.engine.main.ini.getInt("FB", "y", 100),
				ctx.engine.main.ini.getInt("FB", "width", 640), ctx.engine.main.ini.getInt("FB", "height", 480),
				this, ctx.engine.main.ini.getString("FB", "display", "\\Display0"));

		setVisible(true);
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
			return 27;
		case KeyEvent.VK_BACK_SPACE:
			return 8;

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

	int oldx = -1, oldy = -1;

	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();

		spriteframebuffer.position(0);
		spriteframebuffer.put(framebuffer.array());
		spriteframebuffer.position(0);
		gl.glRasterPos2d(-1.0, -1.0);

		if (this.mode == GRAPHICS_MODE_320_240) {
			for (int i = 0; i < SPRITE_COUNT; i++) {
				if (ctx.memory[(SPRITE_DEF_START + i * 8) / 2] != 0) {
					spriteDef[i].spriteAddr = Instruction.fix(ctx.memory[(SPRITE_DEF_START + i * 8) / 2]);
					spriteDef[i].x = ctx.memory[(SPRITE_DEF_START + i * 8 + 2) / 2];
					spriteDef[i].y = ctx.memory[(SPRITE_DEF_START + i * 8 + 4) / 2];
					spriteDef[i].transparentColor = getColor(ctx.memory[(SPRITE_DEF_START + i * 8 + 6) / 2]);
					// if ((spriteDef[i].x != oldx) || (spriteDef[i].y != oldy)) {
					oldx = spriteDef[i].x;
					oldy = spriteDef[i].y;
					fillSprite(spriteDef[i], ctx, spriteDef[i].x, spriteDef[i].y);
					// }
				}
			}
		}
		gl.glDrawPixels(640 * scale, 480 * scale, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, spriteframebuffer);
		gl.glFlush();
	}

	protected void fillSprite(SpriteDef sp, CpuContext ctx, int x, int y) {
		Color[] pixels = new Color[4];
		// int offset = ((479 - pixel.top*2)) * 640 * 3 + (pixel.left*2 * 3);
		int lineBytes = 640*3 * scale;
		int faddr = (479 - y * 2) * lineBytes + (x * 2) * 3;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				int addr = sp.spriteAddr + i * 8 + j * 2;
				short content = ctx.memory[addr / 2];
				pixels[0] = getColor((int) ((content >> 12) & 7));
				pixels[1] = getColor((int) ((content >> 8) & 7));
				pixels[2] = getColor((int) ((content >> 4) & 7));
				pixels[3] = getColor((int) ((content) & 7));
				for (int k = 0; k < 4; k++) {
					int faddr2 = faddr - (i * 2 * lineBytes) + j * 12 * 2 + k * 6;
					if (!pixels[k].equals(sp.transparentColor)) {
						putPixel(spriteframebuffer, faddr2 + 0, pixels[k]);
						putPixel(spriteframebuffer, faddr2 + 3, pixels[k]);
						putPixel(spriteframebuffer, faddr2 - lineBytes + 0, pixels[k]);
						putPixel(spriteframebuffer, faddr2 - lineBytes + 3, pixels[k]);
					}
				}
			}
		}

	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		this.dispose();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		framebuffer.position(0);
		for (int i = 0; i < framebuffer.capacity(); i++) {
			if (this.inverse)
				framebuffer.put((byte) 255);
			else
				framebuffer.put((byte) 0);
		}
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void updateCell(int addr, short cont) {
		int content = cont & 0xFFFF;
		if (this.mode == TEXT_MODE) {
			if ((addr >= Engine.VIDEO_OFFS) && (addr < (Engine.VIDEO_OFFS + 160 * 60))) {
				int row = (addr - Engine.VIDEO_OFFS) / 160;
				Color foregroundColor = getTextColor((int) (~((content >>> 12) & 15)), true);
				Color backgroundColor = getTextColor((int) ((content >>> 8) & 15), false);
				int fixed_addr = ((addr & 0xfffffffe) - Engine.VIDEO_OFFS);
				int col = (fixed_addr % 160) / 2;
				int c = 127 - (int) (content & 0xff);
				int lineBytes = 640*3 * scale;
				int offset = (59 - row) * 8 * lineBytes + (col) * 8 * 3;
				
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if ((font_8x8[c * 8 + i] & (1 << j)) != 0) {
							putPixel(framebuffer, offset + i * lineBytes + (7 - j) * 3, foregroundColor);
						} else {
							putPixel(framebuffer, offset + i * lineBytes + (7 - j) * 3, backgroundColor);
						}
					}
				}

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + col + ", " + row + "): " + String.format("%c", c));
					System.out.println("Foreground Color: " + this.foregroundColors[addr - Engine.VIDEO_OFFS]);
					System.out.println("Background Color: " + this.backgroundColors[addr - Engine.VIDEO_OFFS]);
				}
			}
		} else if (this.mode == GRAPHICS_MODE_320_240) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + (320 * 240) / 2)) {
				Color[] color = new Color[4];
				if ((addr & 1) == 0) {
					color[0] = getColor((int) ((content >> 12) & 15));
					color[1] = getColor((int) ((content >> 8) & 15));
					color[2] = getColor((int) ((content >> 4) & 15));
					color[3] = getColor((int) ((content) & 15));
				} else {
					addr -= 1;
					color[0] = getColor((int) ((content >> 12) & 15));
					color[1] = getColor((int) ((content >> 8) & 15));
					color[2] = getColor((int) ((content >> 4) & 15));
					color[3] = getColor((int) ((content) & 15));
				}
				Insets pixel = getCoordinate(addr);
				int lineBytes = 640*3 * scale;
				int offset = ((479 - pixel.top * 2)) * lineBytes + (pixel.left * 2 * 3);
				for (int i = 0; i < 4; i++) {
					putPixel(framebuffer, offset + i * 6 , color[i]);
					putPixel(framebuffer, offset + i * 6 + 3, color[i]);
					putPixel(framebuffer, offset + i * 6 - lineBytes, color[i]);
					putPixel(framebuffer, offset + i * 6 - lineBytes + 3, color[i]);
				}

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + (pixel.left + 0) + ", " + (pixel.top) + "): " + color[0]);
					System.out.println("(" + (pixel.left + 1) + ", " + (pixel.top) + "): " + color[1]);
					System.out.println("(" + (pixel.left + 2) + ", " + (pixel.top) + "): " + color[2]);
					System.out.println("(" + (pixel.left + 3) + ", " + (pixel.top) + "): " + color[3]);
				}
			}
		} else if (this.mode == GRAPHICS_MODE_640_480) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + (640 * 480) / 8)) {
				Color[] color = new Color[16];
				color[0] = getColor2(content & 0x8000);
				color[1] = getColor2(content & 0x4000);
				color[2] = getColor2(content & 0x2000);
				color[3] = getColor2(content & 0x1000);
				color[4] = getColor2(content & 0x0800);
				color[5] = getColor2(content & 0x0400);
				color[6] = getColor2(content & 0x0200);
				color[7] = getColor2(content & 0x0100);
				color[8] = getColor2(content & 0x0080);
				color[9] = getColor2(content & 0x0040);
				color[10] = getColor2(content & 0x0020);
				color[11] = getColor2(content & 0x0010);
				color[12] = getColor2(content & 0x0008);
				color[13] = getColor2(content & 0x0004);
				color[14] = getColor2(content & 0x0002);
				color[15] = getColor2(content & 1);
				Insets pixel = getCoordinate(addr);
				int lineBytes = 640*3 * scale;
				// int offset = (59 - row) * 8 * 640 * 3 + (col) * 8 * 3;
				int offset = (479 - pixel.top) * lineBytes + (pixel.left * 3);
				for (int i = 0; i < 16; i++) {
					putPixel(framebuffer, offset + i * 3, color[i]);
				}

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + (pixel.left + 0) + ", " + (pixel.top) + "): " + color[0]);
					System.out.println("(" + (pixel.left + 1) + ", " + (pixel.top) + "): " + color[1]);
					System.out.println("(" + (pixel.left + 2) + ", " + (pixel.top) + "): " + color[2]);
					System.out.println("(" + (pixel.left + 3) + ", " + (pixel.top) + "): " + color[3]);
					System.out.println("(" + (pixel.left + 4) + ", " + (pixel.top) + "): " + color[4]);
					System.out.println("(" + (pixel.left + 5) + ", " + (pixel.top) + "): " + color[5]);
					System.out.println("(" + (pixel.left + 6) + ", " + (pixel.top) + "): " + color[6]);
					System.out.println("(" + (pixel.left + 7) + ", " + (pixel.top) + "): " + color[7]);
					System.out.println("(" + (pixel.left + 8) + ", " + (pixel.top) + "): " + color[8]);
					System.out.println("(" + (pixel.left + 9) + ", " + (pixel.top) + "): " + color[9]);
					System.out.println("(" + (pixel.left + 10) + ", " + (pixel.top) + "): " + color[10]);
					System.out.println("(" + (pixel.left + 11) + ", " + (pixel.top) + "): " + color[11]);
					System.out.println("(" + (pixel.left + 12) + ", " + (pixel.top) + "): " + color[12]);
					System.out.println("(" + (pixel.left + 13) + ", " + (pixel.top) + "): " + color[13]);
					System.out.println("(" + (pixel.left + 14) + ", " + (pixel.top) + "): " + color[14]);
					System.out.println("(" + (pixel.left + 15) + ", " + (pixel.top) + "): " + color[15]);
				}
			}
		}

	}

	private int scale = 2;
	private void putPixel(ByteBuffer buff, int addr, Color foregroundColor) {
		int hOffset = 0;
		int row = (addr / 3) / 640;
		int col = ((addr - (row * 640*3)) / 3);
//		System.out.printf("addr: %d, row: %d, col: %d\n", addr, row, col);
		//row = 479 - row;
		if (scale == 1) {
			buff.put(addr + 0, (byte) foregroundColor.getRed());
			buff.put(addr + 1, (byte) foregroundColor.getGreen());
			buff.put(addr + 2, (byte) foregroundColor.getBlue());
		} else {
			try {
				addr = (row * 640*3*scale) +  (col*3*scale);
				buff.put(addr + 0 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 1 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 2 + hOffset, (byte) foregroundColor.getBlue());
	
				buff.put(addr + 3 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 4 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 5 + hOffset, (byte) foregroundColor.getBlue());
	
				buff.put(addr + 640*3*scale + 0 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 640*3*scale + 1 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 640*3*scale + 2 + hOffset, (byte) foregroundColor.getBlue());
	
				buff.put(addr + 640*3*scale + 3 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 640*3*scale + 4 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 640*3*scale + 5 + hOffset, (byte) foregroundColor.getBlue());
			
			} catch (Exception ex) {
				System.out.printf("addr: %d, row: %d, col: %d\n", addr, row, col);
			}
		}
	}

	private static final int ONE = 198;
	private Color getTextColor(int col, boolean fg) {
		int c = col & 15;
//		if (!fg) {
//			if (c > 7) {
//				c -= 8;
//			} else {
//				c += 8;
//			}
//		}
		switch (c) {
		case 0:
			if (this.inverse)
				return Color.white;
			else
				return Color.black;
		case 9:
			if (this.inverse)
				return new Color(255, 255, 0);
			else
				return Color.blue;
		case 10:
			if (this.inverse)
				return new Color(255, 0, 255);
			else
				return Color.green;
		case 11:
			if (this.inverse)
				return new Color(255, 255, 0);
			else
				return new Color(0, 255, 255);
		case 12:
			if (this.inverse)
				return new Color(0, 255, 255);
			else
				return Color.red;
		case 13:
			if (this.inverse)
				return new Color(0, 255, 0);
			else
				return new Color(255, 0, 255);
		case 14:
			if (this.inverse)
				return new Color(0, 0, 255);
			else
				return new Color(255, 255, 0);
		case 15:
			if (this.inverse)
				return Color.black;
			else
				return Color.white;
		case 8:
			if (this.inverse)
				return new Color(128, 128, 128);
			else
				return new Color(128, 128, 128);
		case 1:
			if (this.inverse)
				return new Color(128, 128, 0);
			else
				return new Color(0, 0, ONE);
		case 2:
			if (this.inverse)
				return new Color(128, 0, 128);
			else
				return new Color(0, ONE, 0);
		case 3:
			if (this.inverse)
				return new Color(128, 0, 0);
			else
				return new Color(0, ONE, ONE);
		case 4:
			if (this.inverse)
				return new Color(0, 128, 128);
			else
				return new Color(ONE, 0, 0);
		case 5:
			if (this.inverse)
				return new Color(0, 128, 0);
			else
				return new Color(ONE, 0, ONE);
		case 6:
			if (this.inverse)
				return new Color(0, 0, 128);
			else
				return new Color(ONE, ONE, 0);
		case 7:
			if (this.inverse)
				return new Color(128, 128, 128);
			else
				return new Color(ONE, ONE, ONE);
		default:
			return Color.black;
		}
	}

	private Color getColor(int col) {
		int c = col & 15;
		switch (c) {
		case 0:
			return Color.black;
		case 9:
			return Color.blue;
		case 10:
			return Color.green;
		case 11:
			return new Color(0, 255, 255);
		case 12:
			return Color.red;
		case 13:
			return new Color(255, 0, 255);
		case 14:
			return new Color(255, 255, 0);
		case 15:
			return Color.white;
		case 8:
			return Color.gray;
		case 1:
			return new Color(0, 0, ONE);
		case 2:
			return new Color(0, ONE, 0);
		case 3:
			return new Color(0, ONE, ONE);
		case 4:
			return new Color(ONE, 0, 0);
		case 5:
			return new Color(ONE, 0, ONE);
		case 6:
			return new Color(ONE, ONE, 0);
		case 7:
			return new Color(ONE, ONE, ONE);
		default:
			return Color.black;
		}
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

	public void reset() {
		memMdl.fireTableDataChanged();
		this.backgroundColors = new Color[160 * 60];
		this.foregroundColors = new Color[160 * 60];
	}

	private static void reverse8(byte[] buff) {
		// 0 -> 23, 1 -> 22, 2 -> 21
		byte t;
		for (int i = 0; i < buff.length / 2; i++) {
			t = buff[i];
			buff[i] = buff[buff.length - i - 1];
			buff[buff.length - i - 1] = t;
		}
	}

	//public void dispose() {
		//setVisible(false);
//	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setInverse(boolean b) {
		this.inverse = b;
	}

	@Override
	public JFrame getFrame() {
		return this;
	}

	@Override
	public int getMode() {
		return this.mode;
	}

}
