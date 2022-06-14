/* Made to copy file from a directory to another compound one */
package filecopier;

import java.io.IOException;

public class FileCopier {
    public static void main(String[] args) throws IOException {
        Copier c= new Copier();
        c.start();
    } 
}
