/**
 * GameOverScreen
 * class to create game over screen
 * @version 1.0
 * @author Siyao Chen, Vicki Xu
 * May 23, 2017
 */


import javax.swing.*;
import java.awt.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

class GameOverScreen extends JFrame{

    JFrame gameOverScreen = new JFrame();
    public Player player;

    /**
     * GameOverScreen
     * Constructor
     * @param Player player the player that won and lost 
     */
    public GameOverScreen(Player player){

        setTitle("Ninja Duel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 380);
        setResizable (false);
        setLocationRelativeTo(null); //middle of screen
        setUndecorated(false); 

        this.player = player;

        //create panel 
        CustomJPanel pan = new CustomJPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

        //create panel for buttons
        JPanel buttonPan = new JPanel();
        buttonPan.setLayout(new BoxLayout(buttonPan, BoxLayout.X_AXIS));
        buttonPan.setMaximumSize(new Dimension(500,50));
        buttonPan.setOpaque(false);

        buttonPan.add(Box.createHorizontalStrut(60));

        //create button to restart game
        CustomJButton restartButton = new CustomJButton(160,50);
        restartButton.addActionListener(new RestartButtonListener()); //add action listener for buttom 
        buttonPan.add(restartButton);

        buttonPan.add(Box.createHorizontalStrut(45));

        //create button to exit game
        CustomJButton exitButton = new CustomJButton(160,50);
        exitButton.addActionListener(new ExitButtonListener()); //add action listener for buttom 
        buttonPan.add(exitButton);

        pan.add(Box.createRigidArea(new Dimension(0,286)));
        pan.add(buttonPan); //add button panel to custom panel 
        add(pan); //add custom panel
        setVisible(true);
    }

    //class for restart button listener
    class RestartButtonListener implements ActionListener {

      /*
       * actionPerformed 
       * Method to sense action on restart button 
       * @param ActionEvent event sensed on button 
       */
        public void actionPerformed(ActionEvent event)  {
          
            gameOverScreen.dispose();
            setVisible(false);
          
            //return back to menu screen
            new MenuScreen();
        }

    }


    //class for the exit button listener
  class ExitButtonListener implements ActionListener {
    
    /*
     * actionPerformed 
     * Method to sense action on exit button
     * @param ActionEvent event sensed on button 
     */
    public void actionPerformed(ActionEvent event)  {
      
      // Create variables to catch the result of the confirmation dialog
      int result = 0;
      // Display a confirmation dialog confirmed the player would
      // like to quit the game.
      int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Exit?", result);
      // If the user does want to quit the game
      if (dialog == JOptionPane.YES_OPTION) {
        // Close the game.
        gameOverScreen.dispose();
        
        //stop audio playing 
        try { 
          MenuScreen.stopMusicFile();
        } catch (LineUnavailableException ee) {
        }
        
        setVisible(false);
        System.exit(0);
      }
      
      
    
    }
    
  }

    class CustomJPanel extends JPanel {

        protected void paintComponent(Graphics g) {
            //Call to super method for all other necessary functions
            super.paintComponents(g);

            
            //draw the appropriate winner on the screen
            if (player.playerNum == 1) { 
              try {
                    g.drawImage(ImageIO.read(new File("win_screen2.png")), 0, 0, null);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (player.playerNum == 2) {
                try {
                    g.drawImage(ImageIO.read(new File("win_screen1.png")), 0, 0, null);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    class CustomJButton extends JButton{

      /*
       * CustomJButton 
       * Method to create custom buttons for the panel 
       * @param int width of button 
       * @param int height of button 
       */
        public CustomJButton(int width, int height) {
            super();
            //remove the button's default coloring
            super.setContentAreaFilled(false);

            //sets the size
            setMaximumSize(new Dimension(width,height));
            //make it align center
            setAlignmentX(Component.CENTER_ALIGNMENT);
            //remove border
            setBorderPainted(false);
            //sets the text color to be white
            //remove the focus dotted border
            setFocusPainted(false);
            //sets the font of text on the button
            setFont(new Font("Serif", Font.BOLD, 1));
            //sets the buttons to be invisible
            setOpaque(false);
            setContentAreaFilled(false);

        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0,0,0,0));

            //fills the background with the color determined above
            g.fillRect(0, 0, getWidth(), getHeight());

            //make call to the super method to paint the text above the background
            super.paintComponent(g);
        }

    }

}


