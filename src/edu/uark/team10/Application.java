package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

public class Application extends JFrame { // JFrame lets us create windows

    private UDPServer server = null;
    private Game game = null;
    
    // Create a basic and blank window
    public Application()
    {
        URL logoUrl = getClass().getClassLoader().getResource("edu/uark/team10/assets/logo.png");
        ImageIcon logoImage = new ImageIcon(logoUrl);
        
        this.setTitle("Photon Laser Tag");
        this.setSize(logoImage.getIconWidth(), logoImage.getIconHeight());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);

        // Sets the screen to splash screen on startup
        splashScreen();
    }

    private void setupMenuBar(PlayerEntryTableModel tableModelRedTeam, PlayerEntryTableModel tableModelGreenTeam) {
        // Setup the menu bar for Start/settings
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenu settingsMenu = new JMenu("Settings");
        
        JMenuItem startGameItem = new JMenuItem("Start Game");
        startGameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Game started!", "Game Status", JOptionPane.INFORMATION_MESSAGE);

                for (Object[] playerData : tableModelRedTeam.getRowData(Game.RED_TEAM_NUMBER))
                {
                    game.addPlayer(playerData);
                }

                for (Object[] playerData : tableModelGreenTeam.getRowData(Game.GREEN_TEAM_NUMBER))
                {
                    game.addPlayer(playerData);
                }

                game.start(server); // Start the game
            }
        });

        JMenuItem clearEntriesItem = new JMenuItem("Clear Entries");
        clearEntriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "Cleared player entries.", "Entries", JOptionPane.INFORMATION_MESSAGE);

            tableModelRedTeam.clear();
            tableModelGreenTeam.clear();

        }});

        JMenuItem changeIPNetwork = new JMenuItem("Change IP/Network");
        changeIPNetwork.addActionListener(new ActionListener() {    // Request user input for IP/Network
            @Override
            public void actionPerformed(ActionEvent e) {
            String newIpAddress = JOptionPane.showInputDialog(null, "Enter new IP Address:", "Change IP/Network", JOptionPane.QUESTION_MESSAGE);
            newIpAddress = newIpAddress.replaceAll("[^0-9.]", ""); // Only allow numbers and periods for ip addresses

            if (newIpAddress != null && !newIpAddress.isEmpty()) { // Check if the input is not empty
                UDPServer.networkAddress = newIpAddress;
                JOptionPane.showMessageDialog(null, "IP Address changed to: " + newIpAddress, "IP/Network Status", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "IP Address cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
            }
        });

        JMenuItem enableTestingMode = new JMenuItem("Enable Testing Mode");
        enableTestingMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "Testing mode enabled.", "Testing Mode", JOptionPane.INFORMATION_MESSAGE);
            Game.isTestingMode = true;
        }});

        gameMenu.add(startGameItem);
        gameMenu.add(clearEntriesItem);
        settingsMenu.add(changeIPNetwork);
        settingsMenu.add(enableTestingMode);
        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        this.setJMenuBar(menuBar);
        
    }

    // Changes the screen to the splash screen
    private void splashScreen()
    {
        this.getContentPane().removeAll(); // Removes all components (buttons, labels, ..)
        this.revalidate(); // Necessary when adding new components after using removeAll()
        this.repaint();

        final JPanel splashPanel = new JPanel();
        final JLabel splashLabel = new JLabel();

        // Load the logo image
        URL logoUrl = getClass().getClassLoader().getResource("edu/uark/team10/assets/logo.png");
        ImageIcon logoImage = new ImageIcon(logoUrl);
        splashLabel.setIcon(logoImage); // Set the width and height to match the image
        splashPanel.add(splashLabel);

        // A Future that completes itself after 3 seconds
        // OR completes when the splash screen is clicked
        CompletableFuture<Void> splashScreenFuture = new CompletableFuture<Void>().completeOnTimeout(null, 3L, TimeUnit.SECONDS);
        splashScreenFuture.whenComplete((none, exception) -> {
            if (exception != null)
            {
                exception.printStackTrace();
            }

            // Show player entry screen
            playerEntryScreen();

        });

        // Add a listener to the panel
        splashPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                // Changes to the player entry screen when the screen is clicked
                // if the future is not already completed
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
        
        // Add the panel to the frame
        this.setLayout(new BorderLayout());
        this.add(splashPanel, BorderLayout.CENTER);

        this.validate(); // Necessary when adding new components after using removeAll()
    }

    // Changes the screen to the player entry screen
    private void playerEntryScreen() {
        this.getContentPane().removeAll();
        this.revalidate();
        this.repaint();

        this.game = new Game();
        this.server = new UDPServer(game);
        
        // Create the red team table
        PlayerEntryTableModel tableModelRedTeam = new PlayerEntryTableModel(this.game, this.server);
        JTable tableRedTeam = new JTable(tableModelRedTeam); // Include the custom table model
        tableRedTeam.getTableHeader().setReorderingAllowed(false);
        tableRedTeam.getTableHeader().setResizingAllowed(false);
        tableRedTeam.setFillsViewportHeight(true);
        tableRedTeam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableRedTeam.setRowSelectionAllowed(false);
        tableRedTeam.setColumnSelectionAllowed(false);
        tableRedTeam.setBackground(new Color(122, 0, 0));

        // Create the green team table
        PlayerEntryTableModel tableModelGreenTeam = new PlayerEntryTableModel(this.game, this.server);
        JTable tableGreenTeam = new JTable(tableModelGreenTeam); // Include the custom table model
        tableGreenTeam.getTableHeader().setReorderingAllowed(false);
        tableGreenTeam.getTableHeader().setResizingAllowed(false);
        tableGreenTeam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableGreenTeam.setFillsViewportHeight(true);
        tableGreenTeam.setRowSelectionAllowed(false);
        tableGreenTeam.setColumnSelectionAllowed(false);
        tableGreenTeam.setBackground(new Color(0, 122, 0));

        // Custom cell renderer
        PlayerEntryTableCellRenderer cellRenderer = new PlayerEntryTableCellRenderer();

        // Set cell renderer for red team
        TableColumnModel columnModelRedTeam = tableRedTeam.getColumnModel();
        columnModelRedTeam.getColumn(0).setPreferredWidth(20);
        columnModelRedTeam.getColumn(1).setPreferredWidth(100);
        columnModelRedTeam.getColumn(2).setPreferredWidth(200);
        columnModelRedTeam.removeColumn(columnModelRedTeam.getColumn(3));
        
        columnModelRedTeam.getColumn(0).setCellRenderer(cellRenderer);
        columnModelRedTeam.getColumn(1).setCellRenderer(cellRenderer);
        columnModelRedTeam.getColumn(2).setCellRenderer(cellRenderer);
        columnModelRedTeam.setColumnMargin(4);
        
        // Set cell renderer for green team
        TableColumnModel columnModelGreenTeam = tableGreenTeam.getColumnModel();
        columnModelGreenTeam.getColumn(0).setPreferredWidth(20);
        columnModelGreenTeam.getColumn(1).setPreferredWidth(100);
        columnModelGreenTeam.getColumn(2).setPreferredWidth(200);
        columnModelGreenTeam.removeColumn(columnModelGreenTeam.getColumn(3));

        columnModelGreenTeam.getColumn(0).setCellRenderer(cellRenderer);
        columnModelGreenTeam.getColumn(1).setCellRenderer(cellRenderer);
        columnModelGreenTeam.getColumn(2).setCellRenderer(cellRenderer);
        columnModelGreenTeam.setColumnMargin(4);

        // Add the table to the pane
        JScrollPane scrollPanelRedTeam = new JScrollPane(tableRedTeam);
        JScrollPane scrollPanelGreenTeam = new JScrollPane(tableGreenTeam);
        // Add the panes to a panel
        JPanel tablePanel = new JPanel();
        tablePanel.add(scrollPanelRedTeam, BorderLayout.WEST);
        tablePanel.add(scrollPanelGreenTeam, BorderLayout.EAST);

        setupMenuBar(tableModelRedTeam, tableModelGreenTeam);

        // Layout adjustments
        this.setLayout(new BorderLayout());
        this.add(tablePanel, BorderLayout.CENTER);

        this.validate();
    }

}
