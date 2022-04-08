/**
 * @author Sohrab Najafzadeh
 * Student Number 040770197
 * Course: CST8221 - Java Applications
 * CET-CS-Level 4
 * @Version 1.0
 */
package piccross;

import jdk.nashorn.internal.scripts.JO;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Picross class represents our frame in Picross game.
 *
 * @author Sohrab.N
 */
public class PicrossView extends JFrame {
    Vector<String> ListUser;
    BufferedReader input;
    PrintStream out;
    static Socket s;
    static PiccrossNetworkModalVC connect;
    boolean connected = false;
    Thread thread;
    /**
     * Represents if we are debugging the code or not.
     */
    private static final boolean debug = false;

    /**
     * Represents if we are showing hover in our UI or not. set to false as hover
     * has some bugs.
     */
    private static boolean hover = true;

    /**
     * Represents the game mode we want to play. if set to 0 means we are generating
     * random game.
     */
    private static int debugMode = 0;

    /**
     * default serial version ID - Swing components implement the Serializable
     * interface.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Represents the grid size for our game for example 5 means 5x5 grid.
     */
    private static int gridSize = 5; // This can be change to change the grid from 5x5 to any other size if used is
    // interested.

    /**
     * represents our JFrame which will be the only frame we will use in this game.
     */
    private static PicrossView mainFrame;

    /**
     * panels we are using in our frame
     */
    private final PicrossModel picrossModel;

    /**
     * westPanel represent the panel on left side of frame.
     */
    private JPanel westPanel = new JPanel();

    /**
     * markAndLogoPanel will hold logo and mark panel. markAndLogoPanel is placed at
     * PAGE_START of westPanel.
     */
    private JPanel markAndLogoPanel = new JPanel();

    /**
     * consolePanel will hold point panel, timer panel and scroll pane inside it
     * self. consolePanel is placed at CENTER of westPanel.
     */
    private JPanel consolePanel = new JPanel();

    /**
     * pointPanel will hold pointTextField.pointPanel is placed at NORTH of
     * consolePanel.
     */
    private JPanel pointPanel = new JPanel();

    /**
     * timerPanel which will show the time since game has been started.
     */
    private JPanel timerPanel = new JPanel();

    private JPanel consoleInputPanel = new JPanel();
    private JTextField consoleInput;

    // other components of our panels.
    /**
     * logoLabel will hold the logo to our Picross game.logoLabel is placed at
     * PAGE_START of markAndLogoPanel.
     */
    private JLabel logoLabel = new JLabel();

    /**
     * pointLabel will display "Points : " in our Picross game.pointLabel is a label
     * inside pointPanel.
     */
    private JLabel pointLabel = new JLabel();

    /**
     * resetButton will restart our game (CURRENTLY NOT WORKING).
     */
    private JButton resetButton = new JButton();

    /**
     * consoleOutputTextArea will display outputs to the player. it will be used inside
     * scrollerScrollPane to add scrolling ability to it.
     */
    private JTextArea consoleOutputTextArea;

    /**
     * scrollerScrollPane will hold consoleOutputTextArea and will add scroll ability to
     * it.scrollerScrollPane is placed at SOUTH of consolePanel.
     */
    private JScrollPane scrollerScrollPane;

    /**
     * MarkCheckBox will display a message if its change and display another message
     * if set to uncheck.MarkCheckBox is placed at CENTER of markAndLogoPanel.
     */
    private JCheckBox MarkCheckBox = new JCheckBox();

    /**
     * timerLabel will display the current time of playing game.timerLabel is a
     * component of timerPanel.
     */
    private JLabel timerLabel;

    /**
     * used to track the start time of time.
     */
    private long startTimer = System.currentTimeMillis();

    /**
     * used to indicate if the game is finished.
     */
    private static boolean gameIsDone;

    /**
     * Used to add menu bar to UI.
     */
    private JMenuBar menuBar;

    /**
     * Used to add menu to UI.
     */
    private JMenu menu, subMenu;

