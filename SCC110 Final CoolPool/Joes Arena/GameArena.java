import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.lang.Class;

/**
 * This class provides a simple window in which grahical objects can be drawn. 
 * @author Joe Finney
 */
public class GameArena extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener
{
	// Size of playarea
	private JFrame frame;
	private int arenaWidth;
	private int arenaHeight;

	private boolean exiting = false; 

	private ArrayList<Object> things = new ArrayList<Object>();

	private HashMap<String, Color> colours = new HashMap<>();

	private boolean up = false;
	private boolean down = false;
	private boolean left = false;
	private boolean right = false;
	private boolean shift = false;
	private boolean space = false;
	private boolean esc = false;
	private boolean enter = false;
	private boolean x = false;
	private boolean z = false;
	private boolean o = false;
	private boolean leftMouse = false;
	private boolean rightMouse = false;
	private int mouseX = 0;
	private int mouseY = 0;

	private BufferedImage buffer;
	private Graphics2D graphics;
	private Map<RenderingHints.Key, Object> renderingHints;
	private boolean rendered = false;

	/**
	 * Create a view of a GameArena.
	 * 
	 * @param width The width of the playing area, in pixels.
	 * @param height The height of the playing area, in pixels.
	 */
	public GameArena(int width, int height)
	{
		this.init(width, height, true);
	}

	/**
	 * Create a view of a GameArena.
	 * 
	 * @param width The width of the playing area, in pixels.
	 * @param height The height of the playing area, in pixels.
	 * @param createWindow Defines if a window should be created to host this GameArena. @see getPanel.
	 */
	public GameArena(int width, int height, boolean createWindow)
	{
		this.init(width, height, createWindow);
	}

	/**
	 * Internal initialisation method - called by constructor methods.
	 */
	private void init(int width, int height, boolean createWindow)
	{
		if (createWindow)
		{
			this.frame = new JFrame();
			frame.setTitle("Let's Play!");
			frame.setSize(width, height);
			frame.setResizable(false);
			frame.setBackground(Color.BLACK);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(this);
			frame.setVisible(true);		
		}

		this.setSize(width, height);

		// Add standard colours.
		colours.put("BLACK", Color.BLACK);
		colours.put("BLUE", Color.BLUE);
		colours.put("CYAN", Color.CYAN);
		colours.put("DARKGREY", Color.DARK_GRAY);
		colours.put("GREY", Color.GRAY);
		colours.put("GREEN", Color.GREEN);
		colours.put("LIGHTGREY", Color.LIGHT_GRAY);
		colours.put("MAGENTA", Color.MAGENTA);
		colours.put("ORANGE", Color.ORANGE);
		colours.put("PINK", Color.PINK);
		colours.put("RED", Color.RED);
		colours.put("WHITE", Color.WHITE);
		colours.put("YELLOW", Color.YELLOW);

		// Setup graphics rendering hints for quality
		renderingHints = new HashMap<>();
		renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		try {
			Class rh = Class.forName("java.awt.RenderingHints");
			RenderingHints.Key key = (RenderingHints.Key) rh.getField("KEY_RESOLUTION_VARIANT").get(null);
			Object value = rh.getField("VALUE_RESOLUTION_VARIANT_DPI_FIT").get(null);
			renderingHints.put(key, value);
		}
		catch (Exception e){}

		Thread t = new Thread(this);		
		t.start();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		if (frame != null)
			frame.addKeyListener(this);
	}

