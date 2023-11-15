//Name: Kevin Wu
// Description: Snake game, you eat food to grow longer, you can collect power ups to gain extra lives, eating food can generate obstacles, you can control the snake with arrow keys, wasd, ijkl
//Due date: 1/27/2021
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.io.File;

@SuppressWarnings("serial")
public class snake_Game extends JPanel implements Runnable, KeyListener, ActionListener, ItemListener {
	
	//jframe and thread variables and general game variables
    static JFrame frame;
	JPanel myPanel;
	Thread thread;
	static JMenuBar mainMenu = new JMenuBar ();
	boolean collision = false;
	boolean running = false;
	int colourSet = 1;

	
	//game board variables
	int gridSize = 25;
	int screenWidth = 400;
	int screenHeight = 400;
	Color backgroundColor = new Color(0,255,255);
	
	//snake body related variables
	Rectangle rect = new Rectangle(0, 0, gridSize, gridSize);
	int snakeX[] = new int[240];
	int snakeY[] = new int[240];
	boolean up, down, left, right;
	int score = 0;
	int snakeLength = 2;  
	double lives = 1;
	Color snakeBodyColor = new Color(0,255,0);
	
	
	
	int speed = 250; //medium speed for thread.sleep 
	
	//food variables
	int foodX;
	int foodY;
	Color foodColor = new Color(0,0,255);
	
	
	//walls variables
	boolean obstacleOnOFF = true;
	int obstacleX;
	int obstacleY;
	Rectangle[] walls = new Rectangle[200];
	//using to check if food generates inside wall
	int[] wallX = new int[240];
	int[] wallY = new int[240];

	
	//powerup variables
	int powerUpX; 
	int powerUpY;
	//int powerUpGenerationCounter = 0;
	boolean drawPowerUpOrNot = false;
	boolean food1stTimeDraw = true;
	Color powerUpColor = new Color(255,0,0);


	
	//helps switch between screens
	static int menuScreen = 0;
	

	//Jbuttons and labels
	//start screen
	JButton startButton;
	JButton settingsButton;
	JButton about;
	JButton exitButton;
	JLabel titleLabel;
	
	//setting screen
	JButton obstacleButton;
	JButton fasterSlowerButton;
	JButton colourSwitcherButton;
	JButton backButton;
	JLabel settingJLabel;
	
	//about screen
	JLabel aboutJLabel;
	JLabel instructionsJLabel1;
	JLabel instructionsJLabel2;
	JButton aboutBack;
	
	//game over screen
	JLabel gameOverJLabel;
	JLabel gameOverScore;
	JButton gameOverNewGameButton;
	JButton gameOverMenuButton;
	
	//screen stuff
	int menuScreenPrev;
	int menuScreen1stTime = 0;
	int settingScreen1stTime = 0;
	int gameOverScreen1stTime = 0;
	int aboutScreen1stTime = 0;
	int gameplayedtime = 0;
	String gameoverString = "Game Over";
	String winnerString = "You Won!";
	boolean winClause = false;

	
	//test vars

	
	
	//constructor for snake game
	public snake_Game() {
		//sets up jpanel
		myPanel = new JPanel ();
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setVisible(true);

		JMenuItem newOption, exitOption;
		newOption = new JMenuItem ("New Game");
		exitOption = new JMenuItem ("Exit");


		// Set up the Game Menu
		JMenu gameMenu = new JMenu ("OPTIONS");
		// Add each MenuItem to the Game Menu (with a separator)
		gameMenu.add (newOption);
		gameMenu.addSeparator ();
		gameMenu.add (exitOption);


		mainMenu.add (gameMenu);

		newOption.setActionCommand ("New Game");
		newOption.addActionListener (this);
		exitOption.setActionCommand ("Exit");
		exitOption.addActionListener (this);

	
		setFocusable (true); // Need this to set the focus to the panel in order to add the keyListener
		addKeyListener (this);

		
		//starts thread
		thread = new Thread(this);
		thread.start();
		try {
			musicMethod("Background Music.wav");
		} 
		catch (Exception e1) {
		}
	}
	
