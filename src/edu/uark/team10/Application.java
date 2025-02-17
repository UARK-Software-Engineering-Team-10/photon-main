package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

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

        // Get logo for splash screen
        URL logoUrl = getClass().getClassLoader().getResource("edu/uark/team10/assets/logo.png"); // Resource is in the jar
        ImageIcon logoImage = new ImageIcon(logoUrl);
        
        // Configure the main window
        this.setTitle("Photon Laser Tag");
        this.setSize(logoImage.getIconWidth(), logoImage.getIconHeight());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);

        // Create the splash screen and add it to the screen
        splashScreen();
    }

    /**
     * Creates a menu bar and adds it to this frame.
     * Includes options for starting the game, clearing player entries, changing
     * binding IP address, and test mode.
     * 
     * @param tableModelRedTeam
     * @param tableModelGreenTeam
     */
    private void setupMenuBar(PlayerEntryTableModel tableModelRedTeam, PlayerEntryTableModel tableModelGreenTeam) {
        // Setup the menu bar for Start/settings
        JMenuBar menuBar = new JMenuBar(); // Menu bar container holds menus
        // Game menu holds options for starting game and clearing player entries
        JMenu gameMenu = new JMenu("Game");
        // Settings menu holds options for changing binding ip address and enabling test mode
        JMenu settingsMenu = new JMenu("Settings");
        
        /*
         * Start game item:
         * Starts the game when selected. Takes all added players in the red and green team tables and adds them to the Game instance.
         */
        JMenuItem startGameItem = new JMenuItem("Start Game");
        startGameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Game started!", "Game Status", JOptionPane.INFORMATION_MESSAGE);

                // Add red team players to the game instance
                for (Object[] playerData : tableModelRedTeam.getRowData(Game.RED_TEAM_NUMBER))
                {
                    game.addPlayer(playerData);
                }

                // Add green team players to the instance
                for (Object[] playerData : tableModelGreenTeam.getRowData(Game.GREEN_TEAM_NUMBER))
                {
                    game.addPlayer(playerData);
                }

                game.start(server); // Start the game
            }
        });

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
            tableModelRedTeam.clear();
            tableModelGreenTeam.clear();

        }});

        /*
         * Change IP network item:
         * Prompts the user for an IP address to bind to. The default is localhost (127.0.0.1).
         */
        JMenuItem changeIPNetwork = new JMenuItem("Change IP/Network");
        changeIPNetwork.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                                                                         // QUESTION_MESSAGE: Request user input for IP/Network
            String newIpAddress = JOptionPane.showInputDialog(null, "Enter new IP Address:", "Change IP/Network", JOptionPane.QUESTION_MESSAGE);
            newIpAddress = newIpAddress.replaceAll("[^0-9.]", ""); // Only allow numbers and periods for ip addresses

            if (newIpAddress != null && !newIpAddress.isEmpty()) { // Check if the input is not empty
                UDPServer.networkAddress = newIpAddress; // Change the static network address
                JOptionPane.showMessageDialog(null, "IP Address changed to: " + newIpAddress, "IP/Network Status", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "IP Address cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
            }
        });

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

        // Add items to the game menu
        gameMenu.add(startGameItem);
        gameMenu.add(clearEntriesItem);
        // Add items to the settings menu
        settingsMenu.add(changeIPNetwork);
        settingsMenu.add(enableTestingMode);
        // Add menus to the menu bar
        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        // Add menu to this frame
        this.setJMenuBar(menuBar);
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
        
        
        // Create a new instance of the custom table model.
        // Pass in server so we can send the equipment ID on add player.
        PlayerEntryTableModel tableModelRedTeam = new PlayerEntryTableModel(this.server);
        // Create the red team table
        JTable tableRedTeam = new JTable(tableModelRedTeam); // Use the custom table model on the table
        // Configure table options
        tableRedTeam.getTableHeader().setReorderingAllowed(false);
        tableRedTeam.getTableHeader().setResizingAllowed(false);
        tableRedTeam.setFillsViewportHeight(true);
        tableRedTeam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableRedTeam.setRowSelectionAllowed(false);
        tableRedTeam.setColumnSelectionAllowed(false);
        tableRedTeam.setBackground(new Color(122, 0, 0));

        // Create a new instance of the custom table model.
        // Pass in server so we can send the equipment ID on add player.
        PlayerEntryTableModel tableModelGreenTeam = new PlayerEntryTableModel(this.server);
        // Create the green team table
        JTable tableGreenTeam = new JTable(tableModelGreenTeam); // Use the custom table model on the table
        // Configure table options
        tableGreenTeam.getTableHeader().setReorderingAllowed(false);
        tableGreenTeam.getTableHeader().setResizingAllowed(false);
        tableGreenTeam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableGreenTeam.setFillsViewportHeight(true);
        tableGreenTeam.setRowSelectionAllowed(false);
        tableGreenTeam.setColumnSelectionAllowed(false);
        tableGreenTeam.setBackground(new Color(0, 122, 0));

        // Custom cell renderer for red and green team (shared)
        PlayerEntryTableCellRenderer cellRenderer = new PlayerEntryTableCellRenderer();

        // Configure column options for red team
        TableColumnModel columnModelRedTeam = tableRedTeam.getColumnModel();
        columnModelRedTeam.getColumn(0).setPreferredWidth(50);
        columnModelRedTeam.getColumn(1).setPreferredWidth(100);
        columnModelRedTeam.getColumn(2).setPreferredWidth(200);
        columnModelRedTeam.removeColumn(columnModelRedTeam.getColumn(3)); // Hide column 3 (equipment IDs)
        // Set cell renderer for red team
        columnModelRedTeam.getColumn(0).setCellRenderer(cellRenderer);
        columnModelRedTeam.getColumn(1).setCellRenderer(cellRenderer);
        columnModelRedTeam.getColumn(2).setCellRenderer(cellRenderer);
        columnModelRedTeam.setColumnMargin(4);

        // Configure column options for green team
        TableColumnModel columnModelGreenTeam = tableGreenTeam.getColumnModel();
        columnModelGreenTeam.getColumn(0).setPreferredWidth(50);
        columnModelGreenTeam.getColumn(1).setPreferredWidth(100);
        columnModelGreenTeam.getColumn(2).setPreferredWidth(200);
        columnModelGreenTeam.removeColumn(columnModelGreenTeam.getColumn(3)); // Hide column 3 (equipment IDs)
        // Set cell renderer for green team
        columnModelGreenTeam.getColumn(0).setCellRenderer(cellRenderer);
        columnModelGreenTeam.getColumn(1).setCellRenderer(cellRenderer);
        columnModelGreenTeam.getColumn(2).setCellRenderer(cellRenderer);
        columnModelGreenTeam.setColumnMargin(4);

        // Add the tables to a scroll pane
        JScrollPane scrollPaneRedTeam = new JScrollPane(tableRedTeam);
        JScrollPane scrollPaneGreenTeam = new JScrollPane(tableGreenTeam);
        // Add the scroll panes to a panel
        JPanel tablePanel = new JPanel();
        tablePanel.add(scrollPaneRedTeam, BorderLayout.WEST); // Red team table to the left
        tablePanel.add(scrollPaneGreenTeam, BorderLayout.EAST); // Green team table to the right

        // Create and display the menu bar for game settings
        setupMenuBar(tableModelRedTeam, tableModelGreenTeam);

        // Add the table panel to this frame
        this.setLayout(new BorderLayout());
        this.add(tablePanel, BorderLayout.CENTER);

        this.validate(); // Validate the components in this frame
    }

}