	public void run() {
		try {
			while (!exiting) {
				this.repaint();
				Thread.sleep(10);
			}
		} catch (InterruptedException iex) {}

		if (frame != null)
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Update the size of the GameArena.
	 *
	 * @param width the new width of the window in pixels.
	 * @param height the new height of the window in pixels.
	 */
	public void setSize(int width, int height)
	{
		this.arenaWidth = width;
		this.arenaHeight = height;

		super.setSize(width,height);

		if (frame != null)
			frame.setSize(arenaWidth + frame.getInsets().left + frame.getInsets().right, arenaHeight + frame.getInsets().top + frame.getInsets().bottom);


	}	

	/**
	 * Retrieves the JPanel on which this gameArena is drawn, so that it can be integrated into
	 * a users application. 
	 * 
	 * n.b. This should only be called if this GameArena was constructed without its own JFrame
	 * 
	 * @return the JPanel containing this GameArena.
	 */
	public JPanel getPanel()
	{
		return this;
	}
	/**
	 * Close this GameArena window.
	 * 
	 */
	public void exit()
	{
		this.exiting = true;
	}

	/**
	 * A method called by the operating system to draw onto the screen - <p><B>YOU DO NOT (AND SHOULD NOT) NEED TO CALL THIS METHOD.</b></p>
	 */
	public void paint (Graphics gr)
	{
		
		Graphics2D window = (Graphics2D) gr;

		if (!rendered)
		{
			this.setSize(arenaWidth, arenaHeight);

			// Create a buffer the same size of the window, which we can reuse from frame to frame to improve performance.
			buffer = new BufferedImage(arenaWidth, arenaHeight, BufferedImage.TYPE_INT_ARGB);
			graphics = buffer.createGraphics();
			graphics.setRenderingHints(renderingHints);

			// Remember that we've completed this initialisation, so that we don't do it again...
			rendered = true;
		}

		if (frame == null)
		{
			// Find the JFrame we have been added to, and attach a KeyListner
			frame = (JFrame) SwingUtilities.getWindowAncestor(this);

			if (frame != null)
				frame.addKeyListener(this);
		}

		window.setRenderingHints(renderingHints);

		synchronized (this)
		{
			if (!this.exiting)
			{
				graphics.clearRect(0,0, arenaWidth, arenaHeight);

				for (Object o : things)
				{
					if (o instanceof Ball)
					{
						Ball b = (Ball) o;
						graphics.setColor(this.getColourFromString(b.getColour()));
						graphics.fillOval((int)(b.getXPosition() - b.getSize()/2), (int)(b.getYPosition() - b.getSize()/2), (int)b.getSize(), (int)b.getSize());
					}

					if (o instanceof Rectangle)
					{
						Rectangle r = (Rectangle) o;
						graphics.setColor(this.getColourFromString(r.getColour()));
						graphics.fillRect((int)r.getXPosition(), (int)r.getYPosition(), (int)r.getWidth(), (int)r.getHeight());
					}

					if (o instanceof Line)
					{
						Line l = (Line) o;
						graphics.setColor(this.getColourFromString(l.getColour()));
						graphics.setStroke(new BasicStroke((float)l.getWidth()));

						float sx = (float)l.getXStart();
						float sy = (float)l.getYStart();
						float ex = (float)l.getXEnd();
						float ey = (float)l.getYEnd();

						if (l.getArrowSize() > 0)
						{
							float arrowRatio = (float) (1.0 - ((l.getWidth() * l.getArrowSize()) / l.getLength()));
							ex = sx + ((ex - sx) * arrowRatio); 
							ey = sy + ((ey - sy) * arrowRatio); 
							graphics.fillPolygon(l.getArrowX(), l.getArrowY(), 3);
						}
						graphics.draw(new Line2D.Float(sx,sy,ex,ey));
					}

					if (o instanceof Text)
					{
						Text t = (Text) o;
						graphics.setFont(new Font("SansSerif", Font.BOLD, t.getSize()));
						graphics.setColor(this.getColourFromString(t.getColour()));
						graphics.drawString(t.getText(),(float)t.getXPosition(), (float)t.getYPosition());
					}
				}
			}
					
			window.drawImage(buffer, this.getInsets().left, this.getInsets().top, this);
		}
	}

	//
	// Shouldn't really handle colour this way, but the student's haven't been introduced
	// to constants properly yet, hmmm....
	// 
	private Color getColourFromString(String col)
	{
		Color c = colours.get(col.toUpperCase());

		if (c == null && col.startsWith("#"))
		{
			int r = Integer.valueOf( col.substring( 1, 3 ), 16 );
			int g = Integer.valueOf( col.substring( 3, 5 ), 16 );
			int b = Integer.valueOf( col.substring( 5, 7 ), 16 );

			c = new Color(r,g,b);
			colours.put(col.toUpperCase(), c);
		}

		if (c == null)
			c = Color.WHITE;

		return c;
	}

	/**
	 * Adds a given Object to the drawlist, maintaining z buffering order. 
	 *
	 * @param o the object to add to the drawlist.
	 */
	private void addThing(Object o, int layer)
	{
		boolean added = false;

		if (exiting)
			return;

		synchronized (this)
		{
			if (things.size() > 100000)
			{
				System.out.println("\n\n");
				System.out.println(" ********************************************************* ");
				System.out.println(" ***** Only 100000 Objects Supported per Game Arena! ***** ");
				System.out.println(" ********************************************************* ");
				System.out.println("\n");
				System.out.println("-- Joe\n\n");
				
				this.exit();
			}
			else
			{
				// Try to insert this object into the list.
				for (int i=0; i<things.size(); i++)
				{
					int l = 0;
					Object obj = things.get(i);

					if (obj instanceof Ball)
						l = ((Ball)obj).getLayer();

					if (obj instanceof Rectangle)
						l = ((Rectangle)obj).getLayer();

					if (obj instanceof Line)
						l = ((Line)obj).getLayer();

					if (obj instanceof Text)
						l = ((Text)obj).getLayer();

					if (layer < l)
					{
						things.add(i,o);
						added = true;
						break;
					}
				}

				// If there are no items in the list with an equivalent or higher layer, append this object to the end of the list.
				if (!added)
					things.add(o);
			}
		}
	}

	/**
	 * Remove an object from the drawlist. 
	 *
	 * @param o the object to remove from the drawlist.
	 */
	private void removeObject(Object o)
	{
		synchronized (this)
		{
			things.remove(o);
		}
	}

	/**
	 * Adds a given Ball to the GameArena. 
	 * Once a Ball is added, it will automatically appear on the window. 
	 *
	 * @param b the ball to add to the GameArena.
	 */
	public void addBall(Ball b)
	{
		this.addThing(b, b.getLayer());
	}

	/**
	 * Adds a given Rectangle to the GameArena. 
	 * Once a rectangle is added, it will automatically appear on the window. 
	 *
	 * @param r the rectangle to add to the GameArena.
	 */
	public void addRectangle(Rectangle r)
	{
		this.addThing(r, r.getLayer());
	}

	/**
	 * Adds a given Line to the GameArena. 
	 * Once a Line is added, it will automatically appear on the window. 
	 *
	 * @param l the line to add to the GameArena.
	 */
	public void addLine(Line l)
	{
		this.addThing(l, l.getLayer());
	}

	/**
	 * Adds a given Text object to the GameArena. 
	 * Once a Text object is added, it will automatically appear on the window. 
	 *
	 * @param t the text object to add to the GameArena.
	 */
	public void addText(Text t)
	{
		this.addThing(t, t.getLayer());
	}


	/**
	 * Remove a Rectangle from the GameArena. 
	 * Once a Rectangle is removed, it will no longer appear on the window. 
	 *
	 * @param r the rectangle to remove from the GameArena.
	 */
	public void removeRectangle(Rectangle r)
	{
		this.removeObject(r);
	}

	/**
	 * Remove a Ball from the GameArena. 
	 * Once a Ball is removed, it will no longer appear on the window. 
	 *
	 * @param b the ball to remove from the GameArena.
	 */
	public void removeBall(Ball b)
	{
		this.removeObject(b);
	}

	/**
	 * Remove a Line from the GameArena. 
	 * Once a Line is removed, it will no longer appear on the window. 
	 *
	 * @param l the line to remove from the GameArena.
	 */
	public void removeLine(Line l)
	{
		this.removeObject(l);
	}

	/**
	 * Remove a Text object from the GameArena. 
	 * Once a Text object is removed, it will no longer appear on the window. 
	 *
	 * @param t the text object to remove from the GameArena.
	 */
	public void removeText(Text t)
	{
		this.removeObject(t);
	}

	/**
	 * Pause for a 1/50 of a second. 
	 * This method causes your program to delay for 1/50th of a second. You'll find this useful if you're trying to animate your application.
	 *
	 */
	public void pause()
	{
		try { Thread.sleep(20); }
		catch (Exception e) {};
	}

 	public void keyPressed(KeyEvent e) 
	{
		keyAction(e,true);
	}
 	
	public void keyAction(KeyEvent e,boolean yn) 
	{
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_UP)
		{
			up = yn;
			increasePower();
		}					
		if (code == KeyEvent.VK_DOWN)
		{
			down = yn;
			decreasePower();
			
		}					
		if (code == KeyEvent.VK_LEFT)
		{
			left = yn;
			if(zCounter % 2 != 0)
				rotateAntiClockwiseSlow();			
			else rotateAntiClockwise();		
		}			
		if (code == KeyEvent.VK_RIGHT)
		{
			right = yn;
			if(zCounter % 2 != 0)
				rotateClockwiseSlow();		
			else rotateClockwise();			
		}								
		if (code == KeyEvent.VK_SPACE)
		{
			space = yn;
			play();			
		}					
		if (code == KeyEvent.VK_SHIFT)
		{
			shift = yn;
			changeLineSize();	// increases/ decreases trajectory line size		
		}
		if (code == KeyEvent.VK_ESCAPE)
			esc = yn;		
		if (code == KeyEvent.VK_ENTER)
			enter = yn;		
		if (code == KeyEvent.VK_X)
			x = yn;		
		if (code == KeyEvent.VK_Z)
		{		
			z = yn;
			zCounter++; // acts as toggle button first slwoingdown rotation
		}			
		if (code == KeyEvent.VK_O)
			o = yn;		
	}

