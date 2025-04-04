package librol;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Marcos
 */
public class TypeOfFile extends FileFilter {
    //Type of file that should be display in JFileChooser will be set here  
    //We choose to display only directory and text file  

    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
    }

    //Set description for the type of file that should be display  
    public String getDescription() {
        return "Documentos de texto (*.txt)";
    }
}
