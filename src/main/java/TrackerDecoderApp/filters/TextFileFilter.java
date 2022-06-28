package TrackerDecoderApp.filters;

import TrackerDecoderApp.util.Utils;

import java.io.File;
import javax.swing.filechooser.*;
 
/* ImageFilter.java is used by FileChooserDemo2.java. */
public class TextFileFilter extends FileFilter {
 
    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
        String extension = Utils.getExtension(f);
        if (extension != null) {
            return extension.equals(Utils.txt);
        }
 
        return false;
    }
 
    //The description of this filter
    public String getDescription() {
        return "Text Files";
    }
}