    /**
     * Used to add menu items to UI.
     */
    private JMenuItem menuItem;
    private JMenuItem menuItemConnect;
    private JMenuItem menuItemDisconnect;

    /**
     * Picross class object constructor.
     */
    public PicrossView() {
        // setting name of frame
        super("Picross 3.0");
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        picrossModel = new PicrossModel();
        // Exiting the program if mainFrame is close
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        timerLabel = new JLabel("");
        connect = new PiccrossNetworkModalVC(this);
        consoleOutputTextArea = new JTextArea(24,30);
        scrollerScrollPane = new JScrollPane(consoleOutputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    }


    /**
     * This method will created a new frame, setups the the frame and the grid and
     * then add the other panels and components.
     *
     * @return it will return a integer not -1 shows method has been ran
     * successfully if -1 shows there was error.
     */
    public int startGUI(int gameSize) {
        String iconPath = "piccrossLogo.png";
        String imagePath = "piccrossNameMin.jpg";
        gridSize=gameSize;
        // creating new object of Picross class and storing it in mainFrame
        mainFrame = this;

        URL iconURL = getClass().getResource(iconPath);
        if (iconURL == null) {
            System.err.println("ERROR: ICON PATH NOT FOUND. PLEASE CHECK THE iconPath VARIABLE.");
            return -1;
        }
        ImageIcon img = new ImageIcon(iconURL);
        mainFrame.setIconImage(img.getImage());

        picrossModel.setShowSolution(false);

        gameIsDone = false;
        // sets up the menu bar
        mainFrame.buildMenu();
        // sets up the 5x5 grid
        picrossModel.setup(debugMode);
        JPanel gameGrid = picrossModel.getGridPanel();
        gameGrid.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(255, 204, 102)));
        // adding gridPanel to the mainFrame
        mainFrame.getContentPane().add(gameGrid, BorderLayout.CENTER);
        // default resolution that our frame will lunch
        if (gridSize <= 10) mainFrame.setPreferredSize(new Dimension(gridSize * 220, gridSize * 140));
        else mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        mainFrame.add(westPanel, BorderLayout.LINE_START);

        JTextArea consoleFixedText = new JTextArea(1, 5);
        consoleFixedText.setText("console>");
        consoleFixedText.setEditable(false);
        mainFrame.add(consoleInputPanel, BorderLayout.SOUTH);
//        consoleInputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        consoleInputPanel.setLayout(new BorderLayout());
        consoleInputPanel.add(consoleFixedText, BorderLayout.WEST);
        Dimension size = mainFrame.getSize();
        consoleInput = new JTextField(null, (size.width / 11) - 7);
        consoleInputPanel.add(consoleInput, BorderLayout.EAST);


