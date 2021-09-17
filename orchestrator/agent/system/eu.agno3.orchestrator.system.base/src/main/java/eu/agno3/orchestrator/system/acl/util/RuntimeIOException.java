/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 31, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.acl.util;


import java.io.IOException;


/**
 * @author mbechler
 *
 */
public class RuntimeIOException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7742807683332539533L;


    /**
     * @param cause
     */
    public RuntimeIOException ( IOException cause ) {
        super(cause);
    }

}
