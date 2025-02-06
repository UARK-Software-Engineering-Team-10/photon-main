package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Application extends JFrame { // JFrame lets us create windows
    
    // Create a basic and blank window
    public Application()
    {
        this.setTitle("Photon Laser Tag");
        this.setSize(1151, 733);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);

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

        splashLabel.setIcon(new ImageIcon("../src/edu/uark/team10/assets/logo.png")); // TODO Add assets to jar
        splashPanel.add(splashLabel);

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

    // Create panel for player entry
    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BorderLayout(10, 10));

    JLabel nameLabel = new JLabel("Enter Player Name:");
    JTextField nameField = new JTextField(20);

    formPanel.add(nameLabel, BorderLayout.NORTH);
    formPanel.add(nameField, BorderLayout.CENTER);

    // Add Player Button
    JButton addPlayerButton = new JButton("Add Player");

    addPlayerButton.addActionListener(e -> {
        String playerName = nameField.getText().trim();

        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a player name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simulate player entry
        System.out.println("Player Added: " + playerName);
        splashScreen(); // Navigate back to splash screen after successful entry
    });

    // Layout adjustments
    this.setLayout(new BorderLayout());
    this.add(formPanel, BorderLayout.CENTER);
    this.add(addPlayerButton, BorderLayout.SOUTH);

    this.validate();
}


}
