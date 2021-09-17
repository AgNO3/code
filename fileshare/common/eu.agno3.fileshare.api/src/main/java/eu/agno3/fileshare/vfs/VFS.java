/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.vfs;


import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface VFS {

    /**
     * 
     * @return the VFS id
     */
    public String getId ();


    /**
     * @param readOnly
     * @return a VFS context
     * @throws FileshareException
     */
    VFSContext begin ( boolean readOnly ) throws FileshareException;


    /**
     * Begin using an existing entity transaction
     * 
     * @param ctx
     * @return a VFS context
     * @throws FileshareException
     */
    default VFSContext begin ( EntityTransactionContext ctx ) throws FileshareException {
        return begin(false);
    }

}
