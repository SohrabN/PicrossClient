/**
 * @author Sohrab Najafzadeh
 * Student Number 040770197
 * Course: CST8221 - Java Applications
 * CET-CS-Level 4
 * @Version 1.0
 */

package piccross;

import javax.swing.SwingUtilities;

/**
 * This class represents our main and it will start the program.
 *
 * @author Sohrab.N
 */
public class GameRunner {
    PiccrossView frame;
    /**
     * Our main method inside our main class. This is the starting point of the
     * program.
     */
    public void runGame() {
        PiccrossSplashScreen splashWindow = new PiccrossSplashScreen(1000);// change to 5000 for final
        // Show the Splash screen
        splashWindow.showSplashWindow();
        // PicrossModel model = new PicrossModel();
        // PicrossView frame = model.getMainFrame();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PiccrossView frame = new PiccrossView();

                if (frame.startGUI(5) == -1)
                    System.err.println(
                            "ERROR: ERROR DETECTED IN startGUI METHOD. CHECK THE LOGS TO SEE WHAT IS CAUSING THIS ERROR.");
            }
        });

    }

}
