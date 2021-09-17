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
public interface OutputHandler extends Serializable {

    /**
     * @param buf
     */
    void output ( ByteBuffer buf );


    /**
     * @param remaining
     */
    void eof ( ByteBuffer remaining );

}
