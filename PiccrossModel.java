/**
 * @author Sohrab Najafzadeh
 * Student Number 040770197
 * Course: CST8221 - Java Applications
 * CET-CS-Level 4
 * @Version 3.0
 */
package piccross;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * This class will have all the logic related to our game which will intact with
 * view
 *
 * @author Sohrab.N
 */
public class PiccrossModel {
    /**
     * Represents number of buttons received from a game send by server.
     */
    int count;

    /**
     * Represents if mark check box is set or not.
     */
    private boolean mark = false;

    /**
     * Represents if we want to show solution or not to user.
     */
    private boolean showSolution;

    /**
     * Represents the true grid which will be true or false depending on random
     * generation of the game.
     */
    private static boolean[][] gridBool;

    /**
     * Represents the boxes that are selected by the user.
     */
    private static boolean[][] gridBoolSelected;

    /**
     * Represents the points of the user.
     */
    private int points = 0;

    /**
     * Represents number of boxes that are correctly selected.
     */
    private int trueSelected = 0;

    /**
     * Represents the total boxes that user selected.
     */
    private int gridSelected = 0;

    /**
     * Represents total of true boxes that we have in this random game.
     */
    private int trueButtons = 0;

    /**
     * Represents the text field which will hold the points for the user.
     */
    private final JTextField pointTextField = new JTextField();

    /**
     * Represents the space saved for hints
     */
    private int[] intArr;

    /**
     * gridButtons will hold buttons which will represent our grid. each button
     * represents one of the squares in our grid. It has +1 because we need one on
     * each row and column to show the hints.
     */
    private static JButton[][] gridButtons;

    /**
     * gridPanel will hold the 5x5 gridPanel for our Picross game. this can be
     * changed to other dimensions such as 10x10.
     */
    JPanel gridPanel;

    /**
     * numPanel will hold the hints to our Picross game. It is part of the
     * gridLayout in our gridPanel.
     */
    numPanel[][] numberPanel;

    /**
     * no arg contractor for PicrossModel objects.
     */
    public PiccrossModel() {
        gridBool = new boolean[PiccrossView.getGridSize() + 1][PiccrossView.getGridSize() + 1];
        gridBoolSelected = new boolean[PiccrossView.getGridSize() + 1][PiccrossView.getGridSize() + 1];
        gridPanel = new JPanel();
    }

