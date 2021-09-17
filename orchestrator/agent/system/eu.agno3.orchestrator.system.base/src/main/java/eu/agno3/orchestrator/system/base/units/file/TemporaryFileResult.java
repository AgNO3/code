/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author mbechler
 * 
 */
public class TemporaryFileResult extends FileResult {

    /**
     * 
     */
    private static final long serialVersionUID = 6158113733764356287L;
    private transient Path actualDestination;


    /**
     * @param p
     * @param actualTarget
     */
    public TemporaryFileResult ( Path p, Path actualTarget ) {
        super(p);
        this.actualDestination = actualTarget;
    }


    /**
     * @return the actualDestination
     */
    public Path getActualTarget () {
        return this.actualDestination;
    }


    private void readObject ( ObjectInputStream ois ) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.actualDestination = Paths.get(ois.readUTF());
    }


    private void writeObject ( ObjectOutputStream oos ) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(this.actualDestination.toString());
    }
}
