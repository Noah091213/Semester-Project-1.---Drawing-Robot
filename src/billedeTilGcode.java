import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/*
*
*					  [SEMESTERPROJEKT 1. SEMESTER]
*				[GRUPPE: EMILY, EMMA, LOUISE, DOMINIK]
*						      [NOAH og FREDERIK]
*				...Mads Olsen skal også have lidt credit...
*
*				LAV EN TEGNEROBOT, SOM KAN TEGNE FRA
*							      ET BILLEDE
*
* @auther Frederik
*/

class punktAstar {
  int x, y, g, h;
  punktAstar forrige;
  
  punktAstar(int inX, int inY, int lengthFromStart, int lengthFromEnd, punktAstar last) {
    this.x = inX;
    this.y = inY;
    this.g = lengthFromStart;
    this.h = lengthFromEnd;
    this.forrige = last;
  }

  int negativeWorth() {
    return this.g + this.h;
  }

}

public class billedeTilGcode {

	// Static variables used throughout multiple methods. Most are loaded through the config file during LoadConfig()
    static String Fil, ConfFil= "config.conf";
    static String programName = "billedeTilGcode";
    static int maxXYZ[] = { 0, 0, 0 };
    static double resolution = 0.0;
    static double feed;
    static int reSharpeningLength = 10000; 
	  static String writtenFileName;
    static Properties prop = new Properties();

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
    

