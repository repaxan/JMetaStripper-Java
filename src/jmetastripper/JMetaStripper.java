package jmetastripper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/** Removes all metadata from JPEG files.
 * 
 * @author Repaxan
 */
public class JMetaStripper {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: java -jar JMetaStripper.jar FILENAME");
            System.out.println("    Strips all application segments (JFIF, Exif)");
            System.out.println("        from the given JPEG file.");
            System.out.println("    2 GiB file size limit.");
            System.exit(0);
        }
        
        if (!(args[0].endsWith(".jpg") || args[0].endsWith(".jpeg"))) { // todo: lower/uppercase
            System.out.println("Extension is not .jpg or .jpeg: " + args[0]);
            System.exit(1);
        }
        
        JpegFile jf = null;
        try {
            FileInputStream in = new FileInputStream(args[0]);
            jf = new JpegFile(in);
            in.close();
        } catch (FileNotFoundException excp) {
            System.out.println("No such file: " + args[0]);
            System.exit(1);
        } catch (NotJpegException excp) {
            System.out.println("Not a JPEG file: " + args[0]);
            System.out.println(excp.getMessage());
            System.exit(1);
        } catch (IOException excp) {
            System.out.println("Misc error: " + excp.toString());
            System.exit(1);
        }
        
        jf.stripAppData();
        
        try {
            FileOutputStream out = new FileOutputStream(args[0]);
            out.write(jf.toByteArray());
            out.close();
        } catch (FileNotFoundException excp) {
            System.out.println("No such file: " + args[0]);
            System.exit(1);
        } catch (IOException excp) {
            System.out.println("Misc error: " + excp.toString());
            System.exit(1);
        }
        
        System.exit(0);
        
    }
}
