import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

public class GuiWindow extends JFrame {
    
    public String selectedFile  = "";           // Declared as public and outside the panel class to allow actionlistener interaction
    public static boolean isGCodeDone  = false; // Bool to avoid sending before gCode is made
    public static boolean isGCodeSent  = false; // Bool to avoid sending commands before gCode is sent


    public static void setVar(boolean newState, int varToChange){   // Due to most methods from main being static while the GUI is not, a method to convert bools is necessary
        if (varToChange == 1){              // 1 is for isGCodeDone
            isGCodeDone = newState;         // To avoid sending something that is not yet generated, a bool is made to prevent this
            System.out.println(isGCodeDone);// Prints new state for debugging
        } 
        else if (varToChange == 2) {        // 2 is for isGCodeSent
            isGCodeSent = newState;         // To avoid sending commands to control the PLC before sending the gCode, a bool is made to prevent this
            System.out.println(isGCodeSent);// Prints new state for debugging
        }
    }

    public GuiWindow(String title, Dimension dimension){      // Creates the main JFrame and its parameters
        this.setSize(dimension);
        this.setTitle(title);
        this.setResizable(false);                   // Disables rezizing the GUI, this is to avoid wasted space near buttons or accidentaly hidding some
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE); // Sets the GUI to close when clicking on the exit X
        
