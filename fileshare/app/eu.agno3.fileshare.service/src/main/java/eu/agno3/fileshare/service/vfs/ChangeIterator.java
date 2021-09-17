/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 23, 2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


import eu.agno3.fileshare.model.ContainerChangeEntry;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.vfs.VFSChange;
import eu.agno3.fileshare.vfs.VFSContainerChange;
import eu.agno3.fileshare.vfs.VFSEntityChange;
import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class ChangeIterator <T extends VFSEntity> implements ClosableIterator<VFSChange> {

    private ClosableIterator<T> modEntries;
    private boolean doneWithModifications;
    private ClosableIterator<ContainerChangeEntry> contEntries;


    /**
     * @param modEntries
     * @param contEntries
     */
    public ChangeIterator ( ClosableIterator<T> modEntries, ClosableIterator<ContainerChangeEntry> contEntries ) {
        this.modEntries = modEntries;
        this.contEntries = contEntries;
        this.doneWithModifications = !this.modEntries.hasNext();
        if ( this.doneWithModifications ) {
            this.modEntries.close();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return ( !this.doneWithModifications && this.modEntries.hasNext() )
                || ( this.doneWithModifications && this.contEntries != null && this.contEntries.hasNext() );
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public VFSChange next () {
        if ( !this.doneWithModifications ) {
            VFSEntity e = this.modEntries.next();
            this.doneWithModifications = !this.modEntries.hasNext();
            if ( this.doneWithModifications ) {
                this.modEntries.close();
            }
            return new VFSEntityChange(e);
        }
        if ( this.contEntries == null ) {
            throw new IllegalStateException();
        }
        ContainerChangeEntry ch = this.contEntries.next();
        if ( ch == null ) {
            throw new IllegalArgumentException();
        }
        return new VFSContainerChange(ch.getContainer(), ch.getLocalName(), ch.getEntityType(), ch.getChangeType(), ch.getChangeTime());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.util.iter.ClosableIterator#close()
     */
    @Override
    public void close () {
        if ( !this.doneWithModifications ) {
            this.modEntries.close();
        }
        if ( this.contEntries != null ) {
            this.contEntries.close();
        }
    }

}
