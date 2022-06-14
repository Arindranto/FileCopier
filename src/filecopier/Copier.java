/* the class used to make it happen*/
package filecopier;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Copier {
    protected String base;    // Base directory and the destination one
    protected String dest;
    protected ArrayList<File> files;   // Files to copy from
    public Copier() throws IOException{
        // Constructor
        Scanner sc= new Scanner(System.in); // Scanner
        
        // Scanning the base directory and the destination one
        System.out.print("Enter the base folder to check for linked file directory: ");
        this.base= sc.nextLine();
        System.out.print("Enter the destination folder to create the compound folder and files: ");
        this.dest= sc.nextLine();
        this.files= new ArrayList<>();  // Instanciation of the ArrayList of file
        
        // Existence check
        // base folder
        if (!Files.exists(Paths.get(base))){
            // If base is invalid
            System.out.println(base + " is inexistent");
        }
        else{
            // else make a list of the linked file
            File _files= new File(base);
            for (File file: _files.listFiles(f -> {
                // Directory having a dot in their name and not starting with a dot
                return f.isDirectory() && f.getName().contains(".") && !f.getName().startsWith(".");
            })){ // Filter the linked named directories
                this.files.add(file);   // Adding them to the files in the list
            }
        }
        System.out.println(files.size() + " linked folder(s) found");
        // destination folder
        // base folder
        if (!Files.exists(Paths.get(dest))){
            // If base is invalid
            System.out.print(dest + " is not recognized. Create a new folder? (y/n): ");
            if (sc.next().toLowerCase().charAt(0) == 'y'){
                Files.createDirectories(Paths.get(dest));
            }
            System.out.println(dest + " created successfully");
        }
        
        // Path adjustment using backslashes and removing end backslashes
        String[] tmp;
        // Source folder
        tmp= base.split("\\\\");
        base= "";
        for (int i= 0; i < tmp.length; i++)
            base+= tmp[i] + (i == tmp.length-1? "": "\\");
        tmp= base.split("/");
        base= "";
        for (int i= 0; i < tmp.length; i++)
            base+= tmp[i] + (i == tmp.length-1? "": "\\");
        // Destination folder
        tmp= dest.split("\\\\");
        dest= "";
        for (int i= 0; i < tmp.length; i++)
            dest+= tmp[i] + (i == tmp.length-1? "": "\\");
        tmp= dest.split("/");
        dest= "";
        for (int i= 0; i < tmp.length; i++)
            dest+= tmp[i] + (i == tmp.length-1? "": "\\");
    }
    
    private static ArrayList<String> buildTree(File root){
        // Get all files presents int the root folder
        ArrayList<String> s= new ArrayList<>(); // Return variable
        
        for (File g: root.listFiles(file -> {
                return !file.isDirectory();
            })){
            s.add(g.getAbsolutePath());
            // Only take the path from the base file
        }
        
        File[] sub= root.listFiles(file -> {
            // File filter to list the directories
            return file.isDirectory();
        });
        
        for (File f: sub){
            s.addAll(buildTree(f));
        }
        return s;
    }
   
    private String parsePath(String path) throws IOException{
        // Return the path to the destination directory and creating it on the go
        int offset= base.split("\\\\").length; // Offset
        String[] p= path.split("\\\\"); // Split path
        String[] q;
        String filename= p[p.length - 1];   // Store the filename
        String r= dest;   // Return value
        
        // Reassemble the path without the dest path replacing the base path
        for (int i= offset; i < p.length - 1; i++){
            //s= p[i].split("\\.");   // The dot of the linked directorie
             // Change the linked directories into embedded on
             q= p[i].split("\\.");
            for (int j= 0; j < q.length; j++) { // Best to iterate an array elements
                if ((q[j].toLowerCase().equals(q[j].toUpperCase()) || q[j].contains("-")) && j>0)   // version number
                    r+= "." + q[j];
                else
                    if (q[j].length() < 30)
                        r+= "\\" + q[j];
            }
        }
        
        // Creating the path if it doesn't exist yet
        if (!Files.exists(Paths.get(r)))
            Files.createDirectories(Paths.get(r));
        
        // Adding the filename
        r+= "\\" + filename;
        
        return r;
    }
    
    public int copy(ArrayList<String> sources) throws IOException{
        // Make copies of all the elements having their paths in sources
        String dst;
        int copied= 0;  // counter
        for (String src: sources) {
            if (!src.contains("RECYCLE")){  // Only if it is not a wrong path redirected to the bin folder
                dst= this.parsePath(src); // Destination path

                // The copy
                System.out.println("Source: " + src + "\nDestination: " + dst);
                try{
                    Files.copy(Paths.get(src), Paths.get(dst));
                    copied++;
                }
                catch (FileAlreadyExistsException e){
                    System.out.println(e.getMessage() + " already exists");
                }
            }
        }
        return copied;
    }
    
    public void showFiles(){
        // Show all linked file in the base element
        files.forEach(file -> {
            System.out.println(file.getName());
        });
    }
    
    public void start() throws IOException{
        // Main process of the Copier class with stylish output
        // Create a tree for each element in the directory list and process them one by one
        ArrayList<String> srcs;
        int copied= 0;  // Copied element
        long chrono= LocalTime.now().toNanoOfDay();  // Chronometer
        System.out.println("Started operation... ");
        for (File file: files){
            srcs= Copier.buildTree(file);
            System.out.println("-------------------------------------------------------------------");
            System.out.println("**********\t" + file.getName() + ": " + srcs.size() + " element(s)\t*********");
            System.out.println("-------------------------------------------------------------------");
            copied+= this.copy(srcs);  // Get the number of copied elements
            System.out.println("-------------------------------------------------------------------");
        }
        chrono-= LocalTime.now().toNanoOfDay();
        
        // Final count and stats
        System.out.println("-----------------\t" + copied + " file(s) copied in " + chrono * -0.000000001 + " seconds \t---------------------");
    }
}
