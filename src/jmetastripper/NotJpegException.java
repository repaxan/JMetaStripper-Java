package jmetastripper;

/** Exception thrown when something is not JPEG data.
 *
 * @author Repaxan
 */
class NotJpegException extends Exception {
    
    NotJpegException() {
        super();
    }
    
    NotJpegException(String msg) {
        super(msg);
    }
}
