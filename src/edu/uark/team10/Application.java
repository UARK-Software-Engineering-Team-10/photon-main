package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import edu.uark.team10.table.PlayerEntryTable;

/**
 * Responsible for the user interface.
 */
public class Application extends JFrame { // JFrame lets us create windows

    // Game and server instances
    private Game game = null;
    private UDPServer server = null;

    /**
     * Creates a new JFrame for the program.
     * 
     * @param game
     * @param server
     */
    public Application(Game game, UDPServer server)
    {
        this.game = game;
        this.server = server;

        // Calculate size of window to be ~35% the width of the screen while maintaining exactly 4:3 aspect ratio
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = (int) (Math.floor(screenSize.getWidth() * 0.35) - Math.floor(screenSize.getWidth() * 0.33) % 4); // 4 must divide width
        int windowHeight = (int) (windowWidth * 0.75);
        Dimension windowSize = new Dimension(Math.max(windowWidth, 896), Math.max(windowHeight, 672)); // Set minimum window size to 896x672
        
        // Configure the main window
        this.setTitle("Photon Laser Tag");
        this.setSize(windowSize); // Resize here
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);
        this.setFocusable(true);

        try {
            // Sets the "look and feel" of the program based on operating system
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        try {
            // Get font from jar and register it so we can use it later
            InputStream fontStream = ClassLoader.getSystemClassLoader().getResourceAsStream("edu/uark/team10/assets/Conthrax-SemiBold.otf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream); // "Conthrax SemBd"
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        // Create the splash screen and add it to the screen
        splashScreen();
    }

    /**
     * Creates a menu bar
     * Includes options for starting the game, clearing player entries, changing
     * binding IP address, and test mode.
     * 
     * @param tableModelRedTeam
     * @param tableModelGreenTeam
     * @return The created menu bar to be added to a frame
     */
    private JMenuBar getMenuBar(PlayerEntryTable tableRedTeam, PlayerEntryTable tableGreenTeam) {
        // Setup the menu bar for Start/settings
        JMenuBar menuBar = new JMenuBar(); // Menu bar container holds menus
        // Game menu holds options for starting game and clearing player entries
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setFont(new Font("Conthrax SemBd", Font.PLAIN, 11));
        // Settings menu holds options for changing binding ip address and enabling test mode
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(new Font("Conthrax SemBd", Font.PLAIN, 11));
        
        /*
         * Start game item:
         * Starts the game when selected. Takes all added players in the red and green team tables and adds them to the Game instance.
         */
        JMenuItem startGameItem = new JMenuItem("Start Game");
        startGameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Game started!", "Game Status", JOptionPane.INFORMATION_MESSAGE);
                countdown(10);
                game.start(server, tableRedTeam, tableGreenTeam); // Start the game
            }
        });

        startGameItem.setFont(new Font("Conthrax SemBd", Font.PLAIN, 11));

        // Start game button icon
        ImageIcon startIcon = new ImageIcon(getClass().getClassLoader().getResource("edu/uark/team10/assets/icons/start.png"));
        startGameItem.setIcon(startIcon);

