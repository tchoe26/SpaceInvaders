//Basic Game Application
//Version 2
// Basic Object, Image, Movement
// Astronaut moves to the right.
// Threaded

//K. Chun 8/2018

//*******************************************************************************
//Import Section
//Add Java libraries needed for the game
//import java.awt.Canvas;

//Graphics Libraries
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static java.lang.String.valueOf;

//*******************************************************************************
// Class Definition Section

public class BasicGameApp implements Runnable, KeyListener {

	//Variable Definition Section
	//Declare the variables used in the program
	//You can set their initial values too

	//Sets the width and height of the program window
	final int WIDTH = 500;
	final int HEIGHT = 700;

	//Declare the variables needed for the graphics
	public JFrame frame;
	public Canvas canvas;
	public JPanel panel;
	public Color c = new Color(255, 255, 255);
	public int scoreCounter1 = 0;
	public int scoreCounter2 = 0;
	public BufferStrategy bufferStrategy;
	public Image spaceshipPic;
	public Image blackBackground;
	public Image invaderPic;
	public Image winScreen1;
	public Image winScreen2;
	public int bulletCounter;
	private final Astronaut spaceship;
	Astronaut[][] invader = new Astronaut[9999][9999];
	Astronaut[] bullet = new Astronaut[9999];
	public Image bulletPic;
	int bulletRows = 5;
	int bulletColumns=13;
	boolean isCooldown;

	int timeSinceShot;
	int shootTime;

	// Main method definition
	// This is the code that runs first and automatically
	public static void main(String[] args) {
		BasicGameApp ex = new BasicGameApp();   //creates a new instance of the game
		new Thread(ex).start();                 //creates a threads & starts up the code in the run( ) method
	}


	void shootBullet() {
		bullet[bulletCounter].xpos = spaceship.xpos + 45;
		bullet[bulletCounter].ypos = spaceship.ypos + 10;
		bullet[bulletCounter].dy = -50;
		bulletCounter++;
		shootTime = (int) System.currentTimeMillis();
	}

	// Constructor Method
	// This has the same name as the class
	// This section is the setup portion of the program
	// Initialize your variables and construct your program objects here.
	public BasicGameApp() {

		setUpGraphics();

		//spaceship
		spaceshipPic = Toolkit.getDefaultToolkit().getImage("spaceShip.png"); //load the picture
		spaceship = new Astronaut(250, 600, 100, 100, 0, 0, false);
		spaceship.isControllable = true;
		blackBackground = Toolkit.getDefaultToolkit().getImage("blackBackground.png");

		//bullet
		bulletPic = Toolkit.getDefaultToolkit().getImage("bullet.png");
		for (int i = 0; i < bullet.length; i++) {
			bullet[i] = new Astronaut(-100, 500, 2, 10, 0, 0, false);
		}
		//invaders
		invaderPic = Toolkit.getDefaultToolkit().getImage("spaceinvader2.png");
		for (int i = 0; i < bulletRows; i++) {
			for (int j = 0; j < bulletColumns; j++) {
				invader[i][j] = new Astronaut(10 + (41 * j), 50 + (50 * i), 40, 30, 0, 0, false);
			}
		}
		winScreen1 = Toolkit.getDefaultToolkit().getImage("winScreen1.png");
		winScreen2 = Toolkit.getDefaultToolkit().getImage("winScreen2.png");

		//10 objects


	}// BasicGameApp()


//*******************************************************************************
//User Method Section
//
// put your code to do things here.

	// main thread
	// this is the code that plays the game after you set things up
	public void run() {

		//for the moment we will loop things forever.
		while (true) {

			moveThings();  //move all the game objects
			render();  // paint the graphics
			pause(20); // sleep for 10 ms
		}
	}

	public void pause(int time) {
		//sleep
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {

		}
	}

