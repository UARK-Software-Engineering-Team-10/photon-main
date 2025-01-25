import javax.swing.JFrame;

public class Application extends JFrame { // JFrame lets us create windows
    
    // Create a basic and blank window
    public Application()
    {
        this.setTitle("Photon Laser Tag");
        this.setSize(1920, 1080);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        // Sets the screen to splash screen on startup
        splashScreen();
    }

    // Changes the screen to the splash screen
    private void splashScreen()
    {
        // TODO
    }

    // Changes the screen to the player entry screen
    private void playerEntryScreen()
    {
        // TODO
    }

}
