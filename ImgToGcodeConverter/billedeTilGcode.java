
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import javax.imageio.ImageIO;


//package edgedetect;
/*import edgedetect.*;*/

/*import edgedetect.Picture;
import edgedetect.Luminance;
import edgedetect.EdgeDetector;*/

public class billedeTilGcode {
    static String Fil, ConfFil= "config.conf";
    static String programName = "billedeTilGcode";
    static int maxXYZ[] = { 0, 0, 0 };
    static double zInterval = 50.0, minZ = 0.00, rapidHeight = 100.00, resolution = 0.0;
    static double rapidFeed, feed; 
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

    

    public static BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
      Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
      BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());

      Graphics2D g2d = outputImage.createGraphics();
      g2d.drawImage(resultingImage, 0, 0, null);
      g2d.dispose();

      return outputImage;
   }
  
    public static void CreateConfigFile() {
      prop.setProperty("MapSizeX", "<int: 3D printer max x i hele mm>");
      prop.setProperty("MapSizeY", "<int: 3D printer max Y i hele mm>");
      prop.setProperty("MapSizeZ", "<int: 3D printer max Z i hele mm>");
      prop.setProperty("RapidFeed", "<float: Ildgangsfeed, G0; i mm/min>");
      prop.setProperty("Feed", "<float: Feed, G0; i mm/min>");
      prop.setProperty("resolution", "<float: hvor mange mm går der pr pixel>");
      try {
        prop.store(new FileWriter(ConfFil), "Konfigurationsfil til Gkoden");
      }
      catch (FileNotFoundException e) {
        System.out.println("[ERROR]: Fil eksisterer ikke...");
      }
      catch (IOException e) {
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
                    //GAMMELT LAV OM NÅR TID ER
                    /*System.out.println("maxWidth = <int: 3D printer max x i hele mm>");
                    System.out.println("maxHeight = <int: 3D printer max Y i hele mm>");
                    System.out.println("minZ = <float: 3D printer min z vi kan flytte os til; i mm>");
                    System.out.println("rapidHeight = <float: Ildgangshøjde, G0;  i mm>");
                    System.out.println("zInterval = <float: interval vi kører blyanten i z. 0 er minimum og 255 er maximum; i mm>");
                    System.out.println("resolution = <float: Resolution i y i mm>");*/
                    CreateConfigFile();
                    System.exit(0);
                } else {
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
        /*
      MapSizeX=200
      MapSizeY=200
      MapSizeZ=50
      Feed=1500
      RapidFeed=3000
       */
        if(prop.getProperty("MapSizeX") != null && prop.getProperty("MapSizeY") != null && prop.getProperty("MapSizeZ") != null) {

            maxXYZ[0] = Integer.parseInt(prop.getProperty("MapSizeX"));
            System.out.println("Max width loaded[X]: "+maxXYZ[0]);

            maxXYZ[1] = Integer.parseInt(prop.getProperty("MapSizeY"));
            System.out.println("Max height loaded[Y]: "+maxXYZ[1]);

            maxXYZ[2] = Integer.parseInt(prop.getProperty("MapSizeZ"));
            System.out.println("Max height loaded[Z]: "+maxXYZ[2]);

            rapidFeed = Double.parseDouble(prop.getProperty("RapidFeed"));
            System.out.println("Rapid feed loaded: "+rapidFeed+" mm/min");

            feed = Double.parseDouble(prop.getProperty("Feed"));
            System.out.println("Feed loaded: "+feed+" mm/min");

            resolution = Double.parseDouble(prop.getProperty("resolution"));
            System.out.println("Resolution loaded: "+resolution+" mm/pixel");

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
      if(args.length != 0) { 
        Fil = args[0];
        if(Fil.equals("-help")) { //Hvis man ikke ved hvad man skal skrive
            System.out.println("java "+programName+" <PATH_TO_IMAGE>"); //Skriver hvad man skal skrive
            System.exit(0); //Stopper program
        }
      }
      else { //Hvis man ikke ved hvad man skal skrive
        System.out.println("java "+programName+" <PATH_TO_IMAGE>"); //Skriver hvad man skal skrive
        System.exit(0); //Stopper program
      }
      BufferedImage image;
      try {
        EdgeDetector edge = new EdgeDetector(Fil); //Laver objekt og læser billedefil
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
      if(resolution != 0) {
        if(imageHeight > imageWidth) {   // Formindsker billede til grid. Hvis Y er større end x formindsker den i forhold til Y
          imageWidth =(int)((imageScale*maxXYZ[1])/resolution);
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
            Billede[x][((imageHeight-1)-y)] = grayscale<128;
        }
        //System.out.println("loading linje "+(y+1)+"/"+((int)imageHeight)+" ...");
      }
      System.out.println("Billede indlæst...");
      //System.out.println("Billede X: "+imageWidth+"; Y: "+imageHeight);
      //wait(2000);
      //Hvis Billede[x][y] < 128 ignore ellers output Gkode
      String gkode = "";
      int lastPos[] = {0, 0};
      int NextPos[] = {0, 0};
      
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
        
        //System.out.println("x: "+x+"; y: "+y+"; Billede: "+Billede[x][y]+"; hasTaken: "+hasTaken[x][y]);
        
        
        if(Billede[x][y] && !hasTaken[x][y]) { 
          //Et sted hvor der er true i billede men false i hastaken.
          //Finder et nyt sted hvor der ikke tegnet
            
          gkode += "G0 X"+(double)(x*resolution)+" Y"+(double)(y*resolution)+"\n";
          gkode += "G1 Z0 F"+feed+"\n";
          //System.out.println("G0 X"+(double)(x*resolution)+" Y"+(double)(y*resolution));
          //System.out.println("G1 Z0 F"+feed);
  
          int[] dx = {-1, 1, 0, 0};  //Hvilken vej vil vi søge
          int[] dy = {0, 0, -1, 1};

          /*int[] dx = {1, 0, 0, -1};  //Hvilken vej vil vi søge
          int[] dy = {0, 1, -1, 0};*/
  
          Queue<Point> queue = new LinkedList<>();
          queue.add(new Point(x, y));
  
          while(!queue.isEmpty()) { //Søger efter steder hvor billedet er true og hasTaken er false ud fra det sted jeg er startet
            Point p = queue.poll();
            if(p.x < 0 || p.x >= imageWidth || p.y < 0 || p.y >= imageHeight || hasTaken[p.x][p.y] || !Billede[p.x][p.y]) { //Hvis jeg ikke er på limit, expand
              continue;
            }
  
            hasTaken[p.x][p.y] = true; 

           if(Math.sqrt(Math.pow((lastPos[0] - p.x), 2)+Math.pow((lastPos[1] - p.y), 2))> (resolution*12)) {
              gkode += "G0 Z2.0\n";
              gkode += "G0 X"+p.x*resolution+" Y"+p.y*resolution+"\n";
              gkode += "G1 Z0.0\n";

            }
            else {
              gkode += "G1 X"+p.x*resolution+" Y"+p.y*resolution+"\n";
              //System.out.println("G1 X"+p.x*resolution+" Y"+p.y*resolution);
            }
            for (int i = 0; i < 4; i++) {
              queue.add(new Point(p.x + dx[i], p.y + dy[i]));
            }
            lastPos[0] = p.x;
            lastPos[1] = p.y;
  
          }
          gkode += "G0 Z2\n";
          //System.out.println("G0 Z2");
        }
      }
      try (FileWriter writer = new FileWriter("output.gcode")) {
        System.out.println("Output.gcode was succesfully created/updated");
        writer.write(gkode);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }