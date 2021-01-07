/**
 * GameFrame
 * This class creates and updates the game frame 
 * @author Siyao Chen, Vicki Xu
 * @version 1.0
 * May 23, 2017
 */


import java.awt.image.BufferedImage;

import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;

//Graphics & GUI imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

//File imports
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

//Keyboard imports
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

//actions imports
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//audio imports
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


class GameFrame extends JFrame {


    final File dir = new File("c:\\temp\\java\\p1"); //searches directory with player 1 sprites 
    final File dir2 = new File("c:\\temp\\java\\p2");//searches directory with player 2 sprites 
    public int spriteNum; //sprite number 
    public String spriteUsed1; //sprite file used by player 1 
    public String spriteUsed2; //sprite file used by player 2

    public List<Integer> explodeCoordinates;

    public long bombLasting; 

    static GameAreaPanel gamePanel;

    Field gameField;
    Player player1;
    Player player2;

    /**
     * GameFrame 
     * constructor
     */
    GameFrame() {

        super("Ninja Duel");
        this.setSize(480, 380);
        this.setUndecorated(false); //remove title bar
        this.setResizable(false);
        setLocationRelativeTo(null); //middle of screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        player1 = new Player(1);
        player2 = new Player(2);

        //initialize sprite file used 
        spriteUsed1 = ("p1_down1.png");
        spriteUsed2 = ("p2_down1.png");
        spriteNum = 0; 

        gameField = new Field(player1, player2);

        bombLasting = 0;
        explodeCoordinates = new ArrayList<Integer>();

        //Set up the game panel (where we put our graphics)
        gamePanel = new GameAreaPanel();
        this.add(new GameAreaPanel());

        //add keylisteners 
        MyKeyListener keyListener = new MyKeyListener();
        this.addKeyListener(keyListener);

        this.requestFocusInWindow(); //make sure the frame has focus
        this.setVisible(true);

        //Start the game loop in a separate thread
        Thread t = new Thread(new Runnable() {
            public void run() {
                animate();
            }
        }); //start the gameLoop
        t.start();
    }
    
    /**
     * animate
     * the main gameloop - this is where the game state is updated
     */
    public void animate() {

        while (true) {
          
          //update variables
            player1.move(gameField);
            player2.move(gameField);
            
            try {
                Thread.sleep(50);
            } catch (Exception exc) {} //delay
            this.repaint(); //updates screen

        }

    }


    // Inner class for the the game area - This is where all the drawing of the screen occurs
    private class GameAreaPanel extends JPanel {

