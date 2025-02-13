package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.net.URL;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

public class Application extends JFrame { // JFrame lets us create windows
    
    // Create a basic and blank window
    public Application()
    {
        URL logoUrl = getClass().getClassLoader().getResource("edu/uark/team10/assets/logo.png");
        ImageIcon logoImage = new ImageIcon(logoUrl);
        
        this.setTitle("Photon Laser Tag");
        this.setSize(logoImage.getIconWidth(), logoImage.getIconHeight());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        // Sets the screen to splash screen on startup
        splashScreen();
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

        // Add a listener to the panel
        splashPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) // Changes to the player entry screen when the screen is clicked
            {
                playerEntryScreen();
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
        
        // Create the red team table
        JTable tableRedTeam = new JTable(new PlayerEntryTableModel()); // Include the custom table model
        tableRedTeam.setModel(new PlayerEntryTableModel());
        tableRedTeam.setFillsViewportHeight(true);
        tableRedTeam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableRedTeam.setRowSelectionAllowed(false);
        tableRedTeam.setColumnSelectionAllowed(false);
        tableRedTeam.setBackground(new Color(122, 0, 0));

        // Create the green team table
        JTable tableGreenTeam = new JTable(new PlayerEntryTableModel()); // Include the custom table model
        tableGreenTeam.setModel(new PlayerEntryTableModel());
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
        
        columnModelRedTeam.getColumn(0).setCellRenderer(cellRenderer);
        columnModelRedTeam.getColumn(1).setCellRenderer(cellRenderer);
        columnModelRedTeam.getColumn(2).setCellRenderer(cellRenderer);
        columnModelRedTeam.setColumnMargin(4);
        
        // Set cell renderer for green team
        TableColumnModel columnModelGreenTeam = tableGreenTeam.getColumnModel();
        columnModelGreenTeam.getColumn(0).setPreferredWidth(20);
        columnModelGreenTeam.getColumn(1).setPreferredWidth(100);
        columnModelGreenTeam.getColumn(2).setPreferredWidth(200);

        columnModelGreenTeam.getColumn(0).setCellRenderer(cellRenderer);
        columnModelGreenTeam.getColumn(1).setCellRenderer(cellRenderer);
        columnModelGreenTeam.getColumn(2).setCellRenderer(cellRenderer);
        columnModelGreenTeam.setColumnMargin(4);

        // Add the table to the pane
        JScrollPane scrollPanelRedTeam = new JScrollPane(tableRedTeam);
        //scrollPanelRedTeam.setSize(tableRedTeam.getWidth(), tableRedTeam.getHeight());

        JScrollPane scrollPanelGreenTeam = new JScrollPane(tableGreenTeam);
        //scrollPanelGreenTeam.setSize(tableGreenTeam.getWidth(), tableGreenTeam.getHeight());

        // Add the panes to a panel
        JPanel tablePanel = new JPanel();
        tablePanel.add(scrollPanelRedTeam, BorderLayout.WEST);
        tablePanel.add(scrollPanelGreenTeam, BorderLayout.EAST);

        /*
        // Create panel for player entry
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout(10, 10));

        JLabel nameLabel = new JLabel("Enter Player Name:");
        JTextField nameField = new JTextField(1);

        formPanel.add(nameLabel, BorderLayout.NORTH);
        formPanel.add(nameField, BorderLayout.CENTER);

        // Add Player Button
        JButton addPlayerButton = new JButton("Add Player");

        addPlayerButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            int machineId = new Random().nextInt(256) + 1; // TODO get machine id from user

            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a player name.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add player to database
            DB db = DB.get();
            db.addPlayer(machineId, playerName);

            // Simulate player entry
            System.out.println("Player Added: " + playerName);
            System.out.println("Machine ID: " + machineId);
            splashScreen();
        });
        */

        // Layout adjustments
        this.setLayout(new BorderLayout());
        //this.add(formPanel, BorderLayout.CENTER);
        //this.add(addPlayerButton, BorderLayout.SOUTH);
        this.add(tablePanel, BorderLayout.CENTER);

        this.validate();
    }

}
