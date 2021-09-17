/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public interface ContentProvider extends Serializable {

    /**
     * 
     * @param ctx
     * @throws ExecutionException
     */
    void validate ( Context ctx ) throws ExecutionException;


    /**
     * @param ctx
     * @param c
     * @throws IOException
     */
    void transferTo ( Context ctx, FileChannel c ) throws IOException;
}
