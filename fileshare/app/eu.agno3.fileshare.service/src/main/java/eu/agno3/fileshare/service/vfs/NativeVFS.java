/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.vfs.VFS;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    NativeVFS.class
} )
public class NativeVFS implements VFS {

    private DefaultServiceContext ctx;
    private BlockStorageService blockStore;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setBlockStore ( BlockStorageService bss ) {
        this.blockStore = bss;
    }


    protected synchronized void unsetBlockStore ( BlockStorageService bss ) {
        if ( this.blockStore == bss ) {
            this.blockStore = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFS#getId()
     */
    @Override
    public String getId () {
        return "NATIVE"; //$NON-NLS-1$
    }


    @Override
    public VFSContext begin ( boolean readOnly ) throws FileshareException {
        try {
            return new NativeVFSContext(this, readOnly);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to start entity transaction", e); //$NON-NLS-1$
        }
    }


    @Override
    public VFSContext begin ( EntityTransactionContext c ) throws FileshareException {
        return new NativeVFSContext(this, c);
    }


    EntityTransactionService getEntityTransactionService () {
        return this.ctx.getFileshareEntityTS();
    }


    BlockStorageService getBlockStore () {
        return this.blockStore;
    }

}