	public void keyReleased(KeyEvent e)
	{
		
	}


 	public void keyTyped(KeyEvent e) 
	{		
		
	}


	public void mousePressed(MouseEvent e) 
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			this.leftMouse = true;

		if (e.getButton() == MouseEvent.BUTTON3)
			this.rightMouse = true;
	}

	public void mouseReleased(MouseEvent e) 
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			this.leftMouse = false;

		if (e.getButton() == MouseEvent.BUTTON3)
			this.rightMouse = false;
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public void mouseClicked(MouseEvent e) 
	{
	}

	public void mouseMoved(MouseEvent e) 
	{
		mouseX = e.getX();	
		mouseY = e.getY();	
	}

	public void mouseDragged(MouseEvent e) 
	{
	}

	/** 
	 * Gets the width of the GameArena window, in pixels.
	 * @return the width in pixels
	 */
	public int getArenaWidth()
	{
		return arenaWidth;
	}

	/** 
	 * Gets the height of the GameArena window, in pixels.
	 * @return the height in pixels
	 */
	public int getArenaHeight()
	{
		return arenaHeight;
	}

	/** 
	 * Determines if the user is currently pressing the cursor up button.
	 * @return true if the up button is pressed, false otherwise.
	 */
	public boolean upPressed()
	{
		return up;
	}

	/** 
	 * Determines if the user is currently pressing the cursor down button.
	 * @return true if the down button is pressed, false otherwise.
	 */
	public boolean downPressed()
	{
		return down;
	}

	/** 
	 * Determines if the user is currently pressing the cursor left button.
	 * @return true if the left button is pressed, false otherwise.
	 */
	public boolean leftPressed()
	{
		return left;
	}

	/** 
	 * Determines if the user is currently pressing the cursor right button.
	 * @return true if the right button is pressed, false otherwise.
	 */
	public boolean rightPressed()
	{
		return right;
	}

	/** 
	 * Determines if the user is currently pressing the space bar.
	 * @return true if the space bar is pressed, false otherwise.
	 */
	public boolean spacePressed()
	{
		return space;
	}

        /** 
	 * Determines if the user is currently pressing the Esc button.
	 * @return true if the esc button is pressed, false otherwise.
	 */
	public boolean escPressed()
	{
		return esc;
	}

	/**
	 * Determines if the user is currently pressing the enter button.
	 * @return true if the enter button is pressed, false otherwise.
	 */
	public boolean enterPressed()
	{
		return enter;
	}

	/** 
	 * Determines if the user is currently pressing the x button.
	 * @return true if the x button is pressed, false otherwise.
	 */
	public boolean xPressed()
	{
		return x;
	}

	/**
	 * Determines if the user is currently pressing the z button.
	 * @return true if the z button is pressed, false otherwise.
	 */
	public boolean zPressed()
	{
		return z;
	}

	/**
	 * Determines if the user is currently pressing the o button.
	 * @return true if the o button is pressed, false otherwise.
	 */
	public boolean oPressed()
	{
		return o;
	}

	/** 
	 * Determines if the user is currently pressing the shift key.
	 * @return true if the shift key is pressed, false otherwise.
	 */
	public boolean shiftPressed()
	{
		return shift;
	}

	/** 
	 * Determines if the user is currently pressing the left mouse button.
	 * @return true if the left mouse button is pressed, false otherwise.
	 */
	public boolean leftMousePressed()
	{
		return leftMouse;
	}

	/** 
	 * Determines if the user is currently pressing the right mouse button.
	 * @return true if the right mouse button is pressed, false otherwise.
	 */
	public boolean rightMousePressed()
	{
		return rightMouse;
	}

	/**
	 * Gathers location informaiton on the mouse pointer.
	 * @return the current X coordinate of the mouse pointer in the GameArena.
	 */
	public int getMousePositionX()
	{
		return mouseX;
	}

	/**
	 * Gathers location informaiton on the mouse pointer.
	 * @return the current Y coordinate of the mouse pointer in the GameArena.
	 */
	public int getMousePositionY()
	{
		return mouseY;
	}

	private Player player1 = new Player(1, true, "");
	private Player player2 = new Player(2, false, "");

	private Ball [] balls = new Ball[16];
	private Rectangle table = new Rectangle(50, 50, 1400, 800, "BLUE");
	private Rectangle [] cushions = new Rectangle[4];
	private Ball [] pocketCircles = new Ball[6];
	private Rectangle [] pocketRectangles = new Rectangle[10];
	private Rectangle powerBarUnderlay = new Rectangle(1490, 50, 100, 800, "GREY");
	private Rectangle [] powerBar = new Rectangle[20];
	private Line trajectory;
	private int lineSize = 200;
	private int zCounter;	
	private int rotationalIncrement = 0;
	private int rotationalIncrementSlow = 0;
	private int powerIncrement = 1;

	private int turnNumber;
	private int [] queue = {player1.getPlayer(), player2.getPlayer()};	
	private Text powerText = new Text("Power Bar", 20, 1490, 35, "WHITE");
	private Text turn = new Text("Player 1's turn", 30, 50, 900, "WHITE");
	private Text player1ColourText = new Text("Player 1 = ", 30, 50, 940, "WHITE");
	private Text player2ColourText = new Text("Player 2 = ", 30, 50, 980, "WHITE");
	private Text player1Colour = new Text(player1.getColour(), 30, 200, 940, "WHITE");
	private Text player2Colour = new Text(player2.getColour(), 30, 200, 980, "WHITE");
	private Text turnText = new Text("Turn number = ", 30, 350, 900, "WHTIE");
	private Text turnNumberText = new Text("0", 30, 565, 900, "WHITE");

	private boolean firstCollision = false;
	private boolean foulType1; // one turn foul
	private boolean foulType2; // end game foul
	private boolean doubleShot;
	
	public void createBoard() // setup and adding to the JPanel.
	{
		balls[0] = new Ball(400, 450, 20, "WHITE");
		balls[1] = new Ball(1000, 450, 20, "BLACK");
		balls[2] = new Ball(960, 450, 20, "RED");
		balls[3] = new Ball(980, 440, 20, "RED");
		balls[4] = new Ball(1000, 430, 20, "RED");
		balls[5] = new Ball(1020, 460, 20, "RED");
		balls[6] = new Ball(1020, 420, 20, "RED");
		balls[7] = new Ball(1040, 430, 20, "RED");
		balls[8] = new Ball(1040, 490, 20, "RED");
		balls[9] = new Ball(980, 460, 20, "YELLOW");
		balls[10] = new Ball(1000, 470, 20, "YELLOW");
		balls[11] = new Ball(1020, 440, 20, "YELLOW");
		balls[12] = new Ball(1020, 480, 20, "YELLOW");
		balls[13] = new Ball(1040, 470, 20, "YELLOW");
		balls[14] = new Ball(1040, 450, 20, "YELLOW");
		balls[15] = new Ball(1040, 410, 20, "YELLOW");

		for (int i = 0; i < balls.length; i++)
		{
			balls[i].setActive(true);
		}

		cushions[0] = new Rectangle(50, 50, 1400, 20, "GREEN");
		cushions[1] = new Rectangle(50, 50, 20, 800, "GREEN");
		cushions[2] = new Rectangle(50, 830, 1400, 20, "GREEN");
		cushions[3] = new Rectangle(1430, 50, 20, 800, "GREEN");

		powerBar[0] = new Rectangle(1500, 815, 80, 30, "GREEN");
		powerBar[1] = new Rectangle(1500, 775, 80, 30, "GREEN");
		powerBar[2] = new Rectangle(1500, 735, 80, 30, "GREEN");
		powerBar[3] = new Rectangle(1500, 695, 80, 30, "GREEN");
		powerBar[4] = new Rectangle(1500, 655, 80, 30, "GREEN");
		powerBar[5] = new Rectangle(1500, 615, 80, 30, "ORANGE");
		powerBar[6] = new Rectangle(1500, 575, 80, 30, "ORANGE");
		powerBar[7] = new Rectangle(1500, 535, 80, 30, "ORANGE");
		powerBar[8] = new Rectangle(1500, 495, 80, 30, "ORANGE");
		powerBar[9] = new Rectangle(1500, 455, 80, 30, "ORANGE");
		powerBar[10] = new Rectangle(1500, 415, 80, 30, "RED");
		powerBar[11] = new Rectangle(1500, 375, 80, 30, "RED");
		powerBar[12] = new Rectangle(1500, 335, 80, 30, "RED");
		powerBar[13] = new Rectangle(1500, 295, 80, 30, "RED");
		powerBar[14] = new Rectangle(1500, 255, 80, 30, "RED");
		powerBar[15] = new Rectangle(1500, 215, 80, 30, "RED");
		powerBar[16] = new Rectangle(1500, 175, 80, 30, "RED");
		powerBar[17] = new Rectangle(1500, 135, 80, 30, "RED");
		powerBar[18] = new Rectangle(1500, 95, 80, 30, "RED");
		powerBar[19] = new Rectangle(1500, 55, 80, 30, "RED");

		pocketCircles[0] = new Ball(80, 80, 40, "BLACK");
		pocketCircles[1] = new Ball(1420, 80, 40, "BLACK");
		pocketCircles[2] = new Ball(80, 820, 40, "BLACK");
		pocketCircles[3] = new Ball(1420, 820, 40, "BLACK");
		pocketCircles[4] = new Ball(750, 80, 40, "BLACK");
		pocketCircles[5] = new Ball(750, 820, 40, "BLACK");

		pocketRectangles[0] = new Rectangle(60, 60, 40, 20, "BLACK");
		pocketRectangles[1] = new Rectangle(60, 60, 20, 40, "BLACK");
		pocketRectangles[2] = new Rectangle(1400, 60, 40, 20, "BLACK");
		pocketRectangles[3] = new Rectangle(1420, 60, 20, 40, "BLACK");
		pocketRectangles[4] = new Rectangle(60, 820, 40, 20, "BLACK");
		pocketRectangles[5] = new Rectangle(60, 800, 20, 40, "BLACK");
		pocketRectangles[6] = new Rectangle(1400, 820, 40, 20, "BLACK");
		pocketRectangles[7] = new Rectangle(1420, 800, 20, 40, "BLACK");
		pocketRectangles[8] = new Rectangle(730, 60, 40, 20, "BLACK");
		pocketRectangles[9] = new Rectangle(730, 820, 40, 20, "BLACK");
		
		
		trajectory = new Line(balls[0].getXPosition(), balls[0].getYPosition(), balls[0].getXPosition() + (lineSize * Math.cos((rotationalIncrement + rotationalIncrementSlow)*Math.PI/90)), balls[0].getYPosition() + (lineSize * Math.sin((rotationalIncrement + rotationalIncrementSlow)*Math.PI/90)), 2, "WHITE");

		addRectangle(table);
		
		addLine(trajectory);
		trajectory.setArrowSize(5);

		for (int i = 0; i < balls.length; i++)
		{
			addBall(balls[i]);
		}

		for (int i = 0; i < cushions.length; i++)
		{
			addRectangle(cushions[i]);
		}		

		addRectangle(powerBarUnderlay);		
		addRectangle(powerBar[0]);
		addText(powerText);
		addText(turn);
		addText(player1ColourText);
		addText(player2ColourText);
		addText(turnText);
		addText(turnNumberText);
		addText(player1Colour);
		addText(player2Colour);
		

		for (int i = 0; i < pocketCircles.length; i++)
		{
			addBall(pocketCircles[i]);
		}	
		
		for (int i = 0; i < pocketRectangles.length; i++)
		{
			addRectangle(pocketRectangles[i]);
		}
		
		for (int i = 0; i < cushions.length; i++)
		{
			addRectangle(cushions[i]);
		}

	}

	public void increasePower() // increases power
	{
		if (powerIncrement == 20)
			return;
		else			
			addRectangle(powerBar[powerIncrement]);
			powerIncrement++;	
		
	}

	public void decreasePower() // decreases power
	{
		if (powerIncrement == 1)
			return;
		else					
			removeRectangle(powerBar[powerIncrement-1]);
			powerIncrement--;
		
	}

	public void rotateClockwise() // rotates by 1 degrees
	{
		rotationalIncrement++;
		trajectory.setXEnd(balls[0].getXPosition() + (lineSize * Math.cos((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.setYEnd(balls[0].getYPosition() + (lineSize * Math.sin((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.recalculateArrowhead();
		
	}

	public void rotateAntiClockwise() // rotates by 1 degrees
	{
		rotationalIncrement--;
		trajectory.setXEnd(balls[0].getXPosition() + (lineSize * Math.cos((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.setYEnd(balls[0].getYPosition() + (lineSize * Math.sin((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.recalculateArrowhead();		
		
	}

	public void rotateClockwiseSlow() // rotates by 0.25 degrees
	{
		rotationalIncrementSlow++;
		trajectory.setXEnd(balls[0].getXPosition() + (lineSize * Math.cos((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.setYEnd(balls[0].getYPosition() + (lineSize * Math.sin((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.recalculateArrowhead();
		
	}

	public void rotateAntiClockwiseSlow() // rotates by 0.25 degrees
	{
		rotationalIncrementSlow--;
		trajectory.setXEnd(balls[0].getXPosition() + (lineSize * Math.cos((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.setYEnd(balls[0].getYPosition() + (lineSize * Math.sin((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.recalculateArrowhead();		
		
	}

	public void play () // plays game
	{
		int frames = 0;
		double endFrame = 1000;
		double velSF = 0.001; //velocity scaling factor		
		balls[0].setSpeedX((trajectory.getXEnd() - trajectory.getXStart()) * velSF * powerIncrement);
		balls[0].setSpeedY((trajectory.getYEnd() - trajectory.getYStart()) * velSF * powerIncrement);

		while (frames != endFrame)
		{
			double deceleration = frames/endFrame;
			
			for(int i = 0; i < balls.length; i++) // allows cushions to bounce
			{
				if (balls[i].getActive() == true && balls[i].getXPosition() <= cushions[1].getXPosition() + cushions[1].getWidth() || balls[i].getXPosition() >= cushions[3].getXPosition())
					balls[i].setSpeedX(-balls[i].getSpeedX());

				if (balls[i].getActive() == true && balls[i].getYPosition() <= cushions[0].getYPosition() + cushions[0].getHeight() || balls[i].getYPosition() >= cushions[2].getYPosition())
					balls[i].setSpeedY(-balls[i].getSpeedY());	
			}			

			for (int i = 0; i < balls.length; i++) // detects collisions + fouls
			{
				for ( int j = i + 1; j < balls.length; j++)
				{
					if (balls[i].collides(balls[j]) && balls[i].getActive() == true && balls[j].getActive() == true)
					{
						deflect(balls[i], balls[j]);
						
						firstCollision = true;

						if (player1.getColour() == "" || player2.getColour() == "" && balls[i].getColour() == "WHITE") // case no colours assigned 
							foulType1 = false;						
						else if (player1.getActive() && player1.getColour() != balls[j].getColour() && balls[i].getColour() == "WHITE")	// case p1 colour assigned but missed their colour
							foulType1 = true;
						else if (player2.getActive() && player2.getColour() != balls[j].getColour() && balls[i].getColour() == "WHITE")	// case p2 colour assigned but missed their colour																
							foulType1 = true;						
						else foulType1 = false;					
					}					
				}
			}			

			for (int i = 0; i < balls.length; i++) // moves balls at a speed depending on how many frames have passed
			{
				if (balls[i].getActive() == true)
				balls[i].move(balls[i].getSpeedX() * (1 - deceleration), balls[i].getSpeedY() * (1 - deceleration));				
			}
			
			for (int i = 0; i < balls.length; i++) // detects ball pot and calls method for the ball.
			{
				for (int j = 0; j < pocketCircles.length; j++)
				{
					if (balls[i].getActive() == true && balls[i].collides(pocketCircles[j]))
					pot(balls[i]);
				}				
			}

			frames++;

			// i am unable to get pause(); and repaint(); working to ave had to not animate sadly.

		}

		if (!firstCollision) // detects full miss
			foulType1 = true;		

		queueDeterminer();	//detects whos turn it id next
		
		if (queue[0] == 1) // detects if p1 to go next
		{
			turn.setText("Player 1's Turn");
			player1.setActive(true);
			player2.setActive(false);
		}
		else if (queue[0] == 2) // detects if p2 to go next
		{
			turn.setText("Player 2's Turn");
			player1.setActive(false);
			player2.setActive(true);
		}
		
		turnNumber++;
		
		turnNumberText.setText(String.valueOf(turnNumber)); // counts turn number
	
		trajectory.setLinePosition(balls[0].getXPosition(), balls[0].getYPosition(), balls[0].getXPosition() + (200 * Math.cos((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)), balls[0].getYPosition() + (200 * Math.sin((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));	// attaches trajectory arrow to white ball	

		reset(); // sets vel to 0 for all balls and resets fouls
	}

	public void queueDeterminer ()
	{
		if (doubleShot && !foulType1 && player1.getActive()) //double shot case
		{
			queue[0] = player1.getPlayer();
			queue[1] = player2.getPlayer();
		}
		else if (doubleShot && !foulType1 && player2.getActive()) //double shot case
		{
			queue[0] = player2.getPlayer();
			queue[1] = player1.getPlayer();
		}			
		else if (queue[0] == queue[1] && player1.getActive() && !foulType1) //foul on the previous turn
		{
			queue[0] = player1.getPlayer();
			queue[1] = player2.getPlayer();
		}		
		else if (queue[0] == queue[1] && player2.getActive() && !foulType1) //foul on the previous turn
		{
			queue[0] = player2.getPlayer();
			queue[1] = player1.getPlayer();
		}
		else if (!foulType1 && player1.getActive()) //no foul - normal case
		{
			queue[0] = player2.getPlayer();
			queue[1] = player1.getPlayer();
		}
		else if (!foulType1 && player2.getActive()) //no foul - normal case
		{
			queue[0] = player1.getPlayer();
			queue[1] = player2.getPlayer();
		}
		else if (foulType1 && player1.getActive()) //foul type 1 case
		{
			queue[0] = player2.getPlayer();
			queue[1] = player2.getPlayer();
		}
		else if (foulType1 && player2.getActive()) //foul type 1 case
		{
			queue[0] = player1.getPlayer();
			queue[1] = player1.getPlayer();
		}
	}

	public void pot(Ball b) //detects what colour ball was pot and calls function associated with that colour.
	{			

		if (b.getColour() == "RED")
		{
			redBallPot(b);
			doubleShot = true;
		}

		if (b.getColour() == "YELLOW")
		{
			yellowBallPot(b);
			doubleShot = true;
		}

		if (b.getColour() == "WHITE")
		{
			whiteReset();
			foulType1 = true;
		}

		if (b.getColour() == "BLACK")
		{
			blackBallPot();
		}			
	
	}

	public void blackBallPot()
	{
		foulType2 = false;

		for (int i = 0; i < balls.length; i++)
		{
			if (player1.getActive() && player1.getColour() == balls[i].getColour() && balls[i].getActive())	// detects if all balls are pot for p1		
				foulType2 = true;
			else if (player2.getActive() && player2.getColour() == balls[i].getColour() && balls[i].getActive()) // detects if all balls are pot for p2
				foulType2 = true;
		}

		if (foulType2 & player1.getActive())
			p2Wins();
		else if (foulType2 & player2.getActive())
			p1Wins();
		else if (!foulType2 & player1.getActive())
			p1Wins();
		else if (!foulType2 & player2.getActive())
			p2Wins();
		
	}

	public void p1Wins() // win condition satified for p1
	{
		Text p1W = new Text("Player 1 wins!", 30, 800, 900, "WHITE");

		for (int i = 0; i < balls.length; i++)
		{
			if (!balls[i].getActive())
				removeBall(balls[i]);
		}
		
		addText(p1W);

	}

	public void p2Wins() // win condition satified for p2
	{
		Text p2W = new Text("Player 2 wins!", 30, 800, 900, "WHITE");

		for (int i = 0; i < balls.length; i++)
		{
			if (!balls[i].getActive())
				removeBall(balls[i]);
		}
		
		addText(p2W);

	}

	public void redBallPot(Ball b)
	{

		boolean firstPot = true; // detects if colours need assigning to players
		boolean lastPot = true; // detects if player needs his colour reassigning to black.

		for (int i = 2; i < balls.length; i++)
		{			
			if (!balls[i].getActive())
			{
				firstPot = false; // if any balls are not active, change condition.
			}
		}
		
		b.setActive(false);
		removeBall(b);
		
		if(firstPot && player1.getActive()) // if p1 pots first
		{
			player1.setColour("RED");
			player2.setColour("YELLOW");
			player1Colour.setText(player1.getColour());
			player2Colour.setText(player2.getColour());			
		}

		if(firstPot && player2.getActive())
		{
			player1.setColour("YELLOW");
			player2.setColour("RED");
			player1Colour.setText(player1.getColour());
			player2Colour.setText(player2.getColour());						
		}

		for (int i = 0; i < balls.length; i++)
		{
			if (balls[i].getActive() && balls[i].getColour() == "RED")
				lastPot = false;  // if any red balls are active, change condition
		}

		if (lastPot && player1.getActive())
			player1.setColour("BLACK");

		if (lastPot && player2.getActive())
			player2.setColour("BLACK");

		
	}

	public void yellowBallPot(Ball b)
	{
		boolean firstPot = true; // detects if colours need assigning to players
		boolean lastPot = true; // detects if player needs his colour reassigning to black.

		for (int i = 2; i < balls.length; i++)
		{
			if (!balls[i].getActive())
			{
				firstPot = false; // if any balls are not active, change condition.
			}
		}

		b.setActive(false);
		removeBall(b);
		
		if(firstPot && player1.getActive()) // if p1 pots first
		{
			player1.setColour("YELLOW");
			player2.setColour("RED");
			player1Colour.setText(player1.getColour());
			player2Colour.setText(player2.getColour());			
		}

		if(firstPot && player2.getActive()) // if p2 pots first
		{
			player1.setColour("RED");
			player2.setColour("YELLOW");
			player1Colour.setText(player1.getColour());
			player2Colour.setText(player2.getColour());						
		}
			
		for (int i = 0; i < balls.length; i++)
		{
			if (balls[i].getActive() && balls[i].getColour() == "YELLOW")
				lastPot = false; // if any yellow balls are active, change condition
		}

		if (lastPot && player1.getActive())
			player1.setColour("BLACK");

		if (lastPot && player2.getActive())
			player2.setColour("BLACK");
	}

	public void whiteReset() // sets the white back to starting position
	{
		balls[0].setXPosition(400);
		balls[0].setYPosition(450);
		balls[0].setSpeedX(0);
		balls[0].setSpeedY(0);
	}

	public void reset() // sets all balls to still and all rule to be unactive.
	{
		for (int i = 0; i < balls.length; i++)
		{
			balls[i].setSpeedX(0);
			balls[i].setSpeedY(0);
		}
		firstCollision = false;
		foulType1 = false;
		foulType2 = false;
		doubleShot = false;
	}

	public void deflect(Ball b1, Ball b2)
	{
		double xPosition1, xPosition2, yPosition1, yPosition2;
		double xSpeed1, xSpeed2, ySpeed1, ySpeed2;

		xPosition1 = b1.getXPosition();
		yPosition1 = b1.getYPosition();
		xSpeed1 = b1.getSpeedX();
		ySpeed1 = b1.getSpeedY();
		xPosition2 = b2.getXPosition();
		yPosition2 = b2.getYPosition();
		xSpeed2 = b2.getSpeedX();
		ySpeed2 = b2.getSpeedY();

		// Calculate initial momentum of the balls... We assume unit mass here.
		double p1InitialMomentum = Math.sqrt(xSpeed1 * xSpeed1 + ySpeed1 * ySpeed1);
		double p2InitialMomentum = Math.sqrt(xSpeed2 * xSpeed2 + ySpeed2 * ySpeed2);
		// calculate motion vectors
		double[] p1Trajectory = {xSpeed1, ySpeed1};
		double[] p2Trajectory = {xSpeed2, ySpeed2};
		// Calculate Impact Vector
		double[] impactVector = {xPosition2 - xPosition1, yPosition2 - yPosition1};
		double[] impactVectorNorm = normalizeVector(impactVector);
		// Calculate scalar product of each trajectory and impact vector
		double p1dotImpact = Math.abs(p1Trajectory[0] * impactVectorNorm[0] + p1Trajectory[1] * impactVectorNorm[1]);
		double p2dotImpact = Math.abs(p2Trajectory[0] * impactVectorNorm[0] + p2Trajectory[1] * impactVectorNorm[1]);
		// Calculate the deflection vectors - the amount of energy transferred from one ball to the other in each axis
		double[] p1Deflect = { -impactVectorNorm[0] * p2dotImpact, -impactVectorNorm[1] * p2dotImpact };
		double[] p2Deflect = { impactVectorNorm[0] * p1dotImpact, impactVectorNorm[1] * p1dotImpact };
		// Calculate the final trajectories
		double[] p1FinalTrajectory = {p1Trajectory[0] + p1Deflect[0] - p2Deflect[0], p1Trajectory[1] + p1Deflect[1] - p2Deflect[1]};
		double[] p2FinalTrajectory = {p2Trajectory[0] + p2Deflect[0] - p1Deflect[0], p2Trajectory[1] + p2Deflect[1] - p1Deflect[1]};
		// Calculate the final energy in the system.
		double p1FinalMomentum = Math.sqrt(p1FinalTrajectory[0] * p1FinalTrajectory[0] + p1FinalTrajectory[1] * p1FinalTrajectory[1]);
		double p2FinalMomentum = Math.sqrt(p2FinalTrajectory[0] * p2FinalTrajectory[0] + p2FinalTrajectory[1] * p2FinalTrajectory[1]);
		// Scale the resultant trajectories if we've accidentally broken the laws of physics.
		double mag = (p1InitialMomentum + p2InitialMomentum) / (p1FinalMomentum + p2FinalMomentum);
		// Calculate the final x and y speed settings for the two balls after collision.
		xSpeed1 = p1FinalTrajectory[0] * mag;
		ySpeed1 = p1FinalTrajectory[1] * mag;
		xSpeed2 = p2FinalTrajectory[0] * mag;
		ySpeed2 = p2FinalTrajectory[1] * mag;

		b1.setSpeedX(xSpeed1);
		b1.setSpeedY(ySpeed1);
		b2.setSpeedX(xSpeed2);
		b2.setSpeedY(ySpeed2);
	}
	/**
	* Converts a vector into a unit vector.
	* Used by the deflect() method to calculate the resultnt direction after a collision.
	*/
	private double[] normalizeVector(double[] vec)
	{
		double mag = 0.0;
		int dimensions = vec.length;

		double[] result = new double[dimensions];

		for (int i=0; i < dimensions; i++)
			mag += vec[i] * vec[i];
		mag = Math.sqrt(mag);

		if (mag == 0.0)
		{
			result[0] = 1.0;
			for (int i=1; i < dimensions; i++)
			result[i] = 0.0;
		}
		else
		{
			for (int i=0; i < dimensions; i++)
			result[i] = vec[i] / mag;
		}
		return result;
	}

	public void changeLineSize() // extents arrow for an easier shot
	{
		if(lineSize == 200)
			lineSize = 300;
		else if (lineSize == 300)
			lineSize = 400;
		else if (lineSize == 400)
			lineSize = 100;
		else if(lineSize == 100)
			lineSize = 200;

		trajectory.setXEnd(balls[0].getXPosition() + (lineSize * Math.cos((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.setYEnd(balls[0].getYPosition() + (lineSize * Math.sin((rotationalIncrement + 0.25*rotationalIncrementSlow)*Math.PI/90)));
		trajectory.recalculateArrowhead();
	}
	
}
	
	