        /*
         * Clear entries item:
         * Clears the red and green team tables. Sets all values in the table to null.
         */
        JMenuItem clearEntriesItem = new JMenuItem("Clear Entries");
        clearEntriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "Cleared player entries.", "Entries", JOptionPane.INFORMATION_MESSAGE);

            // Set table values to null
            tableRedTeam.clear();
            tableGreenTeam.clear();

        }});

        clearEntriesItem.setFont(new Font("Conthrax SemBd", Font.PLAIN, 11));

        // Clear entries button icon
        ImageIcon clearIcon = new ImageIcon(getClass().getClassLoader().getResource("edu/uark/team10/assets/icons/clear.png"));
        clearEntriesItem.setIcon(clearIcon);

        /*
         * Change IP network item:
         * Prompts the user for an IP address to bind to. The default is localhost (127.0.0.1).
         */
        JMenuItem changeIPNetwork = new JMenuItem("Change IP/Network");
        changeIPNetwork.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                                                                         // QUESTION_MESSAGE: Request user input for IP/Network
                String newIpAddress = JOptionPane.showInputDialog(null, "Enter new IP Address:", "Change IP/Network: " + UDPServer.networkAddress, JOptionPane.QUESTION_MESSAGE);

                // May be null if user selects 'cancel'
                if (newIpAddress == null) return;

                newIpAddress = newIpAddress.replaceAll("[^0-9.]", ""); // Only allow numbers and periods for ip addresses

                if (newIpAddress != null && !newIpAddress.isEmpty()) { // Check if the input is not empty
                    UDPServer.networkAddress = newIpAddress; // Change the static network address
                    JOptionPane.showMessageDialog(null, "IP Address changed to: " + newIpAddress, "IP/Network Status", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "IP Address cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
                }
        });

        changeIPNetwork.setFont(new Font("Conthrax SemBd", Font.PLAIN, 11));

        // Change network button icon
        ImageIcon networkIcon = new ImageIcon(getClass().getClassLoader().getResource("edu/uark/team10/assets/icons/network.png"));
        changeIPNetwork.setIcon(networkIcon);

        /*
         * Enable testing mode item:
         * Enables testing mode when selected. Shortens the start countdown and game length to a total of 35 seconds.
         */
        JMenuItem enableTestingMode = new JMenuItem("Enable Testing Mode");
        enableTestingMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "Testing mode enabled.", "Testing Mode", JOptionPane.INFORMATION_MESSAGE);
            Game.isTestingMode = true; // Set static testing mode to true
        }});

        enableTestingMode.setFont(new Font("Conthrax SemBd", Font.PLAIN, 11));

        // Enable testing mode button icon
        ImageIcon testingIcon = new ImageIcon(getClass().getClassLoader().getResource("edu/uark/team10/assets/icons/testing.png"));
        enableTestingMode.setIcon(testingIcon);

        // Add items to the game menu
        gameMenu.add(startGameItem);
        gameMenu.add(clearEntriesItem);
        // Add items to the settings menu
        settingsMenu.add(changeIPNetwork);
        settingsMenu.add(enableTestingMode);
        // Add menus to the menu bar
        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        
        return menuBar;
    }

    /**
     * Creates the splash screen and displays it on this JFrame.
     * Splash image is the photon lazer tag logo.
     * Calls the player entry screen function after 3 seconds, or
     * after a mouse click.
     */
    private void splashScreen()
    {
        /*
         * Removes all components (buttons, labels, ..)
         * Necessary when adding new components after using removeAll()
         */
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        // Load the splash image (logo)
        URL logoUrl = getClass().getClassLoader().getResource("edu/uark/team10/assets/logo.png");
        ImageIcon logoImage = new ImageIcon(logoUrl);
        logoImage = new ImageIcon(logoImage.getImage().getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_DEFAULT));

        // Add image to component
        final JLabel splashLabel = new JLabel();
        splashLabel.setIcon(logoImage);

        // Add image component to container
        final JPanel splashPanel = new JPanel();
        splashPanel.add(splashLabel);

        // A Future that completes itself after 3 seconds
        // OR completes when the splash screen is clicked
        CompletableFuture<Void> splashScreenFuture = new CompletableFuture<Void>().completeOnTimeout(null, 3L, TimeUnit.SECONDS);
        splashScreenFuture.whenComplete((none, exception) -> {
            if (exception != null)
            {
                exception.printStackTrace();
            }

            // Create and draw player entry screen
            playerEntryScreen();

        });

        // Add a listener to the panel
        splashPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                // Completes the future if not already complete.
                splashScreenFuture.complete(null);
            }

            // Unused abstract methods
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}

        });
        
        // Add the panel to this frame
        this.setLayout(new BorderLayout());
        this.add(splashPanel, BorderLayout.CENTER);

        this.validate(); // Validate the components in this frame
    }

    // Changes the screen to the player entry screen
    private void playerEntryScreen() {
        /*
         * Removes all components (buttons, labels, ..)
         * Necessary when adding new components after using removeAll()
         */
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        // Create the red and green team tables
        PlayerEntryTable tableRedTeam = new PlayerEntryTable(this.server, new Color(122, 0, 0));
        PlayerEntryTable tableGreenTeam = new PlayerEntryTable(this.server, new Color(0, 122, 0));

        // Add the tables to a scroll pane
        JScrollPane scrollPaneRedTeam = new JScrollPane(tableRedTeam) {
            @Override
            public Dimension getPreferredSize() {
                // Override the size of the scroll pane to fit the exact size of the table
                return new Dimension(425 + tableRedTeam.getColumnModel().getColumnMargin() * 2,
                                    tableRedTeam.getRowHeight() * tableRedTeam.getRowCount() + tableRedTeam.getRowMargin() * tableRedTeam.getRowCount() * 2);
            }
        };

        JScrollPane scrollPaneGreenTeam = new JScrollPane(tableGreenTeam) {
            @Override
            public Dimension getPreferredSize() {
                // Override the size of the scroll pane to fit the exact size of the table
                return new Dimension(425 + tableGreenTeam.getColumnModel().getColumnMargin() * 2,
                                    tableGreenTeam.getRowHeight() * tableGreenTeam.getRowCount() + tableGreenTeam.getRowMargin() * tableGreenTeam.getRowCount() * 2);
            }
        };

        // Add the scroll panes to a panel
        JPanel tablePanel = new JPanel();
        tablePanel.add(scrollPaneRedTeam, BorderLayout.WEST); // Red team table to the left
        tablePanel.add(scrollPaneGreenTeam, BorderLayout.EAST); // Green team table to the right

        tablePanel.setBackground(new Color(28, 0, 64));

        // Create and add the menu bar for game settings
        this.setJMenuBar(getMenuBar(tableRedTeam, tableGreenTeam));

        // Add the table panel to this frame
        this.setLayout(new BorderLayout());
        this.add(tablePanel, BorderLayout.NORTH);

        this.getContentPane().setBackground(new Color(28, 0, 64));

        this.validate(); // Validate the components in this frame

        // Sets F12 to start game
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Key Pressed: " + e.getKeyCode()); // Debugging, needs to check focus after entering player info
                if (e.getKeyCode() == KeyEvent.VK_F12) {
                    JOptionPane.showMessageDialog(null, "Game started!", "Game Status", JOptionPane.INFORMATION_MESSAGE);
                    countdown(10);
                    game.start(server, null, null);
                }
            }
        });
    }

    private void countdown(int startFrom) {
        // Clear player entry screen
        this.getContentPane().removeAll();
        this.setJMenuBar(null);
        this.revalidate();
        this.repaint();      
        
        final JPanel countdownPanel = new JPanel(null);
        countdownPanel.setBackground(new Color(28, 0, 64));
        
        URL logoUrl = getClass().getClassLoader().getResource("edu/uark/team10/assets/logoBK.png");
        ImageIcon logoImage = new ImageIcon(logoUrl);
        logoImage = new ImageIcon(
                logoImage.getImage().getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_DEFAULT));

        final JLabel backgroundLabel = new JLabel(logoImage);
        backgroundLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
        backgroundLabel.setOpaque(false);
        
        final JLabel countdownLabel = new JLabel(String.valueOf(startFrom), JLabel.CENTER);
        countdownLabel.setFont(new Font("Conthrax SemBd", Font.BOLD, 250));
        countdownLabel.setForeground(Color.YELLOW);
        countdownLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
        
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, this.getWidth(), this.getHeight());
        countdownPanel.add(layeredPane);

        layeredPane.add(backgroundLabel, Integer.valueOf(0)); // Layer 0=background
        layeredPane.add(countdownLabel, Integer.valueOf(1));
        
        this.setLayout(new BorderLayout());
        this.add(countdownPanel, BorderLayout.CENTER);
        this.validate();
        
        // Using a thread for countdown
        Thread countdownThread = new Thread(() -> {
            int count = startFrom;
            try {
                while (count > 0) {
                    final int currentCount = count;

                    SwingUtilities.invokeLater(() -> {
                        countdownLabel.setText(String.valueOf(currentCount));
                    });

                    Thread.sleep(1000);
                    count--;
                }
                
                // When countdown reaches 0, wait a second
                SwingUtilities.invokeLater(() -> {
                    countdownLabel.setText("0");
                });
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        countdownThread.start();
    }
}