      /**
       * paintComponent 
       * method to draw onto screen 
       * @param Graphics g 
       */
        public void paintComponent(Graphics g) {

            // Call to super method for all other necessary functions
            super.paintComponents(g);
            setDoubleBuffered(true);

            //draw background image ( game field )
            try {
                g.drawImage(ImageIO.read(new File("gameBackground.png")), 0, 0, null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // draw tiles onto game field 
            int x = gameField.X_DISPLACEMENT;
            int y = gameField.Y_DISPLACEMENT;
            for (int i = 0; i < gameField.tileArray.length; i++) {
                for (int j = 0; j < gameField.tileArray[i].length; j++) {
                    if (j == 0 && i != 0) {
                        x = gameField.X_DISPLACEMENT;
                        y += gameField.TILE_SIZE;
                    }
                    g.drawImage((gameField.tileArray[i][j].getSprite()), x, y, null); //drawing tiles
                    x += gameField.TILE_SIZE;
                }
            }

            // Draw number of lives onto screen
            g.setFont(new Font("Serif", Font.BOLD, 18));
            g.setColor(Color.white);
            g.drawString((player1.numLives) + "", 208, 25);
            g.drawString((player2.numLives) + "", 280, 25);

            // Draw players
            try {
                g.drawImage(ImageIO.read(new File("p1/" + spriteUsed1)), player1.xCoordinate, player1.yCoordinate, null);
                g.drawImage(ImageIO.read(new File("p2/" + spriteUsed2)), player2.xCoordinate, player2.yCoordinate, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Declare a bomb variable
            Bomb bomb;

            // Check for bomb explosion
            for (int i = 0; i < gameField.bombs.size(); i++) {
                bomb = gameField.bombs.get(i);

                if (bomb.checkExplode()) {
                    // Explode and get list of coordinates to animate
                    explodeCoordinates = bomb.explode(gameField);

                    bombLasting = System.currentTimeMillis();
                }
            }

            // After explosion, no one is immune
            player1.immune = false;
            player2.immune = false;

            // Draw explosion sprites
            if (System.currentTimeMillis() <= bombLasting + 1000) {
                for (int j = 0; j < explodeCoordinates.size(); j++) {
                    // Calculate coordinates to draw on screen
                    int xCoordinate = Field.xPosToCoordinate(explodeCoordinates.get(j) % Field.FIELD_SIZE);
                    int yCoordinate = Field.yPosToCoordinate(explodeCoordinates.get(j) / Field.FIELD_SIZE);

                    // Calculate which image to use
                    int imgIndex = Bomb.getExplosionSpriteIndex(explodeCoordinates, j);

                    // Draw the image
                    g.drawImage(Bomb.explosionSprites[imgIndex], xCoordinate, yCoordinate, null);
                }
                
                try {
                  MenuScreen.playMusicFile("bomb.wav", false);
                } catch (IOException ee) {
                } catch (LineUnavailableException ee) {
                } catch (UnsupportedAudioFileException ee) {
                }
            }

            // Chance to create power-ups: 1/50 chance for each
            if ((int) (Math.random() * 100) == 0) {
                // Generate coordinates
                int xPos = (int) (Math.random() * 11);
                int yPos = (int) (Math.random() * 11);

                // Check if position is a BasicTile
                if (gameField.tileArray[xPos][yPos] instanceof BasicTile) {
                    // Add power-ups to the list
                    gameField.powerUps.add(new HealthPowerUp(gameField.tileArray[xPos][yPos]));
                }
            } else if ((int) (Math.random() * 100) % 20 == 1) {
                // Generate coordinates
                int xPos = (int) (Math.random() * 11);
                int yPos = (int) (Math.random() * 11);

                // Check if position is a BasicTile
                if (gameField.tileArray[xPos][yPos] instanceof BasicTile && gameField.tileArray[xPos][yPos].powerUp == null) {
                    // Add power-ups to the list
                    gameField.powerUps.add(new ExplodePowerUp(gameField.tileArray[xPos][yPos]));
                }
            }

            // Check whether to remove any power-ups
            for (int i = 0; i < gameField.powerUps.size(); i++) {
                // If over 10 seconds have passed, remove power-ups
                if (System.currentTimeMillis() - gameField.powerUps.get(i).placedTime > 10000) {
                    gameField.tileArray[gameField.powerUps.get(i).xPos][gameField.powerUps.get(i).yPos].powerUp = null;
                    gameField.powerUps.remove(i);
                }
            }

            // Draw current power-ups
            for (PowerUp p : gameField.powerUps) {
                g.drawImage(p.getSprite(), p.xPos * Field.TILE_SIZE + Field.X_DISPLACEMENT, p.yPos * Field.TILE_SIZE + Field.Y_DISPLACEMENT, null);
            }

            // Draw current bombs
            for (Bomb b : gameField.bombs) {
                g.drawImage(b.bombSprite, b.xPos * Field.TILE_SIZE + Field.X_DISPLACEMENT, b.yPos * Field.TILE_SIZE + Field.Y_DISPLACEMENT, null);
            }

            // End the game if any player runs out of lives
            if (player1.numLives == 0 || player2.numLives == 0) {

                // Delay game before opening game over screen
                try {
                    Thread.sleep(300);
                } catch (Exception e) {}

                // Open corresponding screen
                if (player1.numLives == 0) {
                    new GameOverScreen(player1);
                } else {
                    new GameOverScreen(player2);
                }

                // Close the frame
                dispose();
            }
        }
    }

    private class MyKeyListener implements KeyListener {

      /** 
       * keyTyped 
       * keylistener for when key is typed 
       * @param KeyEvent e 
       */
        public void keyTyped(KeyEvent e) {

        }

        /** 
       * keyPressed
       * keylistener for when key is pressed 
       * @param KeyEvent e 
       */
        public void keyPressed(KeyEvent e) {

          //exit screen when escape key is pressed
            if (e.getKeyCode() == e.VK_ESCAPE) {
                System.exit(0);
            }

            //to move through player sprites
            spriteNum++;
            if (spriteNum == 3) {
                spriteNum = 1;
            }

            //TESTING PLAYER1
            if (e.getKeyChar() == 'a') { 
                spriteUsed1 = ("p1_left" + spriteNum + ".png");
                player1.xSpeed = -3;
            } else if (e.getKeyChar() == 's') {
                spriteUsed1 = ("p1_down" + spriteNum + ".png");
                player1.ySpeed = 3;
            } else if (e.getKeyChar() == 'd') {
                spriteUsed1 = ("p1_right" + spriteNum + ".png");
                player1.xSpeed = 3;
            } else if (e.getKeyChar() == 'w') {
                spriteUsed1 = ("p1_up" + spriteNum + ".png");
                player1.ySpeed = -3;
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                player1.placeBomb(gameField);
            }

            //TESTING PLAYER 2
            if (e.getKeyCode() == KeyEvent.VK_LEFT) { 
                spriteUsed2 = ("p2_left" + spriteNum + ".png");
                player2.xSpeed = -3;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                spriteUsed2 = ("p2_down" + spriteNum + ".png");
                player2.ySpeed = 3;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                spriteUsed2 = ("p2_right" + spriteNum + ".png");
                player2.xSpeed = 3;
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                spriteUsed2 = ("p2_up" + spriteNum + ".png");
                player2.ySpeed = -3;
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                player2.placeBomb(gameField);
            } 

        }

        /** 
       * keyReleased 
       * keylistener for when key is released 
       * @param KeyEvent e 
       */
        public void keyReleased(KeyEvent e) {

          //stop player from moving after the key is released 
          
            //TESTING PLAYER 1 
        
            if (e.getKeyChar() == 'a') {   
                player1.xSpeed = 0;
            } else if (e.getKeyChar() == 's') {
                player1.ySpeed = 0;
            } else if (e.getKeyChar() == 'd') {
                player1.xSpeed = 0;
            } else if (e.getKeyChar() == 'w') {
                player1.ySpeed = 0;
            }

            //TESTING PLAYER 2
            if (e.getKeyCode() == KeyEvent.VK_LEFT) { 
                player2.xSpeed = 0;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                player2.ySpeed = 0;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                player2.xSpeed = 0;
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                player2.ySpeed = 0;
            }

        }

    }

}