        // west panel setup
        westPanel.setBackground(new Color(248, 244, 244));
        westPanel.setPreferredSize(new Dimension(250, 1000));
        westPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(255, 204, 102)));
        westPanel.setLayout(new BorderLayout(5, 5));
        westPanel.add(markAndLogoPanel, BorderLayout.PAGE_START);
        westPanel.add(consolePanel, BorderLayout.CENTER);
        westPanel.add(resetButton, BorderLayout.PAGE_END);

        // reset setup
        resetButton.setText("Reset");
        resetButton.setSize(100, 50);

        // console panel setup
        consolePanel.setBackground(Color.LIGHT_GRAY);
        consolePanel.setBorder(new LineBorder(Color.BLACK, 0));
        consolePanel.setLayout(new BorderLayout(5, 5));
        consolePanel.add(pointPanel, BorderLayout.NORTH);
        consolePanel.add(timerPanel, BorderLayout.CENTER);
        consolePanel.add(scrollerScrollPane, BorderLayout.SOUTH);

        // console text area setup
        consoleOutputTextArea.setPreferredSize(new Dimension(200, 400));
        consoleOutputTextArea.setEditable(false);

        consoleInput.addKeyListener(new textAreaListener());
        // timer panel setup
        timerPanel.setPreferredSize(new Dimension(250, 50));

        timerLabel.setFont(new Font("TimesNewRoman", Font.BOLD, 14));
        timerPanel.add(timerLabel);

        // point panel setup
        pointLabel.setText("Points : ");
        pointPanel.add(pointLabel, new BorderLayout(5, 5));
        // picrossModel.getPointTextField().setText(" ");
        picrossModel.getPointTextField().setEditable(false);
        picrossModel.getPointTextField().setBackground(Color.WHITE);
        picrossModel.getPointTextField().setText("   " + 0 + "/" + gridSize * gridSize + "  ");
        pointPanel.add(picrossModel.getPointTextField(), new BorderLayout(5, 5));

        // mark panel setup
        markAndLogoPanel.setPreferredSize(new Dimension(250, 75));
        markAndLogoPanel.add(logoLabel, BorderLayout.PAGE_START);
        markAndLogoPanel.add(MarkCheckBox, BorderLayout.CENTER);

        // Mark check box setup
        MarkCheckBox.setText("Mark");
        MarkCheckBox.addItemListener(new CheckBoxHandler());

        // logo setup
        logoLabel.setOpaque(true);
        logoLabel.setVisible(true);
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        logoLabel.setVerticalAlignment(JLabel.CENTER);
        URL image = this.getClass().getResource(imagePath);
        if (image == null) {
            System.out.println("ERROR: IMAGE NOT FOUND. PLEASE CHECK THE");
            return -1;
        }
        ImageIcon picGame = new ImageIcon(image);
        logoLabel.setIcon(picGame);

        // registering the Reset Button
        resetButton.addActionListener(new reset());

        // starting time // was not able to make it work with your timer code. need help
        // regarding to that during the lab.
        new Thread(new Runnable() {
            public void run() {
                try {
                    updateTime();
                } catch (Exception ie) {
                }
            }
        }).start();

        return 0;
    }

    /**
     * This method will update the time in every 1 second interval.
     */
    public void updateTime() {
        try {
            while (!gameIsDone) {
                // geting Time in desire format
                timerLabel.setText(getTimeElapsed());
                Thread.currentThread();
                // thread sleeping for 1 sec
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Exception in Thread Sleep : " + e);
        }
    }
//    public void showNego(String host,int port){
//        consoleOutputTextArea.append("Negotiating connection to " + host + " on port " + port);
//    }
    /**
     * This method will calculate the time that our game has been running and
     * returns it as string in proper format.
     *
     * @return String is returned to the calling which will be the time since game
     * has been started.
     */
    public String getTimeElapsed() {
        // getting time since the game has been started
        long elapsedTime = System.currentTimeMillis() - startTimer;
        // converting millisecond to second
        elapsedTime = elapsedTime / 1000;

        String seconds = Integer.toString((int) (elapsedTime % 60));
        String minutes = Integer.toString((int) ((elapsedTime % 3600) / 60));
        String hours = Integer.toString((int) (elapsedTime / 3600));

        if (seconds.length() < 2) seconds = "0" + seconds;

        if (minutes.length() < 2) minutes = "0" + minutes;

        if (hours.length() < 2) hours = "0" + hours;
        // returning the timer in correct format
        return hours + ":" + minutes + ":" + seconds;
    }

    /**
     * ItemListener for Mark Check Box as inner class inside Picross class.
     *
     * @author Sohrab.N
     */
    private class CheckBoxHandler implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Mark Selected");
                picrossModel.setMark(true);

            } else {
                System.out.println("Mark NOT Selected");
                picrossModel.setMark(false);
            }
        }

    }

    /**
     * ActionListener for restarting the game and timer. Restarting the game it self
     * is not currently working but timer gets restart.
     *
     * @author Sohrab.N
     */
    private class reset implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Reset Selected");
            startTimer = System.currentTimeMillis();
            if (gameIsDone) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            updateTime();
                        } catch (Exception ie) {
                        }
                    }
                }).start();
            }
            picrossModel.resetGame();
            gameIsDone = false;
        }
    }

    /**
     * ActionListener for show about in menu which will pop up a dialog windows and
     * shows information regarding to software.
     *
     * @author Sohrab.N
     */
    private class showAbout implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(mainFrame, "Picross 3.0\nBy: Sohrab Najafzadeh\n\nWinter term 2022\n");
        }
    }

    /**
     * ActionListener for show solution in menu which will mark the correct game
     * moves with green color.
     *
     * @author Sohrab.N
     */
    private class showSolution implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            picrossModel.showSolution();

        }
    }

    /**
     * ActionListener for new game item on menu which will create a new random game.
     *
     * @author Sohrab.N
     */
    private class newGame implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gameIsDone = true;
            mainFrame.dispose();
            //mainFrame = new PicrossView();
            mainFrame.startGUI(5);

        }
    }

    private class setSize5 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gridSize = 5;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame.startGUI(5);
        }
    }

    private class setSize10 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gridSize = 10;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame=new PicrossView();
            mainFrame.startGUI(10);
        }
    }

    private class setSize15 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gridSize = 15;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame=new PicrossView();
            mainFrame.startGUI(15);
        }
    }

    /**
     * ActionListener for exit item on menu which will exit the game.
     *
     * @author Sohrab.N
     */
    private class exit implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    /**
     * ActionListener for debug sub menu debug configuration 1 which will create a
     * predefined game that all outside boxes are correct.
     *
     * @author Sohrab.N
     */
    private class debugConfig1 implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 1;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PicrossView();
            mainFrame.startGUI(5);
        }
    }

    /**
     * ActionListener for debug sub menu debug configuration 2 which will create a
     * predefined game that only the middle box is correct.
     *
     * @author Sohrab.N
     */
    private class debugConfig2 implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 2;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PicrossView();
            mainFrame.startGUI(5);
        }
    }

    /**
     * ActionListener for debug sub menu debug configuration 3 which will create a
     * predefined game that all boxes are false.
     *
     * @author Sohrab.N
     */
    private class debugConfig3 implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 3;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PicrossView();
            mainFrame.startGUI(5);
        }
    }

    private class openConnection implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            Point thisLocation = getLocation();
            Dimension parentSize = getSize();
            Dimension modalSize = connect.getSize();
            int offsetX = (parentSize.width - modalSize.width) / 2 + thisLocation.x;
            int offsetY = (parentSize.height - modalSize.height) / 2 + thisLocation.y;
            connect.setLocation(offsetX, offsetY);
            connect.setVisible(true);
            int port = connect.getPort();
            String host = connect.getAddress();
            consoleOutputTextArea.setText("Negotiating connection to " + host + " on port " + port);

            try {
                s = new Socket();
                s.connect(new InetSocketAddress(InetAddress.getByName(host), port), 10000);
                //s.setSoTimeout(10000);
                try {
                    consoleOutputTextArea.append("\nConnection Successful");
                    consoleOutputTextArea.append("\nWelcome to Sohrab's Picross Server.");
                    consoleOutputTextArea.append("\nUser '/help' for commands.\n");
                    //InputStream inStream = s.getInputStream();
                    input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    out = new PrintStream(s.getOutputStream(),true);
                    String name = connect.getName();
                    out.println(name);
                    connected = true;
                    menuItemConnect.setEnabled(false);
                    menuItemDisconnect.setEnabled(true);
                    thread = new PicrossNetworkController();
                    thread.start();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class disconnect implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String userName = connect.getName();
                System.out.println(userName + " has disconnected.");
                out.println(userName + " has disconnected.");
                s.close();
                connected = false;
                menuItemConnect.setEnabled(true);
                menuItemDisconnect.setEnabled(false);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class textAreaListener implements KeyListener {


        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                String consoleText = consoleInput.getText();
                if (checkMessage(consoleText) == 0) {
                    sendMessage(consoleText);
                }
                consoleInput.setText(null);
//                String temp = consoleText;
//                consoleInput.setText("");
//                if (connected) {
//                    if(consoleText.equals("/get")){
//                        temp = temp.replace("/get", "");
//
//                    }
//                    else if(consoleText.equals("/help")){
//                        consoleOutputTextArea.append("HELP:\n/help: this message.\n/bye: disconnect.\n/who: shows the names of all connected players.\n/name : Rename yourself.\n/get: gets the current challenge game.");
//                    }
//                    else if (consoleText.startsWith("/name")) {
//                        temp = temp.replace("/name", "");
//                        if (temp.equals("")) {
//                            System.out.println("Name is not provided");
//                        } else {
//                            temp = temp.substring(1);
//                            String oldName = connect.getName();
//                            connect.setName(temp);
//                            try {
//                                out = new PrintStream(s.getOutputStream());
//                            } catch (IOException ex) {
//                                ex.printStackTrace();
//                            }
//                            out.println(oldName + " renamed to " + temp);
//                        }
//                    } else if (consoleText.equals("/bye")) {
//                        try {
//                            out.println(connect.getName() + " has disconnected.");
//                            s.close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                }else {
//                    System.out.println("Must be connected");
//                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    private class hoverToggle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (hover)
                hover = false;
            else
                hover = true;
        }
    }

    /**
     * This method will create the menu and sub menus and add the specific action
     * listener to each item.
     */
    private void buildMenu() {

        ImageIcon picLogo;
        // creating game menu
        menu = new JMenu("Game");
        // creating new menu item
        menuItem = new JMenuItem("New");
        picLogo = new ImageIcon(this.getClass().getResource("NewGameIcon.png"));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new newGame());
        KeyStroke ctrlVKeyStroke = KeyStroke.getKeyStroke("control N");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        menu.addSeparator();
        subMenu = new JMenu("Set Game Size");
        picLogo = new ImageIcon(this.getClass().getResource("SetGameSizeIcon.png"));
        subMenu.setIcon(picLogo);
        menuItem = new JMenuItem("5 x 5");
        menuItem.addActionListener(new setSize5());
        subMenu.add(menuItem);

        menuItem = new JMenuItem("10 x 10");
        menuItem.addActionListener(new setSize10());
        subMenu.add(menuItem);

        menuItem = new JMenuItem("15 x 15");
        menuItem.addActionListener(new setSize15());
        subMenu.add(menuItem);
        menu.add(subMenu);
        menu.addSeparator();
        menuItem = new JMenuItem("Toggle ON/OFF Mouse Hover");
        picLogo = new ImageIcon(this.getClass().getResource("MouseHoverIcon.png"));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new hoverToggle());
        menu.add(menuItem);
        menu.addSeparator();
        // creating debug sub menu
        subMenu = new JMenu("Debug");
        // creating debug sub menu item 1
        menuItem = new JMenuItem("Debug Configuration 1");
        menuItem.addActionListener(new debugConfig1());
        subMenu.add(menuItem);
        // creating debug sub menu item 2
        menuItem = new JMenuItem("Debug Configuration 2");
        menuItem.addActionListener(new debugConfig2());
        subMenu.add(menuItem);
        // creating debug sub menu item 3
        menuItem = new JMenuItem("Debug Configuration 3");
        menuItem.addActionListener(new debugConfig3());
        subMenu.add(menuItem);
        menu.add(subMenu);
        menu.addSeparator();
        // creating exit menu item
        menuItem = new JMenuItem("Exit");
        picLogo = new ImageIcon(this.getClass().getResource("\\A3_Graphics\\piciconext.gif"));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new exit());
        ctrlVKeyStroke = KeyStroke.getKeyStroke("control Q");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        menuBar.add(menu);
        menu = new JMenu("Networking");
        // creating solution menu item
        menuItemConnect = new JMenuItem("New Connection");
        menuItemConnect.addActionListener(new openConnection());
        menu.add(menuItemConnect);
        menuItemDisconnect = new JMenuItem("Disconnect");
        menuItemDisconnect.addActionListener(new disconnect());
        menuItemDisconnect.setEnabled(false);
        menu.add(menuItemDisconnect);
        menuBar.add(menu);

        // creating help menu
        menu = new JMenu("Help");
        // creating solution menu item
        menuItem = new JMenuItem("Solution");
        picLogo = new ImageIcon(this.getClass().getResource("\\A3_Graphics\\piciconsol.gif"));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new showSolution());
        ctrlVKeyStroke = KeyStroke.getKeyStroke("alt S");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        // creating about menu item
        menuItem = new JMenuItem("About");
        picLogo = new ImageIcon(this.getClass().getResource("\\A3_Graphics\\piciconabt.gif"));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new showAbout());
        ctrlVKeyStroke = KeyStroke.getKeyStroke("alt S");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        menuBar.add(menu);

    }

    /**
     * This method is a getter for gridSize.
     *
     * @return gridSize which will represent number of boxes in our grid.
     */
    public static int getGridSize() {
        return gridSize;
    }

    /**
     * This method is a getter for debug variable which if set to true it will show
     * additional info on console .
     *
     * @return debug is used to debug the code.
     */
    public static boolean getDebug() {
        return debug;
    }

    /**
     * This method is a getter for hover variable which if set to true it will add
     * hove option to the game but currently its a bit buggy so default is set to
     * false.
     *
     * @return hover which will represents if we are showing hover of the mouse in
     * our UI or not.
     */
    public static boolean getHover() {
        return hover;
    }

    /**
     * This method is a getter for gameIsDone variable.
     *
     * @return gameIsDone represents if game has been finished or not.
     */
    public static boolean getGameIsDone() {
        return gameIsDone;
    }
    public void sendMessage(String message) {

        out.println(message);
        consoleInput.setText(null);
        SwingUtilities.updateComponentTreeUI(mainFrame);

    }
    public int checkMessage(String message) {

        /*if help just print help message*/
        if (message.contains("/help")) {
            consoleOutputTextArea.append("/help:this message\n/bye: disconnect\n/who: shows name of all connected players\n/name (name): Rename yourslef\n");
            return 1;
        }
        /*if bye then tell server to kick user*/
        if (message.contains("/bye")) {
            consoleOutputTextArea.append("Disconnected from server.\n");
            connected = false;
            new disconnect();
            return 0;
        }
        /*if who then look at list for all who are in server*/
        if (message.contains("/who")) {
            /*while list is not null keep printing user names*/
            for (String user : ListUser) {
                consoleOutputTextArea.append("user:" + user +"\n");
            }
            return 1;
        }
        return 0;
    }

    /**
     * This method is a setter for gameIsDone.
     *
     * @param gameIsDone represents if game has been finished or not.
     */
    public static void setGameIsDone(boolean gameIsDone) {
        PicrossView.gameIsDone = gameIsDone;
    }
    class PicrossNetworkController extends Thread {
        public void run() {
            String message;
            /*while thread is not Interrupted try to parse message*/
            while(!Thread.currentThread().isInterrupted()&&connected){
                try {
                    message = input.readLine();
                    /*if recived message not null check if user can be found and print message*/
                    if(message != null){
						/*see if frist char is "[" acts as a flag that a user list is in message
		            	  I got this idea from a server example online*/
                        if (message.charAt(0) == '[') {
                            message = message.substring(1, message.length()-1);
                            ListUser = new Vector<String>(Arrays.asList(message.split(", ")));
                        } else {
                            consoleOutputTextArea.append(message+ "\n");
                        }
                    }
                }catch (SocketException ex){

                }
                /*catch any issues while parsing message*/
                catch (IOException ex) {
                    ex.printStackTrace();
                    consoleOutputTextArea.append("message failed to be parsed");
                }
            }
        }
    }
}