    static BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
      Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
      BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());

      Graphics2D g2d = outputImage.createGraphics();
      g2d.drawImage(resultingImage, 0, 0, null);
      g2d.dispose();

      return outputImage;

    }
    static final int[] dy = {0, 1, 1, 1, 0, -1, -1, -1}; //Hvilken vej vil vi søge
    static final int[] dx = {1, 1, 0, -1, -1, -1, 0, 1};  //Har gjort det muligt at tegne diagonalt
    
    //static final int[] dy = {0, 1, 0, -1}; //Hvilken vej vil vi søge
    //static final int[] dx = {1, 0, -1, 0};  //Har gjort det muligt at tegne diagonalt

    static List<Point> shortestPath(boolean[][] grid, Point start, Point end) { 
      //Bygget på https://en.wikipedia.org/wiki/A*_search_algorithm
      //A* search algorithm
      if ((!grid[start.x][start.y] || !grid[end.x][end.y]) || start.equals(end)) {
          return null; // Start eller slutpoint ikke true eller du er der allerede
      }
      //System.out.println("shortestPath active");

      int imageWidth = grid.length;
      int imageHeight = grid[0].length;
      boolean visited[][] = new boolean[imageWidth][imageHeight];
      //Finder størrelsen af mappet

      //int minLengthToEndPoint = Math.abs(end.x - start.x) + Math.abs(end.y - start.y); //https://en.wikipedia.org/wiki/Taxicab_geometry
      int minLengthToEndPoint = Math.max(Math.abs(end.x - start.x), Math.abs(end.y - start.y)); //https://en.wikipedia.org/wiki/Chebyshev_distance
      /*Math.sqrt(Math.pow((end.x - start.x), 2) + Math.pow((end.y - start.y), 2)); */


      LinkedList<punktAstar> queue = new LinkedList<>(); //Lav et dynamisk array vi kan arbejde med. Her kan vi søge efter nye punkter at gå til
      queue.add(new punktAstar(start.x, start.y, 0, minLengthToEndPoint, null)); //Tilføj startpunktet det vi står på
      visited[start.x][start.y] = true; 
      //WeightMap[start.x][start.y] = LengthToStart; //Definer længden punktet start til at være 0 væk fra start.

      while (!queue.isEmpty()) {
        punktAstar p = queue.peek(); //Tager det første punkt på listen 
        for(punktAstar P: queue) { //Finder det punkt tættest på start og kigger der først
          if(P.negativeWorth() < p.negativeWorth()) {
            p = P; //Hvis vi finder et punkt på listen der er tættere på, så tag det. 
          }
        }
        queue.remove(p); //fjerner p fra listen.
        

        if(p.x == end.x && p.y == end.y) {
          //Vi har fundet slutningen.
          //return listen med retning;
          punktAstar nuPoint = p; 
          LinkedList<Point> path = new LinkedList<>();

          while (nuPoint != null) {
            Point nu = new Point(nuPoint.x, nuPoint.y);
            path.add(nu);
            nuPoint = nuPoint.forrige;
          }
          Collections.reverse(path);
          /*System.out.println("New path: ");
          for (Point point : path) {
            System.out.println("(" + point.x + ", " + point.y + ")");
          }
          System.out.println("Path end\n\n\n");*/
          return path;

        }

        for (int i = 0; i < dx.length; i++) {
          if((p.x + dx[i]) >= 0 && (p.x + dx[i]) < imageWidth && (p.y + dy[i]) >= 0 && (p.y + dy[i]) < imageHeight && grid[p.x][p.y] && !visited[p.x + dx[i]][p.y + dy[i]]) {
            
            int neighbourMyLengthToEnd = Math.max(Math.abs(end.x - (p.x + dx[i])), Math.abs(end.y - (p.y + dy[i]))); //Finder minimumslængden fra nabopunktet til enden
            //int neighbourMyLengthToEnd = Math.abs(end.x - (p.x + dx[i])) + Math.abs(end.y - (p.y + dy[i]));
            queue.add(new punktAstar(p.x + dx[i], p.y + dy[i], p.g+1, neighbourMyLengthToEnd, p));
            visited[p.x + dx[i]][p.y + dy[i]] = true;
          }
        }
      }
      //System.out.println("shortestPath returned null");
      return null; //Ingen vej fundet
      
    }
   
  
    public static void CreateConfigFile() {		// If a config file does not exist, one has to be created and filled, this method is for that purpose
      prop.setProperty("MapSizeX"   , "<int: 3D printer max x i hele mm>");										// Set max dimension size for the printer in X
      prop.setProperty("MapSizeY"   , "<int: 3D printer max Y i hele mm>");										// Set max dimension size for the printer in Y
      prop.setProperty("MapSizeZ"   , "<int: 3D printer max Z i hele mm>");										// Set max dimension size for the printer in Z
      prop.setProperty("Feed"       , "<float: Feed, G1; i mm/min>");												// Set speed for drawn lines
      prop.setProperty("resolution" , "<float: hvor mange mm går der pr pixel>");									// Set resolution for the drawn picture, mm per pixel
      prop.setProperty("reSharpeningLength" , "<int: hvor mange mm går der før vi spidser blyanten>"); //Set reSharpeningLength

      prop.setProperty("IP"         , "<String: IP to send to via TCP>");											// Set IP to connect to with TCP for transfer
      prop.setProperty("Port"       , "<int: Port used to send through when using TCP>");							// Set port to open for TCP communication
      prop.setProperty("FileName"   , "<String: File name written to and read from when sending information>");	// Set the filename to be read when sending information
      
	  try {		// Try to save it, errors can occur, refer to next few lines
        prop.store(new FileWriter(ConfFil), "Konfigurationsfil til Gkoden");										// Saves all the previous values in the config file 
      }

      catch (FileNotFoundException e) {			// If file doesn't exist, throw this exception
        System.out.println("[ERROR]: Fil eksisterer ikke...");
      }
	  
      catch (IOException e) {					// When IO errors occur, throw this exception
        System.out.println("[ERROR]: "+e);
      }
    }

    public static void LoadConfig() {
        
        try (FileInputStream fis = new FileInputStream(ConfFil)) {
            prop.load(fis);
        } 
        catch (FileNotFoundException ex) {
            // FileNotFoundException catch is optional and can be collapsed
            System.out.println("Configfilen eksisterer ikke. Vi danner den og du fylder den ud!");
            try {
                File myObj = new File(ConfFil);
                if (myObj.createNewFile()) {
                    System.out.println("Fil skabt: " + myObj.getName());
                    System.out.println("Fyld konfigurationsfilen ud: ");
                    CreateConfigFile();
                    System.exit(0);
                } 
				else {
                    System.out.println("Fil eksistere allerede");
                    System.out.println("Der skete noget uventet");
                    System.exit(0);
                }
            } 
            catch (IOException e) {
                System.out.println("[ERROR]: Der opstod en fejl");
                System.out.println(e);
            }
        } catch (IOException ex) {
                System.out.println("[ERROR]: CRASH ON CONF READ");
        }
        
        if(prop.getProperty("MapSizeX") != null && prop.getProperty("MapSizeY") != null && prop.getProperty("MapSizeZ") != null) {

            maxXYZ[0] = Integer.parseInt(prop.getProperty("MapSizeX"));
            System.out.println("Max width loaded[X]: "+maxXYZ[0]);

            maxXYZ[1] = Integer.parseInt(prop.getProperty("MapSizeY"));
            System.out.println("Max height loaded[Y]: "+maxXYZ[1]);

            maxXYZ[2] = Integer.parseInt(prop.getProperty("MapSizeZ"));
            System.out.println("Max height loaded[Z]: "+maxXYZ[2]);

            feed = Double.parseDouble(prop.getProperty("Feed"));
            System.out.println("Feed loaded: "+feed+" mm/min");

            resolution = Double.parseDouble(prop.getProperty("resolution"));
            System.out.println("Resolution loaded: "+resolution+" mm/pixel");

            reSharpeningLength = Integer.parseInt(prop.getProperty("reSharpeningLength"));
            System.out.println("Sharpening length loaded: "+reSharpeningLength);

			      writtenFileName = prop.getProperty("FileName");
			      System.out.println("File name loaded: "+writtenFileName);
        }

        else {
          //Crash
          System.out.println("[ERROR]: Crash på configurationsfilen. Ingen data!");
          System.exit(0);
        }
    }
