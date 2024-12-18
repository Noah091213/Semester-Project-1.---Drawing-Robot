/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class PLCClientConnection {  

    enum TCPState {         // Enum used for send recieve switch case
        sending, recieving
    }

    // The main socket and in and out streams are declared outside methods to be used by all methods
    static Socket mainSocket;   
    static PrintWriter out;
    static BufferedReader in;


    public static void connectToPLC(){     // Method to send the gCode file previously generated, to the PLC via TCP connection

        // Predefined IP and Port number for connection with PLC, can be defined in config file
        String plcHostIP    = billedeTilGcode.prop.getProperty("IP");
        int    plcPort      = Integer.parseInt(billedeTilGcode.prop.getProperty("Port"));
        

        try {
            mainSocket  = new Socket( plcHostIP, plcPort );                                         // The main socket for TCP connection is declared and opened, to allow for communincation
            out         = new PrintWriter(mainSocket.getOutputStream(), true);            // PrintWriter is used to send information to the PLC
            in          = new BufferedReader(new InputStreamReader( mainSocket.getInputStream() )); // BufferedReader declared to open the in stream. This is the information recieved from the PLC  
            
            System.out.println("Establishing connection!");
        } 
        catch (UnknownHostException e) {      // Catch for errors regarding either IP or Port having issues
            System.err.println("Don't know about host " + plcHostIP ); 
        } 
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + plcHostIP );  
        } 
    }


    public static void sendFileToPLC() {    // MEthod for sending the gCode file to the PLC via TCP
        connectToPLC();                     // First the connection needs to be established

        int lineCount       = 1;                                                // Line count is used for debugging    
        String gCodeFile    = billedeTilGcode.prop.getProperty("FileName"); // Filename is taken from config file
        
        TCPState tcpState = TCPState.sending;   // Defaults the TCP switch case to send first

        try {
            
            Scanner fileReader = new Scanner(new File( gCodeFile ));    // Opens the file declared as "gCodeFile" and to be read through line for line

            while(fileReader.hasNextLine()){    // While loop to read through the whole gCode file
                    
                switch (tcpState) {     // Starts the switch case, this is used to both send information to the PLC and recieve an OK check
                    
                    case sending:                                                                           // Case for sending information to the PLC
                        String currentLine = fileReader.nextLine();                                         // Sets currentLine string to the current line of the gCode file, to be able to send it to the PLC
                        out.println(currentLine);                                                           // Prints the currently read line to the stream sending data      
                        System.out.println( lineCount + ". line: \"" + currentLine +"\" is sent to PLC");   // Debugging print, writes the current line and the information being sent to the PLC
                        lineCount++;                                                                        // Adds 1 to linecount, only used for debugging
                        tcpState = TCPState.recieving;                                                      // Switches case to check if the current message was recieved, before sending the next one
                        System.out.println("Message sent, switching to recieving");                       // Debugging print

                    case recieving:                                                         // Case for recieving a confirmation from the PLC
                        System.out.println("Waiting for response...");                    // Debugging print
                        String recievedMsg = in.readLine();                                 // Saves the last info from the recieved "in" stream
                        System.out.println( recievedMsg );                                  // Debugging print
                        
                        if ("ok".equalsIgnoreCase( recievedMsg )){                          // Checks if the recieved message is "ok" if yes proceed, otherwise check again
                            tcpState = TCPState.sending;                                    // Switch the case to send the next message
                            System.out.println("OK recieved, switching to sending \n");   // Debugging print
                        }
                        
                }
            }
        
            mainSocket.close();                         // Closes the main socket thus severing the TCP connection

            fileReader.close();                         // Closes the file scanner as it is no longer needed
        } 
        catch (IOException e) { e.printStackTrace(); }
        
        GuiWindow.setVar(true,2);  // Sets isGCodeSent to true, to confirm that gCode has been sent. This is used to avoid sending commands without gCode
    }

    public static void sendSingleCommand(String commandToSend){ // Method for sending single word commands to PLC, we are using a string to allow different commands to be sent

        connectToPLC(); // Establishes connection to the PLC

        out.println("\n"+ commandToSend +"\n"); // Send the string command to the PLC
        
        try {                       // Attempts to close the main socket thus severing the TCP connection, if the connection is closed, and error is thrown and the program continues
            mainSocket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}   