	//Description:Starts the game once the thread gets started
	//Parameters: none
	//Return: none
	@Override
	public void run() {
		foodGeneration();
		while(true) {
			//main game loop
			update();
			this.repaint();
			try {
				Thread.sleep(speed);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//Description: gets called everytime the run method is called, 
	//			   it checks if the game is running and if so it calls the required methods to make the game run such as the move method, 
	//             checks if new food needs to be spawned and checks for collisions
	//Parameters: none
	//Return: none
	public void update() {
		if (menuScreen == 1) {
			if (running) {
				if (collision == false) {
					//checks for win
					winCheck();
					if (winClause) {
						collision = true;
					}
					
					//moves the snake body
					move();
					
					//checks if food is eaten before generating new food
					if (foodEatCheck() == true) {
						foodGeneration();
						if (obstacleOnOFF) {
							obstacleGeneration();
						}
						snakeLength = snakeLength + 1;
						score = score + 1;
					}
					
					//generates powerup 
					//change to 30 later setting to 10 for testing
					do {
						if (score >= 2) {
							//first power up block will spawn after two points have been collected
							if (score == 2 && food1stTimeDraw) {
								extraLifePowerUp();
								drawPowerUpOrNot = true;
								food1stTimeDraw = false;
							}
							// then every multiple of 5 another power up block will spawn
							else if (score  % 5 == 0 && food1stTimeDraw) {
								extraLifePowerUp();
								drawPowerUpOrNot = true;
								food1stTimeDraw = false;
							}	
						}
						//used for debugging
						System.out.println("Lives " + lives);
					} while (powerUpEatCheck());
					

					//checks if it goes out of bounds on the border
					keepInBound();
					
					
					//checks for collision with obstacles
					if (obstacleOnOFF) {
						for(int i = 0; i < score; i++)
							checkCollision(walls[i]);
						}	
					}
				//if collision stops game and movement, sleeps the thread for 10 seconds to allow user to see what they messed up
				else if (collision) {
					System.out.println("Gameover");
					left = false;
					right = false;
					down = false;
					up = false;
					running = false;
					try {
						thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					menuScreen = 3;
					menuScreenPrev = 3;
					gameOverScreen1stTime = 0;
				}
			}
			
		}

	}
	
	//description: checks if user has acheived a full board with no obstacles (240 score) or 120 with obstacles half of board filled with obstacles
	//parameters: none
	//return: none
	public void winCheck() {
		if (obstacleOnOFF) {
			if (score == 240) {
				winClause = true;
			}
		}
		else {
			if (score == 120) {
				winClause = true;
			}
		}
	}

	//Description: Draw all the images for the different screens, draws all the menu screens and updates the snake and its body movement
	//Parameters: graphics g object
	//Return: none
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

				
		
		//start screen
		if (menuScreen == 0) {
			if (menuScreen1stTime == 0) {
				//if coming from setting screen
				if (menuScreenPrev == 2) {
					remove(settingJLabel);
					remove(backButton);
					remove(obstacleButton);
					remove(colourSwitcherButton);
					remove(fasterSlowerButton);
					settingScreen1stTime = 0;
					menuScreenPrev = -1;
				}
				//if coming from gameover/win screen
				if (menuScreenPrev == 3) { 
					remove(gameOverJLabel);
					remove(gameOverScore);
					remove(gameOverNewGameButton);
					remove(gameOverMenuButton);
					gameOverScreen1stTime = 0;
					menuScreenPrev = -1;
					
					snakeX = new int[200];
					snakeY = new int[200];
					score = 0;
					snakeLength = 2;
					lives = 1;
					rect = new Rectangle(0, 0, gridSize, gridSize);
					running = true;
					collision = false;
					left = false;
					right = false;
					down = false;
					up = false;
					
				}
				//if coming from about menu screen
				if (menuScreenPrev == 4) {
					remove(aboutJLabel);
					remove(instructionsJLabel1);
					remove(aboutBack);
					aboutScreen1stTime = 0;
					menuScreenPrev = -1;
				}
				setLayout(new GridLayout(5,1));
				
				//adds the labels and buttons needed for home menu screen
				titleLabel = new JLabel("Snake", JLabel.CENTER);
				titleLabel.setOpaque(true);
				titleLabel.setForeground(Color.WHITE);
				titleLabel.setBackground(Color.BLACK);
				add(titleLabel);
				
				startButton = new JButton("Start");
				startButton.setActionCommand("Start");
				startButton.setForeground(new Color(0,255,255));
				startButton.setBackground(Color.BLACK);
				startButton.addActionListener(this);
				
				add(startButton);
				
				
				settingsButton = new JButton("Settings");
				settingsButton.setActionCommand("Settings");
				settingsButton.setForeground(new Color(255,99,71));
				settingsButton.setBackground(Color.BLACK);
				settingsButton.addActionListener(this);
				add(settingsButton);
				
				about  = new JButton("About");
				about.setActionCommand("About");
				about.setForeground(new Color(0,255,255));
				about.setBackground(Color.BLACK);
				about.addActionListener(this);
				add(about);
				
				exitButton = new JButton("Exit");
				exitButton.setActionCommand("Exit");
				exitButton.setForeground(new Color(255,99,71));
				exitButton.setBackground(Color.BLACK);
				exitButton.addActionListener(this);
				add(exitButton);
								
				
				
				exitButton.setFont(new Font("Arial", Font.PLAIN, 30));
				about.setFont(new Font("Arial", Font.PLAIN, 30));
				settingsButton.setFont(new Font("Arial", Font.PLAIN, 30));
				startButton.setFont(new Font("Arial", Font.PLAIN, 30));
				titleLabel.setFont(new Font("Arial", Font.PLAIN, 50));
				
				menuScreen1stTime ++;
			}
		
		}
		//game screen
		if (menuScreen == 1) {
			//sets the title screen buttons and label invisible
			titleLabel.setVisible(false);
			startButton.setVisible(false);
			exitButton.setVisible(false);
			about.setVisible(false);
			settingsButton.setVisible(false);
			g2.setColor(backgroundColor);
			g2.fillRect(0, 0, screenWidth, screenHeight);

			// creates a grid to visualize
			g.setColor(Color.GRAY);
			for (int i = 0; i < screenHeight/gridSize; i++) {
				g.drawLine(i * gridSize, 0, i * gridSize, screenHeight);
				g.drawLine(0, i * gridSize, screenWidth, i * gridSize);

			}
			
			
			//draws food
			g2.setColor(foodColor);
			g2.fillRect(foodX, foodY, gridSize, gridSize);
			//draws snake
			for (int i = 0; i < snakeLength; i++) {
				if (i == 1) {
					g.setColor(Color.BLACK);
					g.fillRect(snakeX[i], snakeY[i], gridSize, gridSize);
				}
				else {
					g.setColor(snakeBodyColor);
					g.fillRect(snakeX[i], snakeY[i], gridSize, gridSize);
				}
			}
			
			
			//draws obstacle
			if (obstacleOnOFF) {
				g2.setColor(Color.GRAY);
				for (int i = 0; i < score; i++) {
					g2.fill(walls[i]);
				}
			}
			
			
			//draws powerup
			if (drawPowerUpOrNot) {
				g2.setColor(powerUpColor);
				g2.fillRect(powerUpX, powerUpY, gridSize, gridSize);
			}
			
			//draws score
			g.setColor(Color.BLACK);
			g2.setFont(new Font("Arial", Font.PLAIN, 20));
			g2.drawString("Score: " + score,  300, 25 );
			
			//draws lives
			g.setColor(Color.BLACK);
			g2.setFont(new Font("Arial", Font.PLAIN, 20));
			g2.drawString("Lives: " + lives,  300, 50 );
			
		}
		
		
		// options screen
		if (menuScreen == 2) {
			if (settingScreen1stTime == 0 ) {
				setLayout(new GridLayout(0,1));
				//removes buttons and labels from previous screen
				remove(titleLabel);
				remove(startButton);
				remove(exitButton);
				remove(about);
				remove(settingsButton);
				
				//generates all the buttons for that screen
				backButton = new JButton("Back");
				backButton.setActionCommand("Back");
				backButton.setForeground(new Color(0,164,204));
				backButton.setBackground(Color.BLACK);
				backButton.addActionListener(this);
				
				
				colourSwitcherButton  = new JButton("Colour Switcher");
				colourSwitcherButton.setActionCommand("Colour Switcher");
				colourSwitcherButton.setForeground(new Color(249,87,0));
				colourSwitcherButton.setBackground(Color.BLACK);
				colourSwitcherButton.addActionListener(this);
				
				
				fasterSlowerButton = new JButton("Cycle Speed");
				fasterSlowerButton.setActionCommand("Faster/Slower");
				fasterSlowerButton.setForeground(new Color(0,164,204));
				fasterSlowerButton.setBackground(Color.BLACK);
				fasterSlowerButton.addActionListener(this);
				
				
				obstacleButton = new JButton("Obstacles ON/OFF");
				obstacleButton.setActionCommand("Obstaacles On/Off");
				obstacleButton.setForeground(new Color(249,87,0));
				obstacleButton.setBackground(Color.BLACK);
				obstacleButton.addActionListener(this);
				
				settingJLabel = new JLabel("Settings", JLabel.CENTER);
				settingJLabel.setOpaque(true);
				settingJLabel.setForeground(Color.WHITE);
				settingJLabel.setBackground(Color.BLACK);
				

				//adds the buttons to the screen
				add(backButton, 4, 0);
				add(colourSwitcherButton, 3, 0);
				add(fasterSlowerButton, 2, 0);
				add(obstacleButton, 1, 0);
				add(settingJLabel, 0, 0);

				
				backButton.setFont(new Font("Arial", Font.PLAIN, 30));
				colourSwitcherButton.setFont(new Font("Arial", Font.PLAIN, 30));
				fasterSlowerButton.setFont(new Font("Arial", Font.PLAIN, 30));
				obstacleButton.setFont(new Font("Arial", Font.PLAIN, 30));
				settingJLabel.setFont(new Font("Arial", Font.PLAIN, 60));
				
				settingScreen1stTime++;
	
			}
		}
		
		
		
		//gameover screen
		if (menuScreen == 3) {
			
			if (gameOverScreen1stTime == 0 ) {
				setLayout(new GridLayout(0,1));
				//removes buttons and labels from previous screen
				remove(titleLabel);
				remove(startButton);
				remove(exitButton);
				remove(about);
				remove(settingsButton);
				
				//if he won use "you won" instead of "game over"
				if (winClause) {
					gameOverJLabel = new JLabel("You Win", JLabel.CENTER);
					gameOverJLabel.setOpaque(true);
					gameOverJLabel.setForeground(Color.GREEN);
					gameOverJLabel.setBackground(Color.BLACK);
					winClause = false;
				}
				else {
					gameOverJLabel = new JLabel("Game Over", JLabel.CENTER);
					gameOverJLabel.setOpaque(true);
					gameOverJLabel.setForeground(Color.RED);
					gameOverJLabel.setBackground(Color.BLACK);
				}

				//shows your final score
				String scoreString = String.valueOf(score);
				gameOverScore = new JLabel("SCORE: " + scoreString, JLabel.CENTER);
				gameOverScore.setOpaque(true);
				gameOverScore.setForeground(Color.WHITE);
				gameOverScore.setBackground(Color.BLACK);

				//creates new game button
				gameOverNewGameButton = new JButton("New Game");
				gameOverNewGameButton.setActionCommand("New Game");
				gameOverNewGameButton.addActionListener(this);
				gameOverNewGameButton.setForeground(Color.WHITE);
				gameOverNewGameButton.setBackground(Color.BLACK);
				
				//creating home menu button
				gameOverMenuButton = new JButton("Menu");
				gameOverMenuButton.setActionCommand("Menu");
				gameOverMenuButton.addActionListener(this);
				gameOverMenuButton.setForeground(Color.WHITE);
				gameOverMenuButton.setBackground(Color.BLACK);
				
				//adds all the buttons to the screen
				add(gameOverMenuButton, 3, 0);
				add(gameOverNewGameButton, 2, 0);
				add(gameOverScore, 1, 0);
				add(gameOverJLabel, 0, 0);
				gameOverScore.setFont(new Font("Arial", Font.PLAIN, 40));
				gameOverJLabel.setFont(new Font("Arial", Font.PLAIN, 60));
				gameOverMenuButton.setFont(new Font("Arial", Font.PLAIN, 40));
				gameOverNewGameButton.setFont(new Font("Arial", Font.PLAIN, 40));
				

				gameplayedtime++;
				gameOverScreen1stTime++;
			}
			
		}
		
		
		//about screen / instructions screen
		if (menuScreen == 4) {
			if (aboutScreen1stTime == 0) {
				setLayout(new GridLayout(0,1));
				//removes buttons and labels from previous screen
				remove(titleLabel);
				remove(startButton);
				remove(exitButton);
				remove(about);
				remove(settingsButton);
				
				//generates all the labels and buttons needed for the about screen screen
				aboutJLabel = new JLabel("ABOUT", JLabel.CENTER);
				aboutJLabel.setOpaque(true);
				aboutJLabel.setForeground(Color.WHITE);
				aboutJLabel.setBackground(Color.BLACK);

				
				
				instructionsJLabel1 = new JLabel("<html>MADE BY: KEVIN WU<br>INSTRUCTIONS: <br>- Control the snake with arrow keys, WASD or IJKL <br>- Collect food to earn points and increase snake length <br>- Hitting walls (can be turned off), snake's body or border will end tha game <br>- Collect 10 Power Ups blocks to gain an extra life <br>- Power up can save snake from colliding with walls, body and border<html>", JLabel.CENTER);
				instructionsJLabel1.setOpaque(true);
				instructionsJLabel1.setForeground(Color.WHITE);
				instructionsJLabel1.setBackground(Color.BLACK);

				
				aboutBack = new JButton("Back");
				aboutBack.setActionCommand("Back Menu");
				aboutBack.addActionListener(this);
				aboutBack.setBackground(Color.BLACK);
				aboutBack.setForeground(Color.WHITE);
				
				add(aboutBack, 4, 0);
				add(instructionsJLabel1, 2, 0);
				add(aboutJLabel, 0, 0);
				aboutJLabel.setFont(new Font("Arial", Font.ITALIC, 40));
				instructionsJLabel1.setFont(new Font("Arial", Font.PLAIN, 12));
				aboutBack.setFont(new Font("Arial", Font.PLAIN, 20));
				aboutScreen1stTime++;
			}
			
			
		}	
	}
	
	//Description: generates the power up
	//Parameters: none
	//Return: none
	public void extraLifePowerUp() {
		boolean powerupDupe = true;
		while (powerupDupe) {
			powerUpX = (int)(Math.random() * (12)+1);
			powerUpY = (int)(Math.random() * (12)+1);
			powerUpX *= 25;
			powerUpY *= 25;
			//will only generate at score 2 than at multiples of 5
			for (int i = 0; i < score; i++) {
				if (powerUpX == wallX[i] && powerUpY == wallY[i]) {
					System.out.println("Dupe powerup");
					powerupDupe = true;
					break;
				}
				else {
					powerupDupe = false;
				}
			}
		}
		
	}
		

	//Description: generates wall block
	//Parameters: none
	//Return:none
	public void obstacleGeneration() {
		while (true) {
			obstacleX = (int)(Math.random() * (13)+1);
			obstacleY = (int)(Math.random() * (13)+1);
			obstacleX *= 25;
			obstacleY *= 25;
			wallX[score] = obstacleX;
			wallY[score] = obstacleY;  
			walls[score] = new Rectangle(obstacleX, obstacleY, gridSize, gridSize);
			//checks if generates ontop of food
			if (foodX != obstacleX && foodY != obstacleY) {
				break;
			}
			else {
				System.out.println("DUPE");
			}
		}
	}
	
	//Description: generates food blocks
	//Parameters: none
	//Return: none
	public void foodGeneration() {
		boolean foodDupe = true;
		while (foodDupe) {
			foodX = (int)(Math.random() * (12)+1);
			foodY = (int)(Math.random() * (12)+1);
			foodX *= 25;
			foodY *= 25;
			System.out.println("food x" +foodX );
			System.out.println("Food y" +foodY );
			//checks if the food generates on a wall
			if (score >= 1) {
				for (int i = 0; i < score; i++) {
					if (foodX == wallX[i] && foodY == wallY[i]) {
						System.out.println("Dupe food");
						foodDupe = true;
						break;
					}
					else {
						foodDupe = false;
					}
				}
			}
			else {
				foodDupe = false;
			}
		}
	}
	
	//Description: checks if the power up gets eaten by the snake yet
	//Parameters: none
	//Return: none
	public boolean powerUpEatCheck() {
		boolean powerUpEatOrNot = false;
		if (snakeX[0] == powerUpX && snakeY[0] == powerUpY && drawPowerUpOrNot) {
			powerUpEatOrNot = true;
			drawPowerUpOrNot = false;
			food1stTimeDraw = true;
			System.out.println("eatem superpower");
			lives = Math.round((lives + 0.1) * 100.0);
			lives /= 100.0;
			try {
				musicMethod("Power Up Effect.wav");
			} 
			catch (Exception e1) {
			}
		}
		return powerUpEatOrNot;
	}
	
	//Description: Checks if food block is eaten by snake yet
	//Parameters:none
	//Return: none
	public boolean foodEatCheck() {
		boolean eatOrNot = false;
		if (snakeX[0] == foodX && snakeY[0] == foodY) {
			eatOrNot = true;
			try {
				musicMethod("Eat Effect.wav");
			} 
			catch (Exception e1) {
			}
		}
		return eatOrNot;
	}
	

	//Description: takes in the key input and changes the direction, once key is released other method will physically change the direction
	//Parameters: takes in keyEvent e and if it matches the controls it will change direction
	//Return: none
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		//moves snake left
		if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT ||key == KeyEvent.VK_J) {
			left = true;
			right = false;
			down = false;
			up = false;
		//moves snake right
		}else if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT ||key == KeyEvent.VK_L) {
			left = false;
			right = true;
			down = false;
			up = false;
		//moves snake up
		}else if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP ||key == KeyEvent.VK_I) {
			left = false;
			right = false;
			down = false;
			up = true;
		//moves snake down	
		}else if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN ||key == KeyEvent.VK_K) {
			left = false;
			right = false;
			down = true;
			up = false;
		}
		//stops all snake movement (will result in death if you don't give yourself alot of lives mostly used for testing)
		else if(key == KeyEvent.VK_Q ) {
			left = false;
			right = false;
			down = false;
			up = false;
		}
		//gives you extra lives to test the features
		else if(key == KeyEvent.VK_M ) {
			lives += 10;
		}
		//sets you to one life so you can test features like dying due to body collision, wall collision, border collision
		else if(key == KeyEvent.VK_N ) {
			lives = 1;
		}
		//sets win clause to true
		else if(key == KeyEvent.VK_Y ) {
			winClause = true;
		}
	}
	
	
	//Description: Changes the direction of the snake once a key is released
	//Parameters: takes in keyEvent e but does not get utilized
	//Return: none
	@Override
	public void keyReleased(KeyEvent e) {
	}
	//self describing, depending on direction it will add or add the size of grid to move it in that direction
	void move() {
		if(left)
			snakeX[0] -= gridSize;
		else if(right)
			snakeX[0] += gridSize;
		
		if(up)
			snakeY[0] += -gridSize;
		else if(down)
			snakeY[0] += gridSize;
		//used for debugging
		System.out.println("Snake X " +snakeX[0]);
		System.out.println("Snake Y " +snakeY[0]);

		for(int i = snakeLength;i>0;i--) {
			snakeX[i] = snakeX[i-1];
			snakeY[i] = snakeY[i-1];
		}
		
		//using a underlying rect to track collision with wall obstacles
		if(left)
			rect.x -= gridSize;
		else if(right)
			rect.x += gridSize;
		
		if(up)
			rect.y += -gridSize;
		else if(down)
			rect.y += gridSize;

	}
	
	//Description: Keeps the snake inside the game borders (works almost all of the time small bug where the snake can go one block under the bottom border)
	//Parameter: none
	//Return: none
	void keepInBound() {
		//left collision
		if(snakeX[0] < 0) {
			System.out.println("Collision boundary");
			if (lives-1 > 1) {
				lives -= 1;
				for (int i = 0; i < snakeX.length; i++) {
					snakeX[i] += 100;
				}
			}	
			else {
				collision = true;
			}
		}
		
		//top collision
		else if(snakeY[0] < 0) {
			System.out.println("Collision boundary");
			if (lives-1 > 1) {
				lives -= 1;
				for (int i = 0; i < snakeY.length; i++) {
					snakeY[i] += 100;
				}
			}	
			else {
				collision = true;
			}
		}
		
		//right collision
		if(snakeX[0] > screenWidth - gridSize) {
			if (lives-1 > 1) {
				lives -= 1;
				for (int i = 0; i < snakeX.length; i++) {
					snakeX[i] = screenWidth - 100;
				}
			}	
			else {
				collision = true;
			}
		}
		
		//bottom collision
		else if(snakeY[0] > screenHeight - gridSize) {
			if (lives-1 > 1) {
				lives -= 1;
				for (int i = 0; i < snakeX.length; i++) {
					snakeY[i] = screenHeight - 100;
				}
			}	
			else {
				collision = true;
			}
		}
		
		// temp using it to keep track of rectangle under main snake
		//left
		if(rect.x < 0)
			rect.x += 100;
		//top
		else if(rect.y < 0)
			rect.y += 100;
		//right
		if(rect.x > screenWidth - rect.width)
			rect.x = screenWidth - 100;
		//bottom
		else if(rect.y > screenHeight - rect.height)
			rect.y = screenHeight - 100;
		
	}
	
	void checkCollision(Rectangle wall) {
		//check if rect touches wall
		if(rect.intersects(wall)) {
			System.out.println("collision");
			if (lives-1 > 1) {
				lives -= 1;
			}	
			else {
				collision = true;
			}
			
		}
		
		//check if snake head hits body
		if (snakeLength > 1) {
			
			for (int i = 2; i < snakeLength; i++) {
				if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
						System.out.println("body collide");
						if (lives < 2) {
							collision = true;
							break;
						}
						else {
							System.out.println("Safe");
							//I divide 1 by the score since the when the snake head passes through only one block of the body 
							//it counts the collisions as with all of the blocks so dividing by the score helps makes sure it only takes off one life
							double tempMinus = (double)(1.0/score);
							System.out.println("minus " + tempMinus);
							lives -= tempMinus;
							break;
						}
					}
				}
			}
		}
		
	//Description: Handles all of the actions related to menus and buttons
	//Parameters: Takes in the action event which is the command that each button has been assigned
	//Return: none
	@Override
	public void actionPerformed(ActionEvent event) {
		// To handle normal menu item
		String eventName = event.getActionCommand ();
		if (eventName.equals ("New Game")){
		   newGame();
		}
		else if (eventName.equals ("Exit")){
		    System.exit (0);
		}
		else if (eventName.equals ("Start")){
			menuScreen = 1;
			running = true;
		}
		
		else if (eventName.equals("About")) {
			menuScreen = 4;
		}
		
		else if (eventName.equals("Settings")) {
			menuScreen = 2;
		}
		else if (eventName.equals("Back")) {
			menuScreen = 0;
			menuScreen1stTime = 0;
			menuScreenPrev = 2;
		}
		else if (eventName.equals("Back Menu")) {
			menuScreen = 0;
			menuScreen1stTime = 0;
			menuScreenPrev = 4;
		}
		else if (eventName.equals("Menu")) {
			menuScreen = 0;
			menuScreen1stTime = 0;
			menuScreenPrev = 3;
			collision = false;
		}
		else if (eventName.equals("Obstaacles On/Off")) {
			if (obstacleOnOFF) {
				obstacleOnOFF = false;
				JOptionPane.showMessageDialog (this, "Obstacles Turned OFF",
						"Obstacles", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				obstacleOnOFF = true;
				JOptionPane.showMessageDialog (this, "Obstacles Turned ON",
						"Obstacles", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (eventName.equals("Faster/Slower")) {
			if (speed == 250) {
				speed = 150;//fastest speed option
				JOptionPane.showMessageDialog (this, "Speed set Fast",
						"Speed", JOptionPane.INFORMATION_MESSAGE);
			}
			else if (speed == 150) {
				speed = 450; //slowest speed
				JOptionPane.showMessageDialog (this, "Speed set Slow",
						"Speed", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				speed = 250; //medium speed option the default
				JOptionPane.showMessageDialog (this, "Speed set Medium",
						"Speed", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (eventName.equals("Colour Switcher")) {
			if (colourSet == 1) {
				backgroundColor = new Color(255,69,0); // orange
				snakeBodyColor = new Color(255,255,0); //yellow
				foodColor = new Color(128,0,128); // purple
				powerUpColor = new Color(255,0,0); //red
				colourSet = 2;
				JOptionPane.showMessageDialog (this, "Colour Set 2: \n Background: Orange \n Body: Yellow \n Food: Purple \n Power Up : Red",
						"Colour", JOptionPane.INFORMATION_MESSAGE);
			}
			else if (colourSet == 2) {
				backgroundColor = new Color(255,255,0); //yellow
				snakeBodyColor = new Color(255,255,255); //white
				foodColor = new Color(0,128,128); //teal
				powerUpColor = new Color(255,0,0);//red
				colourSet = 3;
				JOptionPane.showMessageDialog (this, "Colour Set 3: \n Background: Yellow \n Body: White \n Food: Teal \n Power Up : Red",
						"Colour", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				backgroundColor = new Color(0,255,0); //cyan
				snakeBodyColor = new Color(0,255,255); // lime
				foodColor = new Color(0,0,255); // blue
				powerUpColor = new Color(255,0,0);  //red
				colourSet = 1;
				JOptionPane.showMessageDialog (this, "Colour Set 1: \n Background: Cyan \n Body: Lime \n Food: Blue \n Power Up : Red",
						"Colour", JOptionPane.INFORMATION_MESSAGE);
			}
		}

    }

	// Description: This method gets called when either New game is called from the top menu bar or from the game over screen
	// Parameters: none
	// return: none
	public void newGame() {
		
		snakeX = new int[200];
		snakeY = new int[200];
		score = 0;
		snakeLength = 2;
		lives = 1;
		rect = new Rectangle(0, 0, gridSize, gridSize);
		running = true;
		collision = false;
		left = false;
		right = false;
		down = false;
		up = false;
		
		//if starting new game from gameover screen
		if (menuScreenPrev == 3) {
			gameOverJLabel.setVisible(false);;
			gameOverScore.setVisible(false);
			gameOverNewGameButton.setVisible(false);
			gameOverMenuButton.setVisible(false);
			
		}
		// if starting from about screen
		if (menuScreen == 4) {
			remove(aboutJLabel);
			remove(aboutBack);
			remove(instructionsJLabel1);
			aboutScreen1stTime = 0;
		}
		//if starting from settings screen
		if(menuScreen == 2) {
			remove(backButton);
			remove(obstacleButton);
			remove(colourSwitcherButton);
			remove(fasterSlowerButton);
			remove(settingJLabel);
			settingScreen1stTime = 0;
		}
		// if from gameover screen
		if (gameplayedtime > 0) {
			remove(gameOverJLabel);
			remove(gameOverScore);
			remove(gameOverNewGameButton);
			remove(gameOverMenuButton);
		}

		menuScreen = 1;
	}
	
	// Description: If called opens music file and plays it
	//Parameters: a string which contains the name of the file that will be played
	// return: none
	public static void musicMethod(String fileName) throws Exception {
		Clip backgroundSound;
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File(fileName));
			backgroundSound = AudioSystem.getClip();
			backgroundSound.open(sound);
			//backgroundSound.start();
			if(fileName == "Background Music.wav") {
				backgroundSound.loop(Clip.LOOP_CONTINUOUSLY);
				
			}

		} 
		catch (Exception e) {
		}
	}
	
	// Main Method
	public static void main(String[] args) {
		JFrame frame = new JFrame ("Snake");
		snake_Game myPanel = new snake_Game();
		frame.add(myPanel);
		frame.addKeyListener(myPanel);
		frame.setVisible(true);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		frame.setJMenuBar(mainMenu);


	}



	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
}





























































