package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;

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

    // Get all players and convert the data into the correct type
    HashMap<Integer, String> players = DB.get().getAllPlayers();
    Object[] playerMachineIds = players.keySet().toArray();
    Object[][] tableDataRedTeam = new Object[20][2];
    Object[][] tableDataGreenTeam = new Object[20][2];
    Object[] columnNames = Arrays.asList("Machine ID", "Playername").toArray();

    /*
    // Convert hashmap into 2D array
    for (int i = 0; i < players.size() && i < 20; i++)
    {
        if (i % 2 == 0)
        {
            tableDataRedTeam[i][0] = playerMachineIds[i];
            tableDataRedTeam[i][1] = players.get(playerMachineIds[i]);
        } else
        {
            tableDataGreenTeam[i - 1][0] = playerMachineIds[i];
            tableDataGreenTeam[i - 1][1] = players.get(playerMachineIds[i]);
        }

    }
    */
    
    // Create the table
    JTable tableRedTeam = new JTable(tableDataRedTeam, columnNames);
    tableRedTeam.setFillsViewportHeight(true);
    tableRedTeam.setRowSelectionAllowed(false);
    tableRedTeam.setColumnSelectionAllowed(false);
    tableRedTeam.setBackground(Color.RED);
    //tableRedTeam.putClientProperty("terminateEditOnFocusLost", true);
    JTable tableGreenTeam = new JTable(tableDataGreenTeam, columnNames);
    tableGreenTeam.setFillsViewportHeight(true);
    tableGreenTeam.setRowSelectionAllowed(false);
    tableGreenTeam.setColumnSelectionAllowed(false);
    tableGreenTeam.setBackground(Color.GREEN);
    //tableGreenTeam.putClientProperty("terminateEditOnFocusLost", true);
    /* {
        @Override
        public void editingStopped(ChangeEvent e)
        {
            TableModel model = this.getModel();
            HashMap<Integer, String> completePlayerData = new HashMap<>();
            
            for (int row = 0; row < model.getRowCount(); row++)
            {
                Object machineIdObject = model.getValueAt(row, 0);
                Object playernameObject = model.getValueAt(row, 1);

                if (machineIdObject == null || playernameObject == null) continue;
                
                Integer machineId = 0;
                String playername = playernameObject.toString();

                try {
                    machineId = Integer.valueOf(machineIdObject.toString());
                } catch (NumberFormatException exception)
                {
                    System.out.println("Invalid Machine ID: Input is not a number.");
                    model.setValueAt(null, row, 0); // Remove invalid input from table
                }
                

                completePlayerData.put(machineId, playername);
                model.setValueAt(machineId, row, 0);
                model.setValueAt(playername, row, 1);
            }

            completePlayerData.forEach((id, codename) -> DB.get().addPlayer(id, codename));

        }
        
    };*/
    JPanel tablePanel = new JPanel();
    // Add the table to the pane
    JScrollPane scrollPanelRedTeam = new JScrollPane(tableRedTeam);
    //scrollPanelRedTeam.setBackground(Color.RED);
    JScrollPane scrollPanelGreenTeam = new JScrollPane(tableGreenTeam);
    //scrollPanelGreenTeam.setBackground(Color.GREEN);
    // Add the panes to the panel
    tablePanel.add(scrollPanelRedTeam, BorderLayout.WEST);
    tablePanel.add(scrollPanelGreenTeam, BorderLayout.EAST);

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

    // Layout adjustments
    this.setLayout(new BorderLayout());
    this.add(formPanel, BorderLayout.CENTER);
    this.add(addPlayerButton, BorderLayout.SOUTH);
    this.add(tablePanel, BorderLayout.NORTH);

    this.validate();
}


}
