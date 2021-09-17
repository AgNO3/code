/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.Serializable;
import java.nio.ByteBuffer;


/**
 * @author mbechler
 * 
 */
public interface InputProvider extends Serializable {

    /**
     * Call on start of input
     */
    void doStart ();


    /**
     * Called before the stream is closed
     */
    void doEnd ();


    /**
     * 
     * @return whether the stream shall be closed
     */
    boolean eof ();


    /**
     * @return a buffer containing data that should be feeded to input
     * @throws InterruptedException
     */
    ByteBuffer getInput () throws InterruptedException;

}