    /**
     * This method will set up the gridPanel which will hold the hints and grid
     * boxes.
     *
     * @param debugMode used to indicate either to generate random games or
     *                  predefined games.
     */
    public void setup(int debugMode) {
        count = 0;
        intArr = new int[PiccrossView.getGridSize()];
        gridButtons = new JButton[PiccrossView.getGridSize() + 1][PiccrossView.getGridSize() + 1];
        boolean debug = PiccrossView.getDebug();
        numberPanel = new numPanel[PiccrossView.getGridSize() + 1][PiccrossView.getGridSize() + 1];
        for (int i = 0; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 0; j < PiccrossView.getGridSize() + 1; j++) {
                numberPanel[i][j] = new numPanel(PiccrossView.getGridSize());
            }
        }
        // setting up the grid with PicrossView.getGridSize()+1 in GridLayout. We used +1 because we need
        // to count for numberPanel which will hold the hints to our Picross game.
        gridPanel.setLayout(new GridLayout(PiccrossView.getGridSize() + 1, PiccrossView.getGridSize() + 1));
        // sets background color to white
        gridPanel.setBackground(Color.WHITE);
        // creates border between buttons
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        // for loop to set up and add it to gridPanel.
        for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {

                gridBool[i][j] = setup(i, j, debugMode);
                if (gridBool[i][j]) trueButtons++;
            }
        }
        setupHints();
        addHints();
    }

    /**
     * This method will setup each button with receiving the i and j which will
     * indicate which button we are setting up on our 2D-Array of buttons. It also
     * add the MouseEvent listener to each buttons so we can detect we a button is
     * pushed.
     *
     * @param i represents the index for our 2D-Array of buttons.
     * @param j represents the index for our 2D-Array of buttons.
     * @param debugMode represents in which mode setup method will be called.
     * @return Returns true if button is true and false if button is false.
     */
    private boolean setup(int i, int j, int debugMode) {
        boolean bool = false;
        if (j != 0 || i != 0) {
            // creating new JButton object for index passed to this function
            gridButtons[i][j] = new JButton();
            // sets border for this button
            gridButtons[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
            // sets background to white
            gridButtons[i][j].setBackground(Color.WHITE);
            // sets size of the button
            gridButtons[i][j].setSize(50, 50);

            // adding MouseEvent listener to each button
            switch (debugMode) {
                case 0:
                    bool = new Random().nextBoolean();
                    break;
                case 1:// debug sub menu predefined game 1
                    if (i == 1 || j == 1 || i == 5 || j == 5) {
                        bool = true;
                    }
                    break;
                case 2:// debug sub menu predefined game 2
                    if (PiccrossView.getGridSize() % 2 == 0) {
                        if (i == (PiccrossView.getGridSize() / 2) && j == (PiccrossView.getGridSize() / 2)) {
                            bool = true;
                        }
                    } else {
                        if (i == ((PiccrossView.getGridSize() / 2) + 1) && j == ((PiccrossView.getGridSize() / 2) + 1)) {
                            bool = true;
                        }
                    }
                    break;
                case 3:// debug sub menu predefined game 3
                    break;
                case 4:// parses the game received from server and sets the buttons according to that
                    String game = PiccrossView.getGameBoolVec();
                    String[] messageArr = game.split(",");
                    if (count < messageArr.length) {
                        if (messageArr[count].equals("1")) {
                            bool = true;
                            count++;
                        } else if (messageArr[count].equals("0")) {
                            count++;
                        }
                    }
                    break;
            }
            gridButtons[i][j].addMouseListener(new MouseAdapter() {
                /**
                 * ActionListener when a program detects a mouse click.
                 * @param e MouseEvent object.
                 */
                public void mousePressed(MouseEvent e) {
                    if (!PiccrossView.isGameIsDone()) {
                        if (gridBool[i][j] && !gridBoolSelected[i][j]) {
                            if (!mark) {
                                trueSelected++;
                                gridButtons[i][j].setBackground(new Color(148, 216, 243));
                                points++;
                                pointTextField.setEditable(false);
                                pointTextField.setBackground(Color.WHITE);
                                pointTextField.setText("   " + points + "/" + PiccrossView.getGridSize() * PiccrossView.getGridSize() + "  ");
                                if (trueSelected == trueButtons) {
                                    Object[] buttons = {"Yes", "No"};
                                    PiccrossView.setGameIsDone(true);
                                    gameDone();
                                    PiccrossView.getTimer().setStatus(ControllableTimer.STOP);
                                    if (points == trueButtons) {
                                        points = PiccrossView.getGridSize() * PiccrossView.getGridSize();
                                        pointTextField.setText("   " + points + "/" + PiccrossView.getGridSize() * PiccrossView.getGridSize() + "  ");
                                        ImageIcon imageWinner = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("gamepicwinner.png")));
                                        JLabel picWinner = new JLabel(imageWinner);
                                        int dialogResult = JOptionPane.showOptionDialog(null, picWinner, "Congrats!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,//do not use a custom Icon
                                                buttons, buttons[1]);
                                        if (dialogResult == JOptionPane.YES_OPTION) {
                                            sendGameToServer();
                                        }
                                    } else {
                                        ImageIcon imageEnd = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("gamepicend.png")));
                                        JLabel picEnd = new JLabel(imageEnd);

                                        int dialogResult = JOptionPane.showOptionDialog(null, picEnd, "Game Over!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,//do not use a custom Icon
                                                buttons, buttons[1]);
                                        if (dialogResult == JOptionPane.YES_OPTION) {
                                            sendGameToServer();
                                        }
                                    }
                                }
                            } else {
                                gridButtons[i][j].setBackground(new Color(148, 216, 243));
                                Image img = null;
                                try {
                                    img = ImageIO.read(Objects.requireNonNull(getClass().getResource("\\A3_Graphics\\xmark.png")));
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                gridButtons[i][j].setIcon(new ImageIcon(Objects.requireNonNull(img)));
                                if (points > 0) points--;
                                pointTextField.setText("   " + points + "/" + PiccrossView.getGridSize() * PiccrossView.getGridSize() + "  ");
                            }
                        } else if (!gridBool[i][j] && !gridBoolSelected[i][j]) {
                            if (!mark) {
                                gridButtons[i][j].setBackground(new Color(220, 220, 220));
                                Image img = null;
                                try {
                                    img = ImageIO.read(Objects.requireNonNull(getClass().getResource("\\A3_Graphics\\xmark.png")));
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                gridButtons[i][j].setIcon(new ImageIcon(Objects.requireNonNull(img)));
                                if (points > 0) points--;
                                pointTextField.setText("   " + points + "/" + PiccrossView.getGridSize() * PiccrossView.getGridSize() + "  ");
                            } else {
                                gridButtons[i][j].setBackground(new Color(220, 220, 220));
                                points++;
                                pointTextField.setEditable(false);
                                pointTextField.setBackground(Color.WHITE);
                                pointTextField.setText("   " + points + "/" + PiccrossView.getGridSize() * PiccrossView.getGridSize() + "  ");
                            }
                        }
                        if (gridBoolSelected[i][j]) gridSelected++;
                        if (gridSelected == PiccrossView.getGridSize() * PiccrossView.getGridSize()) {
                            PiccrossView.setGameIsDone(true);
                            gameDone();
                            PiccrossView.getTimer().setStatus(ControllableTimer.STOP);
                            Object[] buttons = {"Yes", "No"};
                            ImageIcon imageWinner = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("gamepicwinner.png")));
                            JLabel picWinner = new JLabel(imageWinner);
                            JOptionPane.showOptionDialog(null, picWinner, "Congrats!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,//do not use a custom Icon
                                    buttons,//the titles of buttons
                                    buttons[1]);//default button title

                        }
                        gridBoolSelected[i][j] = true;
                    }
                }

                /**
                 * ActionListener when mouse enters a button. Used for hover feature.
                 * @param e MouseEvent object.
                 */
                public void mouseEntered(MouseEvent e) {
                    if (PiccrossView.getHover() && !PiccrossView.isGameIsDone()) {
                        if (!gridBoolSelected[i][j]) {
                            gridButtons[i][j].setBackground(new Color(232, 203, 93));
                        }
                    }
                }

                /**
                 * ActionListener when mouse exits a button. Used for hover feature.
                 * @param e MouseEvent object.
                 */
                public void mouseExited(MouseEvent e) {
                    if (PiccrossView.getHover() && !PiccrossView.isGameIsDone()) {
                        if (!showSolution) {
                            if (gridBoolSelected[i][j]) {
                                if (gridBool[i][j]) {
                                    gridButtons[i][j].setBackground(new Color(148, 216, 243));
                                } else {
                                    gridButtons[i][j].setBackground(new Color(220, 220, 220));
                                    BufferedImage img = null;
                                    try {
                                        img = ImageIO.read(Objects.requireNonNull(getClass().getResource("\\A3_Graphics\\xmark.png")));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    gridButtons[i][j].setIcon(new ImageIcon(img));
                                }
                            } else {
                                gridButtons[i][j].setBackground(Color.WHITE);
                            }
                        } else {
                            if (gridBool[i][j] && gridBoolSelected[i][j]) {
                                gridButtons[i][j].setBackground(new Color(148, 216, 243));
                            } else if (gridBool[i][j]) {
                                gridButtons[i][j].setBackground(Color.GREEN);
                            } else if (gridBoolSelected[i][j] && !gridBool[i][j]) {
                                gridButtons[i][j].setBackground(new Color(220, 220, 220));
                            } else {
                                gridButtons[i][j].setBackground(Color.WHITE);
                            }
                        }
                    }
                }

                public void mouseReleased(MouseEvent e) {
                }
            });
        }
        return bool;
    }

    /**
     * Method will send the current game in Piccross to server.
     */
    public void sendGameToServer() {
        Socket socket = PiccrossView.getSocket();
        if (socket == null) {
            PiccrossView.getConsoleOutputTextArea().append("ALERT: You must be connected to Picross Server in order to upload score.");
        } else {
            PrintStream out = null;
            try {
                out = new PrintStream(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder gameStream = new StringBuilder("[");
            for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
                for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {
                    if (gridBool[i][j]) {
                        gameStream.append("1,");
                    } else {
                        gameStream.append("0,");
                    }
                }
            }
            gameStream.append("]");
            gameStream.append("Points:").append(points).append(",Time:").append(PiccrossView.getTimeElapsed(PiccrossView.getTimer().getTime()));
            Objects.requireNonNull(out).println(gameStream);
        }
    }

    /**
     * Method will set up and add hint to our gridPanel layout.
     */
    public void addHints() {
        for (int i = 0; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 0; j < PiccrossView.getGridSize() + 1; j++) {
                if ((j == 0 || i == 0)) {
                    numberPanel[i][j].setBorder(BorderFactory.createEtchedBorder(new Color(234, 197, 66), Color.black));
                    if (j == 0) {
                        for (int j2 = 0; j2 < intArr.length; j2++) {
                            if (numberPanel[i][0].getValue(j2) != 0) {
                                JLabel label = new JLabel(" " + numberPanel[i][0].getValue(j2));
                                Font font = new Font("Courier", Font.BOLD, 24);
                                label.setFont(font);
                                numberPanel[i][0].add(label);
                            }
                        }
                    }
                    if (i == 0) {
                        for (int j2 = 0; j2 < intArr.length; j2++) {
                            if (numberPanel[0][j].getValue(j2) != 0) {
                                JLabel label = new JLabel(" " + numberPanel[0][j].getValue(j2));
                                Font font = new Font("Courier", Font.BOLD, 24);
                                label.setFont(font);
                                numberPanel[0][j].add(label);
                            }
                        }
                    }
                    numberPanel[i][j].setVisible(true);
                    this.gridPanel.add(numberPanel[i][j]);
                } else {
                    gridPanel.add(gridButtons[i][j]);
                }
            }
        }
    }

    /**
     * This method will set up hints for our game depending on what is inside random
     * generated boxes.
     */
    public void setupHints() {
        boolean debug = PiccrossView.getDebug();
        int k = 0;

        int value = 0;
        for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {
                if (gridBool[i][j]) {
                    if (debug) System.out.println(i + "," + j + " is true");
                    intArr[k] = ++value;
                    if (gridBool[0].length > (j + 1) && gridBool[i][j + 1]) {
                        if (debug) System.out.println(i + "," + (j + 1) + " is true");
                        intArr[k] = ++value;
                        j++;
                    } else {
                        if (gridBool[0].length > (j + 1) && debug) System.out.println(i + "," + (j + 1) + " is false");
                        k++;
                        value = 0;
                    }
                } else {
                    if (debug) System.out.println(i + "," + j + " is false");
                    k++;
                    value = 0;
                }
                if (debug) {
                    System.out.println("k is: " + k);
                    System.out.println("value is: " + value);
                    System.out.println("Array is: " + Arrays.toString(intArr));
                }
            }

            numberPanel[i][0].setValue(intArr);
            Arrays.fill(intArr, 0);
            value = 0;
            k = 0;
        }
        for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {
                if (gridBool[j][i]) {
                    if (debug) System.out.println(j + "," + i + " is true");
                    intArr[k] = ++value;
                    if (gridBool[0].length > (j + 1) && gridBool[j + 1][i]) {
                        if (debug) System.out.println((j + 1) + "," + i + " is true");
                        intArr[k] = ++value;
                        j++;
                    } else {
                        if (gridBool[0].length > (j + 1) && debug) System.out.println((j + 1) + "," + i + " is false");
                        k++;
                        value = 0;
                    }
                } else {
                    if (debug) System.out.println(j + "," + i + " is false");
                    k++;
                    value = 0;
                }
                if (debug) {
                    System.out.println("k is: " + k);
                    System.out.println("value is: " + value);
                    System.out.println("Array is: " + Arrays.toString(intArr));
                }
            }
            numberPanel[0][i].setValue(intArr);
            Arrays.fill(intArr, 0);
            value = 0;
            k = 0;
        }
    }

    /**
     * This method will show the correct boxes with changing the background of box
     * to green. If we run the same method again it will go back to original color
     * which means solution will go away.
     */
    public void showSolution() {
        showSolution = !showSolution;
        for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {
                // setups each button and adds MouseEvent listener to it.
                if (showSolution) {
                    if (gridBool[i][j]) {
                        gridButtons[i][j].setBackground(Color.GREEN);
                    }
                } else {
                    if (gridBool[i][j]) {
                        if (gridBoolSelected[i][j]) gridButtons[i][j].setBackground(new Color(148, 216, 243));
                        else gridButtons[i][j].setBackground(Color.WHITE);
                    }
                }
            }
        }
    }

    /**
     * This method is a getter for gridPanel which will be used in view.
     *
     * @return Returns gridPanel to other classes.
     */
    public JPanel getGridPanel() {
        return gridPanel;
    }

    /**
     * This method is a getter for showSolution.
     *
     * @return Returns boolean value of showSolution.
     */
    public boolean isShowSolution() {
        return showSolution;
    }

    /**
     * This class is used to create numPanel object which is a JPanel with a int
     * value
     */
    public static class numPanel extends JPanel {

        /**
         * Default serial version ID - Swing components implement the Serializable
         * interface
         */
        private static final long serialVersionUID = 1;
        /**
         * Used to save the hints which will be an integer value
         */
        protected int[] value;

        /**
         * numPanel class object constructor.
         * @param gridSize represents the grid size which will be an int value example: if gridSize=5 game is 5*5.
         */
        public numPanel(int gridSize) {
            super();
            value = new int[gridSize];
        }

        /**
         * This method is a setter which will set the value array.
         *
         * @param value Represents the int array that holds the hints.
         */
        public void setValue(int[] value) {
            this.value = Arrays.copyOf(value, value.length);
        }

        /**
         * This method is a getter which will return a single index of value array.
         *
         * @param index Represents the index of int value array.
         * @return Returns a value on single index of value array.
         */
        public int getValue(int index) {
            return this.value[index];
        }

    }

    /**
     * This method is a getter for pointTextField.
     *
     * @return pointTextField which will represent the space in UI which will hold
     * the points for the user.
     */
    public JTextField getPointTextField() {
        return pointTextField;
    }

    /**
     * This method is setter which will set the mark to true or false.
     *
     * @param mark represents if we are trying to find true boxes or false boxes in
     *             the game.
     */
    public void setMark(boolean mark) {
        this.mark = mark;
    }

    /**
     * This method will be used when ever we are reseting the game with same grid
     * boxes.
     */
    public void resetGame() {

        for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {
                gridBoolSelected[i][j] = false;
                gridButtons[i][j].setBackground(Color.WHITE);
                gridButtons[i][j].setIcon(null);

            }
        }
        points = 0;
        gridSelected = 0;
        trueSelected = 0;
        pointTextField.setText("   " + points + "/" + PiccrossView.getGridSize() * PiccrossView.getGridSize() + "  ");
    }

    /**
     * This method is a setter for showSolution.
     *
     * @param showSolution represents boolean value that if set to true will show
     *                     the correct boxes if false will not.
     */
    public void setShowSolution(boolean showSolution) {
        this.showSolution = showSolution;
    }

    /**
     * This method will disable the boxes after the game is done.
     */
    public void gameDone() {
        for (int i = 1; i < PiccrossView.getGridSize() + 1; i++) {
            for (int j = 1; j < PiccrossView.getGridSize() + 1; j++) {
                gridButtons[i][j].setEnabled(false);
            }
        }
    }
}
