/**
 * @author Sohrab Najafzadeh
 * Student Number 040770197
 * Course: CST8221 - Java Applications
 * CET-CS-Level 4
 * @Version 1.0
 */
package piccross;

import java.awt.*;
import javax.swing.*;

/**
 * Beginning of PiccrossSplashScreen class
 */
public class PiccrossSplashScreen extends JWindow {

    private static final long serialVersionUID = 6248477390124803341L;

    private final int duration;
    static JProgressBar loadBar;

    /**
     * Beginning of PiccrossSplashScreen - Allows the duration
     */
    public PiccrossSplashScreen(int duration) {
        this.duration = duration;
    }// End

    /**
     * Beginning of showSplashWindow - Has all of the customizations that the
     * creators selected
     */
    public void showSplashWindow() {
        loadBar = new JProgressBar(0, duration);
        loadBar.setStringPainted(true);
        loadBar.setForeground(new Color(234, 197, 66));
        JPanel content = new JPanel(new BorderLayout());
        content.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        /**
         * Color selection
         */
        Color bColor = new Color(234, 197, 66);
        content.setBackground(bColor);

        /**
         * Size of everything
         */
        int width = 1100;
        int height = 700;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;

        /**
         * Images and text
         */
        JLabel label = new JLabel(new ImageIcon(getClass().getResource("piccrossNameMin.jpg")));
        JLabel demo = new JLabel("Sohrab Najafzadeh , Student ID: 040770197", JLabel.CENTER);
        demo.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        content.add(label, BorderLayout.CENTER);
        content.add(demo, BorderLayout.NORTH);
        content.add(loadBar, BorderLayout.SOUTH);

        // Color customColor = new Color(44, 197, 211);
        setBounds(x, y, width, height);
        content.setBorder(BorderFactory.createLineBorder(Color.black, 5));

        setContentPane(content);
        setLocationRelativeTo(null);
        setVisible(true);
        fill();
        dispose();

    }// End of splash window

    public void fill() {
        int i = 0;
        try {
            while (i <= duration) {
                // fill the menu bar
                loadBar.setValue(i + 10);

                // delay the thread
                Thread.sleep(19);
                i += 20;
            }
        } catch (Exception e) {
        }
    }
}// End of class
