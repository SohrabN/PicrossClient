/**
 * @author Sohrab Najafzadeh
 * Student Number 040770197
 * Course: CST8221 - Java Applications
 * CET-CS-Level 4
 * @Version 1.0
 */
package piccross;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Picross class represents our frame in Picross game.
 *
 * @author Sohrab.N
 */
public class PiccrossView extends JFrame {
    /**
     * Represents the game in 0 and 1 for sending it to server.
     */
    static String gameBoolVec;

    /**
     * Represents the input received from server to client.
     */
    BufferedReader input;

    /**
     * Represents the out going string or output from client to server.
     */
    static PrintStream out;

    /**
     * Represents the socket that we make the connection to server.
     */
    static Socket socket;

    /**
     * PiccrossNetworkModalVC object which will be used as backend of dialog box for connecting to server.
     */
    static PiccrossNetworkModalVC connect;

    /**
     * Represents a boolean value that shows if we are connected to a server or not.
     */
    boolean connected = false;

    /**
     * Represents a thread which will run the PicrossNetworkController run method.
     */
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
    private static PiccrossView mainFrame;

    /**
     * panels we are using in our frame
     */
    private final PiccrossModel piccrossModel;

    /**
     * westPanel represent the panel on left side of frame.
     */
    private final JPanel westPanel = new JPanel();

    /**
     * markAndLogoPanel will hold logo and mark panel. markAndLogoPanel is placed at
     * PAGE_START of westPanel.
     */
    private final JPanel markAndLogoPanel = new JPanel();

    /**
     * consolePanel will hold point panel, timer panel and scroll pane inside it
     * self. consolePanel is placed at CENTER of westPanel.
     */
    private final JPanel consolePanel = new JPanel();

    /**
     * pointPanel will hold pointTextField.pointPanel is placed at NORTH of
     * consolePanel.
     */
    private final JPanel pointPanel = new JPanel();

    /**
     * timerPanel which will show the time since game has been started.
     */
    private final JPanel timerPanel = new JPanel();

    /**
     * Represents the area where user can see the output from the game and server.
     */
    private final JPanel consoleInputPanel = new JPanel();

    /**
     * Represents the input area where user can enter commands or chat with other players when connected to server.
     */
    private JTextField consoleInput;

    /**
     * logoLabel will hold the logo to our Picross game.logoLabel is placed at
     * PAGE_START of markAndLogoPanel.
     */
    private final JLabel logoLabel = new JLabel();

    /**
     * pointLabel will display "Points : " in our Picross game.pointLabel is a label
     * inside pointPanel.
     */
    private final JLabel pointLabel = new JLabel();

    /**
     * resetButton will restart our game (CURRENTLY NOT WORKING).
     */
    private final JButton resetButton = new JButton();

    /**
     * consoleOutputTextArea will display outputs to the player. it will be used inside
     * scrollerScrollPane to add scrolling ability to it.
     */
    private static JTextArea consoleOutputTextArea;

    /**
     * scrollerScrollPane will hold consoleOutputTextArea and will add scroll ability to
     * it.scrollerScrollPane is placed at SOUTH of consolePanel.
     */
    private final JScrollPane scrollerScrollPane;

    /**
     * MarkCheckBox will display a message if its change and display another message
     * if set to uncheck.MarkCheckBox is placed at CENTER of markAndLogoPanel.
     */
    private final JCheckBox MarkCheckBox = new JCheckBox();

    /**
     * timerLabel will display the current time of playing game.timerLabel is a
     * component of timerPanel.
     */
    private static JLabel timerLabel;

    /**
     * used to indicate if the game is finished.
     */
    private static boolean gameIsDone;

    /**
     * Used to add menu bar to UI.
     */
    private final JMenuBar menuBar;

    /**
     * Represents the JMenuItem object for connecting to server.
     */
    private JMenuItem menuItemConnect;

    /**
     * Represents the JMenuItem object for disconnect to server.
     */
    private JMenuItem menuItemDisconnect;

    /**
     * Represents the object of ControllableTimer class which is used for timer.
     */
    private static ControllableTimer timer;