        createPanel();
    }

    public void createPanel(){
        // Variables used througout the GUI, mostly colors so they stay consistent
        String imageSelect      = " File: ";
        Color backgroundColor   = new Color(84, 137, 222);
        Color borderColor       = new Color(0, 0, 0);
        Color whiteColor        = new Color(255,255,255);
        Color redColor          = new Color(201, 58, 106);
        Color greenColor        = new Color(110,235,52);
        Font  generalFont       = new Font ("Tahome", Font.BOLD, 24);
        

        // Creates the panel needed for our GUI
        JPanel mainPanel            = new JPanel();


        // Creates the items to add to the various panels
        JButton sendButton  = new JButton("Send the gCode");
        JButton makeGCode   = new JButton("Generate gCode");
        JButton imagePicker = new JButton("Select your image");
        JLabel  imageName   = new JLabel (imageSelect);
        JButton sendStart   = new JButton("Start command");
        JButton sendStop    = new JButton("Stop  command");
        JButton sendSingle  = new JButton("Single command");
        JButton sendClear   = new JButton("Clear command");
        JButton helpButton  = new JButton("Help?");


        // Sets the parameters for the main panel everything else is attached onto
        mainPanel.setLayout(null);
        mainPanel.setBounds(0,0,1600, 1000);
        mainPanel.setBorder(null);
        mainPanel.setBackground(backgroundColor);


        // Button for help dialog
        helpButton.setBounds(250,50, 300, 50);
        helpButton.setBorder(BorderFactory.createLineBorder(borderColor));
        helpButton.setBackground(whiteColor);
        helpButton.setFont(generalFont);
        helpButton.addActionListener(e -> {         // When the help button is clicked, it executes the following:
            System.out.println("Opening help");

            JDialog helpDialog = new JDialog();         // Dialog box is opened in seperate window
            helpDialog.setSize(500,400);   // Size is declared

            JTextArea helpInfo = new JTextArea(         // Text is added in the dialog box to explain the various buttons and commands
                """ 
                To start, select an image to print using \"Select your image\"   

                After choosing an image, click \"Generate gCode\".  
                This will turn the image into something the printer can understand.  

                Lastly click \"send gCode\" to transfer   

                The buttons will turn green, when they can be used   
                
                Commands can only run, when gCode has been sent to the PLC first
                
                Command options: 
                  
                Start  - starts the drawing  
                Stop   - Stops the current drawing 
                Clear  - Clears the gCode from the PLC  
                Single - Stops the robot at every point, 
                            user must send start at every point
                """
                                
                );
            helpInfo.setFont(new Font("Tahome", Font.PLAIN, 15));
            
            helpDialog.add(helpInfo);
            helpDialog.setAlwaysOnTop(true);
            helpDialog.setResizable(false);
            helpDialog.setLocation(400,400);;
            helpDialog.setVisible(true);
        });


        // Button to Open file path to select an image
        imagePicker.setBounds(50,250, 300, 50);
        imagePicker.setBorder(BorderFactory.createLineBorder(borderColor));
        imagePicker.setFont(generalFont);
        imagePicker.addActionListener(e -> {    // Actionlistener executes the following when button is pressed
            
            JFileChooser selecter = new JFileChooser();                                 // Opens a filechooser which is similar to the default windows path window
            selecter.setFileSelectionMode(JFileChooser.FILES_ONLY);                     // Disalows the filechooser to accept folders as the end file so the root folder is not accidentally chosen instead of the image
            if (selecter.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){   // When the user clicks accept and a file is selected, go here
                
                String chosenImage = selecter.getSelectedFile().getAbsolutePath();      // The path for the chosen file is stored
                selectedFile = chosenImage;                                             // Copies the path into the public variable, so the program can use the path outside this actionlistener
                System.out.println(chosenImage);                                        // Debug print
                imageName.setFont(new Font("Tahome", Font.BOLD, 14));
                imageName.setText(" File: "+ chosenImage);                              // Sets the image label to the path of the chosen image
                makeGCode.setBackground(greenColor);                                    // Sets the background for the button to create the gCode to green, to symbolise it being usable now
            }
        });


        // Label showing the image path
        imageName.setBounds(50,150, 300, 50);
        imageName.setBorder(BorderFactory.createLineBorder(borderColor));
        imageName.setBackground(whiteColor);
        imageName.setOpaque(true);
        imageName.setFont(generalFont);


        // Button to create gCode
        makeGCode.setBounds(450, 150, 300,50);
        makeGCode.setBorder(BorderFactory.createLineBorder(borderColor));
        makeGCode.setFont(generalFont);
        makeGCode.setBackground(redColor);
        makeGCode.addActionListener(e -> {  // Actionlistener executes the following when button is pressed
            
            if (selectedFile != "") {       // If the file has been selected, the user can use the the button, otherwise a debug print is made
                try {
                    billedeTilGcode.gCodeConvert(selectedFile);   // Calls the method to create gCode using the previously selected file
                    System.out.println("Making gCode!");        // debug print
                    sendButton.setBackground(greenColor);         // Sets the background for the button to send the gCode to green, to symbolise it being usable now
                } catch (FileNotFoundException r) {System.out.println("File not found");}
            } else {
                System.out.println("Select a file before printing");
            }
        }); 
        
        // Button to send the gCode
        sendButton.setBounds(450, 250, 300,50);
        sendButton.setBorder(BorderFactory.createLineBorder(borderColor));
        sendButton.setBackground(redColor);
        sendButton.setFont(generalFont);
        sendButton.addActionListener(e -> {         // Actionlistener executes the following when button is pressed
            
            if (isGCodeDone == true) {
                System.out.println("Send gCode!");// Debug print
                PLCClientConnection.sendFileToPLC();// Calls the method to send gCode to the PLC

                sendStart .setBackground(greenColor); // Sets command button colors to green, symbolizing them being usable now
                sendStop  .setBackground(greenColor);
                sendClear .setBackground(greenColor);
                sendSingle.setBackground(greenColor);
            } else {
                System.out.println("Need to generate gCode first!");
            }
        });  

        // Button for the "Start" command
        sendStart.setBounds(50, 350, 300,50);
        sendStart.setBorder(BorderFactory.createLineBorder(borderColor));
        sendStart.setBackground(redColor);
        sendStart.setFont(generalFont);
        sendStart.addActionListener(e -> {
            if (isGCodeSent == true){                           // Check to see if the gCode has been sent first, to avoid breaking the PLC side
                System.out.println("Send Start!");            // Debug print
                PLCClientConnection.sendSingleCommand("START"); // Calls the method to send a command
            } else {
                System.out.println("Send gCode before sending commands");
            } 
        });  

        // Button for the "Stop" command
        sendStop.setBounds(50, 450, 300,50);
        sendStop.setBorder(BorderFactory.createLineBorder(borderColor));
        sendStop.setBackground(redColor);
        sendStop.setFont(generalFont);
        sendStop.addActionListener(e -> {
            if (isGCodeSent == true){                          // Check to see if the gCode has been sent first, to avoid breaking the PLC side
                System.out.println("Send Stop!");            // Debug print
                PLCClientConnection.sendSingleCommand("STOP"); // Calls the method to send a command
            } else {
                System.out.println("Send gCode before sending commands");
            }
        });  

        // Button for the "Single Line" command
        sendSingle.setBounds(450, 350, 300,50);
        sendSingle.setBorder(BorderFactory.createLineBorder(borderColor));
        sendSingle.setBackground(redColor);
        sendSingle.setFont(generalFont);
        sendSingle.addActionListener(e -> {
            if (isGCodeSent == true){                                 // Check to see if the gCode has been sent first, to avoid breaking the PLC side
                System.out.println("Send Single!");                 // Debug print
                PLCClientConnection.sendSingleCommand("SINGLEBLOCK"); // Calls the method to send a command
            } else {
                System.out.println("Send gCode before sending commands");
            }
        });  

        // Button for the "Clear" command
        sendClear.setBounds(450, 450, 300,50);
        sendClear.setBorder(BorderFactory.createLineBorder(borderColor));
        sendClear.setBackground(redColor);
        sendClear.setFont(generalFont);
        sendClear.addActionListener(e -> {
            if (isGCodeSent == true){                           // Check to see if the gCode has been sent first, to avoid breaking the PLC side
                System.out.println("Send Clear!");            // Debug print
                PLCClientConnection.sendSingleCommand("CLEAR"); // Calls the method to send a command
                isGCodeSent = false;

                // Sets background colors of the command buttons to red again, symbolizing that they have been deactivated until gCode is sent again
                sendStart .setBackground(redColor);
                sendStop  .setBackground(redColor);
                sendClear .setBackground(redColor);
                sendSingle.setBackground(redColor);
            } else {
                System.out.println("Send gCode before sending commands");
            }
        });  

        // Adds the various buttons and labels to the main panel
        mainPanel.add(helpButton);
        mainPanel.add(makeGCode);
        mainPanel.add(sendButton);
        mainPanel.add(imageName);
        mainPanel.add(imagePicker);
        mainPanel.add(sendStart);
        mainPanel.add(sendStop);
        mainPanel.add(sendSingle);
        mainPanel.add(sendClear);

        // Adds the main panel to the Frame thus showing it in the program
        add(mainPanel);
    }

}