//-----------------------------------------------------------
//
//
//                          Main
//
//
//-----------------------------------------------------------
    public static void main(String[] args) throws FileNotFoundException {
      LoadConfig(); //Loading config file
      
      JFrame mainWindow = new GuiWindow("Image to gCode",new Dimension(800, 600));
        
      mainWindow.setVisible(true);
    }



    public static void gCodeConvert(String File) throws FileNotFoundException {
      BufferedImage image;
      try {
        EdgeDetector edge = new EdgeDetector(File); //Laver objekt og læser billedefil
        image = edge.getBufferedImage(); //Laver et billedeobjekt
        /*Graphics graphics = image.createGraphics(); 
        graphics.drawImage(image,0,0,null);*/
        ImageIO.write(image, "png", new File("checkcode.png")); //Printer det sorthvide billede ud med Edgedetection
      } catch (IOException e) {
        System.out.println("[ERROR]: "+e);
        System.exit(0);
        return;
      }


      int imageWidth = image.getWidth(), imageHeight = image.getHeight();
      double imageScale = ((double)imageHeight/(double)imageWidth);
      
      System.out.println("Image: X: "+imageWidth+" Y: "+imageHeight);
      System.out.println("Imagescale: "+imageScale);
      if(resolution != 0) {
        if(imageHeight > imageWidth) {   // Formindsker billede til grid. Hvis Y er større end x formindsker den i forhold til Y
          imageWidth =(int)((maxXYZ[0]/imageScale)/resolution);
          imageHeight =(int)(maxXYZ[1]/resolution); 
          System.out.println("Resize: X: "+imageWidth+" Y: "+imageHeight);
          image = resize(image, imageWidth, imageHeight);
        }
        else { //Hvis X er større end Y, så formindsker den i forhold til X
          imageWidth =(int)(maxXYZ[0]/resolution);
          imageHeight =(int)((imageScale*maxXYZ[0])/resolution); 
          System.out.println("Resize: X: "+imageWidth+" Y: "+imageHeight);
          image = resize(image, imageWidth, imageHeight);
        }
      }
      boolean[][] Billede = new boolean[imageWidth][imageHeight]; //Billede
      boolean[][] hasTaken = new boolean[imageWidth][imageHeight]; //Bruges til at tjekke om jeg har været på et bestemt sted

      //for(int y=(imageHeight-1); y > 0; y--) {
      for(int y=0; y < imageHeight; y++) {
        for(int x=0; x < imageWidth; x++) {
            int rgb = image.getRGB((x), (y));//Billedet er i sort/hvid)
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            int grayscale = (int)(0.2989 * red + 0.5870 * green + 0.1140 * blue); //https://www.mathworks.com/help/matlab/ref/im2gray.html
            Billede[x][((imageHeight-1)-y)] = grayscale<200;
        }
        //System.out.println("loading linje "+(y+1)+"/"+((int)imageHeight)+" ...");
      }
      System.out.println("Billede indlæst...");

      //Hvis Billede[x][y] < 200 ignore ellers output Gkode
      String gkode = "";
      int lastPos[] = {0, 0};
      int NextPos[] = {0, 0};

      gkode += "M101\n"; //Resharpening the pencil
      gkode += "G0 X0.0 Y0.0 Z"+maxXYZ[2]+" F"+(int)feed+"\n"; //Automation studio vil have det som en DINT. For at spare tid, konvertere jeg den bare her.

      double runLength = 0.00;
      double lastSharpening = 0.00;
      double lastDirectionChangeAngle = 0.00;

      while(true){

        //Find det tætteste punkt
        double shortestDistance = (Math.sqrt(Math.pow(imageWidth, 2)+Math.pow(imageHeight, 2))); //Den længste distance der kan være, er fra hjørne til hjørne
        for(int y=0; y < imageHeight; y++) {
          for(int x=0; x < imageWidth; x++) {
            
            if(Billede[x][y] && !hasTaken[x][y]) {
              double Distance = Math.sqrt(Math.pow((lastPos[0]-x), 2)+Math.pow((lastPos[1]-y), 2));
              
			        if(Distance < shortestDistance) {
                shortestDistance = Distance;
                NextPos[0] = x;
                NextPos[1] = y;
              }
            }
          }
        }

        if(shortestDistance > ((Math.sqrt(Math.pow(imageWidth, 2)+Math.pow(imageHeight, 2))) - 0.01) ) {  //Hvis vi når at loope igennem hele mappet uden at finde nye points, så breaker vi loopet
          break; 
        }
        int x = NextPos[0];
        int y = NextPos[1];
        
        
        if(Billede[x][y] && !hasTaken[x][y]) { 
          //Et sted hvor der er true i billede men false i hastaken.
          //Finder et nyt sted hvor der ikke tegnet

          LinkedList<Point> queue = new LinkedList<>();    
          queue.add(new Point(x, y));    
  
          while(!queue.isEmpty()) { //Søger efter steder hvor billedet er true og hasTaken er false ud fra det sted jeg er startet
            Point p = queue.getFirst();
            for(Point P : queue) { //sort the points to find the nearest
              double p1 = Math.sqrt(Math.pow((p.x - lastPos[0]), 2) + Math.pow((p.y - lastPos[1]), 2));
              double p2 = Math.sqrt(Math.pow((P.x - lastPos[0]), 2) + Math.pow((P.y - lastPos[1]), 2));
              if(p2 < p1) {
                p = P;
              }
            }
            queue.remove(p);

            
            if(p.x < 0 || p.x >= imageWidth || p.y < 0 || p.y >= imageHeight || hasTaken[p.x][p.y] || !Billede[p.x][p.y]) { //Errorcatching af limits og hvis vi kører over det samme punkt
              if(p.x < 0 || p.x >= imageWidth || p.y < 0 || p.y >= imageHeight) {
                System.out.println("ERROR p.x="+p.x+" p.y="+p.y);
              }
              continue;
            }
  
            hasTaken[p.x][p.y] = true; 

            Point start = new Point(lastPos[0], lastPos[1]);
            Point end = new Point(p.x, p.y);
            Point lastPoint = new Point(lastPos[0], lastPos[1]); //For length calculation
            List<Point> path = shortestPath(Billede, start, end);

            double angle = 0.0; //For angle calculations. To make the size of the file smaller

            
            if (path != null) { //We can reach the point without lifting the pencil
              
              boolean doPrint = true; //For angle calculations. To make the size of the file smaller

              for (Point pointOnPath : path) {


                //if(lastPoint.x != pointOnPath.x && lastPoint.y != pointOnPath.y ) { //If the point is not the point we started at
                if(!lastPoint.equals(pointOnPath)) { //If the point is not the point we started at
                  
                  double radius = Math.sqrt(Math.pow((pointOnPath.x)-(lastPoint.x), 2)+Math.pow((pointOnPath.y)-(lastPoint.y), 2)); //Pythagoras imellem points
                  runLength += (radius*resolution); //Sætter en reel længde på

                  double deltaX = pointOnPath.x - lastPoint.x;
                  double deltaY = pointOnPath.y - lastPoint.y;
                  

                  if(deltaY >= 0) {
                    angle = Math.asin(deltaX/radius);
                  }
                  else {
                    angle = (2*Math.PI-Math.asin(deltaX/radius));
                  }

                  if(Math.abs(angle-lastDirectionChangeAngle) > 0.01) { //Comparing the angles to see if they are the same. We change direction now
                    //print the point
                    
                    doPrint = true;
                  } else {
                    //dont print the point
                    doPrint = false;
                  }
                  lastDirectionChangeAngle = angle;
                  
                  
                }
                else {
                  doPrint = false;
                } 
                //if(doPrint && (lastPoint.x != pointOnPath.x && lastPoint.y != pointOnPath.y)) {
                if(doPrint) {
                  //gkode += "G1 X"+pointOnPath.x*resolution+" Y"+pointOnPath.y*resolution+" ANG"+angle+" lastANG"+lastDirectionChangeAngle+"\n"; //Debug
                  gkode += "G1 X"+pointOnPath.x*resolution+" Y"+pointOnPath.y*resolution+"\n";
                }
                hasTaken[pointOnPath.x][pointOnPath.y] = true; 
                lastPoint = pointOnPath;
              }
            } else { //We cannot reach the point without lifting the pencil

              if((runLength - lastSharpening) > (double)reSharpeningLength) { //Checks if we must resharpening the pencil
                gkode += "G0 Z"+maxXYZ[2]+" M101\n"; //Add the code for resharpening the pencil
                lastSharpening = runLength; //Stores the last location we resharpened the pencil
              }
              else {
                gkode += "G0 Z"+maxXYZ[2]+"\n"; //If not we go to the new point and continue
              }
              gkode += "G0 X"+p.x*resolution+" Y"+p.y*resolution+"\n";
              gkode += "G1 Z0.0\n";             
            }
            

            for (int i = 0; i < dx.length; i++) {

              if((p.x + dx[i]) >= 0 && (p.x + dx[i]) < imageWidth && (p.y + dy[i]) >= 0 && (p.y + dy[i]) < imageHeight && !hasTaken[p.x][p.y] && Billede[p.x][p.y]) {
                queue.add(new Point(p.x + dx[i], p.y + dy[i]));
              }
            }
            lastPos[0] = p.x;
            lastPos[1] = p.y;
  
          }
          
        }
      }
      gkode += "M101\n"; //Sharpen pencil at the end.   
      
      //
      //  [GCode is now done]
      //
      //  
      // 
      // --------------------------

      //
      //  Filewriter and sending through TCP
      //

      try (FileWriter writer = new FileWriter(writtenFileName)) {
        System.out.println(writtenFileName + " was succesfully created/updated");
        writer.write(gkode);
        GuiWindow.setVar(true, 1);
      } 
	    catch (IOException e) {
        e.printStackTrace();
      }

      
    }
}

