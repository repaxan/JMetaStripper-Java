package jmetastripper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

/** Java class representation of the contents of a JPEG file.
 *
 * @author Repaxan
 */
class JpegFile {
    
    JpegFile(FileInputStream in) throws NotJpegException, IOException {
        byte[] inTwo = new byte[2];
        builtFile = new LinkedList<byte[]>();
        
        if (in.read(inTwo) != inTwo.length) {
            throw new NotJpegException("File is really small!");
        }
        if (!checkSOI(inTwo)) {
            throw new NotJpegException("File probably isn't JPEG!"
                    + " (did not start with SOI)");
        }
        builtFile.add(SOI);
        
        while (true) {
            byte[] markerSeg = new byte[2];
            if (in.read(markerSeg) != markerSeg.length) {
                throw new NotJpegException("File ended abruptly! [markerSeg]");
            } else if (markerSeg[0] != (byte) 0xFF) {
                System.out.println(toByteArray().length);
                throw new NotJpegException("File probably isn't JPEG!"
                        + " (byte after marker NOT 0xFF)");
            }
            builtFile.add(markerSeg);
            
            if (!checkSOS(markerSeg)) {
                byte[] sizeSeg = new byte[2];
                if (in.read(sizeSeg) != sizeSeg.length) {
                    throw new NotJpegException("File ended abruptly! [sizeSeg]");
                }
                builtFile.add(sizeSeg);

                int dataSegSize = ((((int) sizeSeg[1]) << 24) >>> 24)
                        + ((((int) sizeSeg[0]) << 24) >>> 16) - 2;
                byte[] dataSeg = new byte[dataSegSize];
                if (in.read(dataSeg) != dataSegSize) {
                    throw new NotJpegException("File ended abruptly! [dataSeg]");
                }
                builtFile.add(dataSeg);
            } else {
                byte[] remainder = new byte[(int)
                        (in.getChannel().size() - in.getChannel().position())];
                in.read(remainder);
                inTwo[0] = remainder[remainder.length - 2];
                inTwo[1] = remainder[remainder.length - 1];
                if (!checkEOI(inTwo)) {
                    throw new NotJpegException("File did NOT end in EOI!");
                }
                builtFile.add(remainder);
                
                break;
            }
        }
    }
    
    /** Spits me out as a byte array. */
    byte[] toByteArray() {
        int size = 0;
        for (byte[] k : builtFile) {
            size += k.length;
        }
        
        byte[] out = new byte[size];
        int i = 0;
        
        for (byte[] k : builtFile) {
            for (byte j : k) {
                out[i] = j;
                i += 1;
            }
        }
        
        return out;
    }
    
    /** Removes all Application Data segments (0xFF 0xE?) and comment
     * segments (0xFF 0xFE) from me. */
    void stripAppData() {
        if (builtFile.size() > 3) {
            LinkedList<byte[]> stripped = new LinkedList<byte[]>();
            stripped.add(SOI);
            
            ListIterator<byte[]> iter = builtFile.listIterator(1);
            int i = 1;
            
            while ((i + 2) < builtFile.size()) {
                byte[] marker = iter.next();
                if (!checkAppMarker(marker)) {
                    stripped.add(marker);
                    stripped.add(iter.next());
                    stripped.add(iter.next());
                } else {
                    iter.next();
                    iter.next();
                }
                i += 3;
            }
            
            stripped.add(builtFile.get(builtFile.size() - 2));
            stripped.add(builtFile.getLast());
            builtFile = stripped;
        }
    }
    
    /** Checks the given array against the constant JPEG start of image. */
    private static boolean checkSOI(byte[] in) {
        if (in.length != 2 || in[0] != SOI[0] || in[1] != SOI[1]) {
            return false;
        } else {
            return true;
        }
    }
    
    /** Checks the given array against the constant JPEG end of image. */
    private static boolean checkEOI(byte[] in) {
        if (in.length != 2 || in[0] != EOI[0] || in[1] != EOI[1]) {
            return false;
        } else {
            return true;
        }
    }
    
    /** Checks the given array against the constant JPEG start of scan. */
    private static boolean checkSOS(byte[] in) {
        if (in.length != 2 || in[0] != SOS[0] || in[1] != SOS[1]) {
            return false;
        } else {
            return true;
        }
    }
    
    /** Checks the given array for App Marker ness and comment ness. */
    private static boolean checkAppMarker(byte[] in) {
        return in.length == 2 && in[0] == (byte) 0xFF &&
                (((byte) (in[1] >>> 4) == (byte) 0xFE) || in[1] == (byte) 0xFE);
    }
    
    /** The structure of this file. Will be ordered:
     *    SOI, [MarkerSeg, SizeSeg, Dataseg]..., MarkerSeg, Remainder */
    private LinkedList<byte[]> builtFile;
    
    /** The marker for JPEG start of image. */
    private static final byte[] SOI = { (byte) 0xFF, (byte) 0xD8 };
    
    /** The marker for JPEG end of image. */
    private static final byte[] EOI = { (byte) 0xFF, (byte) 0xD9 };
    
    /** The marker for JPEG start of scan. */
    private static final byte[] SOS = { (byte) 0xFF, (byte) 0xDA };
}