	public void moveThings() {

		//invaders attack (move down)


		timeSinceShot = (int)(System.currentTimeMillis()-shootTime);
		spaceship.move();
		//limit movement to screen
		if (spaceship.xpos<-25) {
			spaceship.xpos=-25;
		}
		if (spaceship.xpos>525-spaceship.width) {
			spaceship.xpos=525-spaceship.width;
		}
		for (int t=0; t<bulletRows; t++) {
			for (int v=0; v<bulletColumns; v++) {
				invader[t][v].wrap();
				//invader[t][v].dx=3;

			}
		}
        for (Astronaut astronaut : bullet) {
            astronaut.move();
            //bullet life (optimization)
            for (int z = 0; z < bullet.length; z++) {
                if (astronaut.ypos < -20) {
                    astronaut.isAlive = false;
                    break;
                }
            }

            //shooting invaders
            for (int j = 0; j < bulletRows; j++) {
                for (int k = 0; k < bulletColumns; k++) {
                    if (astronaut.rec.intersects(invader[j][k].rec) && invader[j][k].isAlive && astronaut.isAlive) {
                        invader[j][k].isAlive = false;
                        astronaut.isAlive = false;

                    }

                }
            }

        }

	}


	//Graphics setup method
	private void setUpGraphics() {
		frame = new JFrame("Application Template");   //Create the program window or frame.  Names it.

		panel = (JPanel) frame.getContentPane();  //sets up a JPanel which is what goes in the frame
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));  //sizes the JPanel
		panel.setLayout(null);   //set the layout

		// creates a canvas which is a blank rectangular area of the screen onto which the application can draw
		// and trap input events (Mouse and Keyboard events)
		canvas = new Canvas();
		canvas.setBounds(0, 0, WIDTH, HEIGHT);
		canvas.setIgnoreRepaint(true);
		canvas.addKeyListener(this);

		panel.add(canvas);  // adds the canvas to the panel.

		// frame operations
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //makes the frame close and exit nicely
		frame.pack();  //adjusts the frame and its contents so the sizes are at their default or larger
		frame.setResizable(false);   //makes it so the frame cannot be resized
		frame.setVisible(true);      //IMPORTANT!!!  if the frame is not set to visible it will not appear on the screen!

		// sets up things so the screen displays images nicely.
		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();
		canvas.requestFocus();
		//System.out.println("DONE graphic setup");

	}


	//paints things on the screen using bufferStrategy
	private void render() {
		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
		g.clearRect(0, 0, WIDTH, HEIGHT);

		if (scoreCounter1 < 10 && scoreCounter2 < 10) {
			//draw the image of the astronaut
			g.drawImage(blackBackground, 0, 0, WIDTH, HEIGHT, null);

			g.drawImage(spaceshipPic, spaceship.xpos, spaceship.ypos, spaceship.width, spaceship.height, null);

            for (Astronaut astronaut : bullet) {
                if (astronaut.isAlive) {
                    g.drawImage(bulletPic, astronaut.xpos, astronaut.ypos, astronaut.width, astronaut.height, null);
                }
            }
			for (int i = 0; i < bulletRows; i++) {
				for (int j = 0; j < bulletColumns; j++) {
					if (invader[i][j].isAlive) {
						g.drawImage(invaderPic, invader[i][j].xpos, invader[i][j].ypos, invader[i][j].width, invader[i][j].height, null);
					}

				}
			}
		}
		g.dispose();
		bufferStrategy.show();
	}


	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode()==65) {
			spaceship.spaceshipIsLeft = true;
		}
		if (e.getKeyCode()==68) {
			spaceship.spaceshipIsRight = true;
		}
		if (e.getKeyCode() ==32 && !isCooldown && (timeSinceShot>150 || timeSinceShot<1)) {
			shootBullet();
			
			isCooldown=true;
		}

		//reset game
		if (e.getKeyCode() ==82) {
			System.out.println("test");
			int temp1 = ((int)(Math.random()*bulletRows));
			int temp2 = ((int)(Math.random()*bulletColumns));
			invader[temp1][temp2].dx = (int)(Math.random()*8)-4;
			System.out.println(invader[temp1][temp2].dx );
			invader[temp1][temp2].dy = 5;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode()==65) {
			spaceship.spaceshipIsLeft = false;
		}
		if (e.getKeyCode()==68) {
			spaceship.spaceshipIsRight = false;
		}
		if (e.getKeyCode() ==32) {
			isCooldown=false;
		}

	}




}