    /**
     * Picross class object constructor.
     */
    public PiccrossView() {
        // setting name of frame
        super("Picross 3.0");
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        piccrossModel = new PiccrossModel();
        timerLabel = new JLabel("");
        connect = new PiccrossNetworkModalVC(this);
        consoleOutputTextArea = new JTextArea(24, 30);
        scrollerScrollPane = new JScrollPane(consoleOutputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Picross class object constructor that will be called if a new game is called after running the initial game.
     */
    public PiccrossView(Socket socket, JTextArea consoleOutputTextArea, boolean connected) {
        // setting name of frame
        super("Picross 3.0");
        this.connected = connected;
        PiccrossView.consoleOutputTextArea.setText(consoleOutputTextArea.getText());
        PiccrossView.socket = socket;
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        piccrossModel = new PiccrossModel();
        timerLabel = new JLabel("");
        connect = new PiccrossNetworkModalVC(this);
        scrollerScrollPane = new JScrollPane(consoleOutputTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        if (this.connected) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    consoleInput.setText("/bye");
                    consoleInput.dispatchEvent(new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER));
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    mainFrame.connected = false;
                    System.exit(0);
                }
            });
        } else {
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
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
        gridSize = gameSize;
        // creating new object of Picross class and storing it in mainFrame
        mainFrame = this;
        URL iconURL = getClass().getResource(iconPath);
        if (iconURL == null) {
            System.err.println("ERROR: ICON PATH NOT FOUND. PLEASE CHECK THE iconPath VARIABLE.");
            return -1;
        }
        ImageIcon img = new ImageIcon(iconURL);
        mainFrame.setIconImage(img.getImage());
        piccrossModel.setShowSolution(false);
        gameIsDone = false;
        // sets up the menu bar
        mainFrame.buildMenu();
        if (connected) {
            menuItemDisconnect.setEnabled(true);
            menuItemConnect.setEnabled(false);
        }
        // sets up the 5x5 grid
        piccrossModel.setup(debugMode);
        JPanel gameGrid = piccrossModel.getGridPanel();
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
        consoleOutputTextArea.setPreferredSize(new Dimension(500, 4000));
        consoleOutputTextArea.setEditable(false);

        consoleInput.addKeyListener(new textAreaListener());
        // timer panel setup
        timerPanel.setPreferredSize(new Dimension(250, 50));
        timerLabel.setFont(new Font("TimesNewRoman", Font.BOLD, 14));
        timerLabel.setText("00" + ":" + "00" + ":" + "00");
        timerPanel.add(timerLabel);

        // point panel setup
        pointLabel.setText("Points : ");
        pointPanel.add(pointLabel, new BorderLayout(5, 5));
        piccrossModel.getPointTextField().setEditable(false);
        piccrossModel.getPointTextField().setBackground(Color.WHITE);
        piccrossModel.getPointTextField().setText("   " + 0 + "/" + gridSize * gridSize + "  ");
        pointPanel.add(piccrossModel.getPointTextField(), new BorderLayout(5, 5));

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
        timer = new ControllableTimer(mainFrame);
        timer.start();
        return 0;
    }

    /**
     * This method is a getter to timer object.
     *
     * @return Returns timer object which is ControllableTimer object that used to start stop restart and terminal the timer.
     */
    public static ControllableTimer getTimer() {
        return timer;
    }

    /**
     * This method is a getter to timer object.
     *
     * @return Returns timerLabel object which is JLabel object that used to show time that user spent on each game.
     */
    public JLabel getTimerLabel() {
        return timerLabel;
    }

    /**
     * This method will calculate the time that our game has been running and
     * returns it as string in proper format.
     *
     * @return String is returned to the calling which will be the time since game
     * has been started.
     */
    public static String getTimeElapsed(int elapsed) {
        // getting time since the game has been started
        String seconds = Integer.toString((int) ((long) elapsed % 60));
        String minutes = Integer.toString((int) (((long) elapsed % 3600) / 60));
        String hours = Integer.toString((int) ((long) elapsed / 3600));

        if (seconds.length() < 2) seconds = "0" + seconds;

        if (minutes.length() < 2) minutes = "0" + minutes;

        if (hours.length() < 2) hours = "0" + hours;
        // returning the timer in correct format
        timerLabel.setText(hours + ":" + minutes + ":" + seconds);
        return hours + ":" + minutes + ":" + seconds;
    }

    /**
     * This method is a getter to String gameBoolVec object.
     * @return Returns gameBoolVec which represent the game in string format as 0 and 1 which will be used to send the game to server.
     */
    public static String getGameBoolVec() {
        return gameBoolVec;
    }

    /**
     * This method is a getter to JTextArea consoleOutputTextArea which represents the text area that user will see output of program or server.
     * @return Returns consoleOutputTextArea.
     */
    public static JTextArea getConsoleOutputTextArea() {
        return consoleOutputTextArea;
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
                piccrossModel.setMark(true);

            } else {
                System.out.println("Mark NOT Selected");
                piccrossModel.setMark(false);
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
            timer.setStatus(ControllableTimer.RESET);
            timer.setStatus(ControllableTimer.START);
            if (piccrossModel.isShowSolution()) piccrossModel.showSolution();
            timerLabel.setText("00:00:00");
            piccrossModel.resetGame();
            gameIsDone = false;
        }
    }

    /**
     * ActionListener for show about in menu which will pop up a dialog windows and
     * shows information regarding software.
     *
     * @author Sohrab.N
     */
    private static class showAbout implements ActionListener {

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
            piccrossModel.showSolution();

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
            mainFrame = new PiccrossView(socket, consoleOutputTextArea, connected);
            timer.setStatus(ControllableTimer.RESET);
            SwingUtilities.updateComponentTreeUI(mainFrame);
            mainFrame.startGUI(5);

        }
    }

    /**
     * This class will implement ActionListener and will set the size of game to 5*5 when selected from menu.
     */
    private class setSize5 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gridSize = 5;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PiccrossView(socket, consoleOutputTextArea, connected);
            timer.stop();
            SwingUtilities.updateComponentTreeUI(mainFrame);
            mainFrame.startGUI(gridSize);
        }
    }

    /**
     * This class will implement ActionListener and will set the size of game to 10*10 when selected from menu.
     */
    private class setSize10 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gridSize = 10;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PiccrossView(socket, consoleOutputTextArea, connected);
            timer.stop();
            SwingUtilities.updateComponentTreeUI(mainFrame);
            mainFrame.startGUI(gridSize);
        }
    }

    /**
     * This class will implement ActionListener and will set the size of game to 15*15 when selected from menu.
     */
    private class setSize15 implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 0;
            gridSize = 15;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PiccrossView(socket, consoleOutputTextArea, connected);
            timer.stop();
            SwingUtilities.updateComponentTreeUI(mainFrame);
            mainFrame.startGUI(gridSize);
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
            if (connected) {
                consoleInput.setText("/bye");
                consoleInput.dispatchEvent(new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER));
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                connected = false;
                menuItemConnect.setEnabled(true);
                menuItemDisconnect.setEnabled(false);
            }
            System.exit(0);
        }
    }

    /**
     * ActionListener for debug sub menu debug configuration 1 which will create a
     * predefined game that all outside boxes are correct.
     *
     * @author Sohrab.N
     */
    private static class debugConfig1 implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 1;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PiccrossView();
            mainFrame.startGUI(5);
        }
    }

    /**
     * ActionListener for debug sub menu debug configuration 2 which will create a
     * predefined game that only the middle box is correct.
     *
     * @author Sohrab.N
     */
    private static class debugConfig2 implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 2;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PiccrossView();
            mainFrame.startGUI(5);
        }
    }

    /**
     * ActionListener for debug sub menu debug configuration 3 which will create a
     * predefined game that all boxes are false.
     *
     * @author Sohrab.N
     */
    private static class debugConfig3 implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            debugMode = 3;
            gameIsDone = true;
            mainFrame.dispose();
            mainFrame = new PiccrossView();
            mainFrame.startGUI(5);
        }
    }

    /**
     * This class will implement ActionListener and will open a dialog box to get information from user and try connects to server.
     */
    private class openConnection extends Thread implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Connect connect = new Connect();
            Thread thread = new Thread(connect);
            thread.start();
        }

        private class Connect implements Runnable {

            @Override
            public void run() {
                Point thisLocation = getLocation();
                Dimension parentSize = getSize();
                Dimension modalSize = connect.getSize();
                int offsetX = (parentSize.width - modalSize.width) / 2 + thisLocation.x;
                int offsetY = (parentSize.height - modalSize.height) / 2 + thisLocation.y;
                connect.setLocation(offsetX, offsetY);
                connect.setVisible(true);
                int port = connect.getPort();
                String host = connect.getAddress();
                if (port != -1) {
                    consoleOutputTextArea.append("Negotiating connection to " + host + " on port " + port + "\n");
                    try {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(InetAddress.getByName(host), port), 10000);
                        try {
                            consoleOutputTextArea.append("Connection Successful\n");
                            consoleOutputTextArea.append("\n###################################\nWelcome to Sohrab's Picross Server.\n###################################\n");
                            consoleOutputTextArea.append("User '/help' for commands.\n");
                            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            out = new PrintStream(socket.getOutputStream(), true);
                            String name = connect.getName();
                            out.println(name);
                            connected = true;
                            menuItemConnect.setEnabled(false);
                            menuItemDisconnect.setEnabled(true);
                            thread = new PicrossNetworkController();
                            addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent e) {

                                    consoleInput.setText("/bye");
                                    consoleInput.dispatchEvent(new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER));
                                    try {
                                        socket.close();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    mainFrame.connected = false;
                                    menuItemConnect.setEnabled(true);
                                    menuItemDisconnect.setEnabled(false);

                                    System.exit(0);
                                }
                            });


                            thread.start();

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } catch (SocketTimeoutException ex) {
                        consoleOutputTextArea.append("ALERT: Connection Timeout!\nPlease make sure IP or hostname entered is correct.\n");
                    } catch (UnknownHostException ex) {
                        consoleOutputTextArea.append("ALERT: hostname entered for server is not correct!\nALERT: Please try to connect again and enter correct hostname or IP.\n");
                    } catch (ConnectException ex) {
                        consoleOutputTextArea.append("ALERT: Connection refused!\nALERT: Please make sure server is running and port " + port + " is not blocked by firewall.\n");
                    } catch (Exception ex) {
                        consoleOutputTextArea.append("ERR: General error while trying to connect to server.\nPlease report this issue and behaviour to programmer to patch!\n");
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * This class will implement ActionListener and disconnects from server if selected from menu.
     */
    private class disconnect implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {

                consoleInput.setText("/bye");
                int keyCode = KeyEvent.VK_ENTER;
                consoleInput.dispatchEvent(new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER));
                String userName = connect.getName();
                socket.close();
                connected = false;
                menuItemConnect.setEnabled(true);
                menuItemDisconnect.setEnabled(false);
                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This class will implement KeyListener and will implement the lister for console input text area so when ever user press enter key it will execute the code.
     */
    private class textAreaListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER && connected) {
                String consoleText = consoleInput.getText();
                if (checkMessage(consoleText) == 0) {
                    sendMessage(consoleText);
                }
                Dimension dimension = consoleOutputTextArea.getSize();
                dimension.width = dimension.width + 1;
                dimension.height = dimension.height + 1;
                consoleOutputTextArea.setSize(dimension);
                consoleInput.setText(null);
                mainFrame.repaint();
                mainFrame.revalidate();
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER && !connected) {
                String consoleText = consoleInput.getText();
                if (consoleText.equals("/cls")) {
                    consoleOutputTextArea.setText(null);
                } else {
                    consoleOutputTextArea.append("NOT connected!\nIn order to interact with command prompt please connect to server to Picross Server.\n");
                }
                consoleInput.setText(null);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    /**
     * This method will implement ActionListener and will enable or disable hover feature of the game.
     */
    private static class hoverToggle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            hover = !hover;
        }
    }

    /**
     * This method will create the menu and sub menus and add the specific action
     * listener to each item.
     */
    private void buildMenu() {

        ImageIcon picLogo;
        // creating game menu
        JMenu menu = new JMenu("Game");
        // creating new menu item
        JMenuItem menuItem = new JMenuItem("New");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("NewGameIcon.png")));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new newGame());
        KeyStroke ctrlVKeyStroke = KeyStroke.getKeyStroke("control N");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        menu.addSeparator();
        JMenu subMenu = new JMenu("Set Game Size");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("SetGameSizeIcon.png")));
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
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("MouseHoverIcon.png")));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new hoverToggle());
        menu.add(menuItem);
        menu.addSeparator();
        // creating debug sub menu
        subMenu = new JMenu("Debug");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("DebugIcon.png")));
        subMenu.setIcon(picLogo);
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
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("ExitIcon.png")));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new exit());
        ctrlVKeyStroke = KeyStroke.getKeyStroke("control Q");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        menuBar.add(menu);
        menu = new JMenu("Networking");
        // creating solution menu item
        menuItemConnect = new JMenuItem("New Connection");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("ConnectIcon.png")));
        menuItemConnect.setIcon(picLogo);
        menuItemConnect.addActionListener(new openConnection());
        menu.add(menuItemConnect);
        menuItemDisconnect = new JMenuItem("Disconnect");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("DisconnectIcon.png")));
        menuItemDisconnect.setIcon(picLogo);
        menuItemDisconnect.addActionListener(new disconnect());
        menuItemDisconnect.setEnabled(false);
        menu.add(menuItemDisconnect);
        menuBar.add(menu);

        // creating help menu
        menu = new JMenu("Help");
        // creating solution menu item
        menuItem = new JMenuItem("Solution");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("ShowSolutionIcon.png")));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new showSolution());
        ctrlVKeyStroke = KeyStroke.getKeyStroke("alt S");
        menuItem.setAccelerator(ctrlVKeyStroke);
        menu.add(menuItem);
        // creating about menu item
        menuItem = new JMenuItem("About");
        picLogo = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("AboutIcon.png")));
        menuItem.setIcon(picLogo);
        menuItem.addActionListener(new showAbout());
        ctrlVKeyStroke = KeyStroke.getKeyStroke("alt A");
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
     * This method is a getter for Socket which represents the socket that clients makes the connection to server.
     * @return Returns socket which is client socket from client to server.
     */
    public static Socket getSocket() {return socket;}

    /**
     * This method is a getter for gameIsDone variable.
     *
     * @return gameIsDone represents if game has been finished or not.
     */
    public static boolean isGameIsDone() {
        return gameIsDone;
    }

    /**
     * This method will send a message in string to server. It can be command, or it can be just regular chat.
     * @param message Represents the message in String entered by user.
     */
    public void sendMessage(String message) {

        out.println(message);
        consoleInput.setText(null);
        SwingUtilities.updateComponentTreeUI(mainFrame);

    }

    /**
     * This method will check the message entered by user and tries to match which one of the case below.
     * @param message Represents the message in String entered by user.
     * @return Returns 0 and 1, if 0 means send the message to server, if 1 it means do not send message to server and message will be used locally.
     */
    public int checkMessage(String message) {

        if (message.contains("/bye")) {
            consoleOutputTextArea.append("Disconnected from server.\n");
            connected = false;
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            connected = false;
            menuItemConnect.setEnabled(true);
            menuItemDisconnect.setEnabled(false);
            return 0;
        }
        if (message.equals("/cls")) {
            consoleOutputTextArea.setText(null);
            return 1;
        }
        if (message.equals("/upload")) {
            piccrossModel.sendGameToServer();
            return 1;
        }
        if (message.startsWith("[1") || message.startsWith("[0")) {

            message = message.substring(1, message.length() - 2);
            System.out.println(message);
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
        PiccrossView.gameIsDone = gameIsDone;
    }

    /**
     *
     */
    class PicrossNetworkController extends Thread {
        public void run() {
            String message;
            /*while thread is not Interrupted try to parse message*/
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                try {
                    message = input.readLine();
                    /*if recived message not null check if user can be found and print message*/
                    if (message != null) {
						/*see if frist char is "[" acts as a flag that a user list is in message
		            	  I got this idea from a server example online*/
                        try {
                            if (message.startsWith("[1") || message.startsWith("[0")) {
                                message = message.substring(1, message.length() - 1);
                                gameBoolVec = message;
                                debugMode = 4;
                                gameIsDone = true;
                                timer.setStatus(ControllableTimer.TERMINATE);
                                timer.stop();
                                timerLabel.setText("00:00:00");
                                mainFrame.dispose();
                                mainFrame = new PiccrossView(socket, consoleOutputTextArea, connected);
                                mainFrame.startGUI(5);
                            } else {
                                consoleOutputTextArea.append(message + "\n");

                            }
                        } catch (StringIndexOutOfBoundsException e) {

                        }
                    }
                } catch (SocketException ex) {
                    consoleOutputTextArea.append("Connection was terminated from server side.\n");
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    consoleOutputTextArea.append("message failed to be parsed");
                }
            }
            if (connected) {
                consoleInput.setText("/bye");
                consoleInput.dispatchEvent(new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER));
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                connected = false;
                menuItemConnect.setEnabled(true);
                menuItemDisconnect.setEnabled(false);
            }

        }

    }
}