package src.edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Application extends JFrame { // JFrame lets us create windows
    
    // Create a basic and blank window
    public Application()
    {
        this.setTitle("Photon Laser Tag");
        this.setSize(700, 500);
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
        

        final JPanel buttonPanel = new JPanel();
        final JButton startButton = new JButton("Start");

        // Changes to the player entry screen when the button is pressed
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerEntryScreen();
            }
        });

        buttonPanel.add(startButton);

        final JPanel textPanel = new JPanel();
        final JLabel titleLabel = new JLabel("Photon Laser Tag | Welcome");

        textPanel.add(titleLabel);
        
        // Add the components to the frame
        this.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(textPanel, BorderLayout.NORTH);

        this.validate(); // Necessary when adding new components after using removeAll()
    }

    // Changes the screen to the player entry screen
    private void playerEntryScreen() {
    resetScreen();

    final JPanel formPanel = new JPanel();
    formPanel.setLayout(new BorderLayout(5, 5));

    // Player entry form components
    final JLabel nameLabel = new JLabel("Player Name:");
    final JTextField nameField = new JTextField(20);
    final JButton submitButton = new JButton("Add Player");

    // Handle player submission
    submitButton.addActionListener(e -> {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a player name.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Placeholder for adding the player to the game logic
            System.out.println("Player added: " + playerName); // Debugging output
            nameField.setText(""); // Clear the input field for the next player
        }
    });

    formPanel.add(nameLabel, BorderLayout.WEST);
    formPanel.add(nameField, BorderLayout.CENTER);
    formPanel.add(submitButton, BorderLayout.EAST);

    final JPanel titlePanel = new JPanel();
    final JLabel titleLabel = new JLabel("Photon Laser Tag | Add Players");
    titlePanel.add(titleLabel);

    // Add the components to the frame
    this.setLayout(new BorderLayout());
    this.add(titlePanel, BorderLayout.NORTH);
    this.add(formPanel, BorderLayout.CENTER);

    this.validate();
}

