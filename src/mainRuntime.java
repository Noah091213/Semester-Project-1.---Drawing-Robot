import javax.swing.JFrame;
import java.awt.*;


public class mainRuntime {
    public static void main(String[] args) {    // Main runtime, run this file to run the program
      billedeTilGcode.LoadConfig();             //Loading config file
      
      JFrame mainWindow = new GuiWindow("Image to gCode",new Dimension(800, 600));  // Creates the JFrame for the GUI, this calls on the rest of the GUI code, utilizing the paramaters sent with the call
        
      mainWindow.setVisible(true);  // Sets the GUI to be visible
    }
}
