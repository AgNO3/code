/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public class QuotaReservation implements AutoCloseable {

    private QuotaServiceInternal quotaService;

    private final long reservation;
    boolean commited;

    private EntityKey rootId;

    private boolean dummy;


    /**
     * 
     */
    public QuotaReservation () {
        this.reservation = 0;
        this.dummy = true;
    }


    /**
     * @param quotaService
     * @param sizeDiff
     * 
     */
    public QuotaReservation ( QuotaServiceInternal quotaService, long sizeDiff ) {
        this.quotaService = quotaService;
        this.reservation = sizeDiff;
        this.dummy = true;
    }


    /**
     * @param quotaService
     * @param rootId
     * @param reservation
     * 
     */
    public QuotaReservation ( QuotaServiceInternal quotaService, EntityKey rootId, long reservation ) {
        this.quotaService = quotaService;
        this.rootId = rootId;
        this.reservation = reservation;
    }


    /**
     * Commit the reservation
     * 
     * @param v
     * 
     * @param e
     * @throws FileshareException
     */
    public void commit ( VFSContext v, VFSContainerEntity e ) throws FileshareException {
        if ( this.dummy ) {
            if ( this.reservation != 0 ) {
                this.quotaService.commit(v, e, this.reservation);
            }
            return;
        }
        this.quotaService.commit(v, e, this.reservation);
        this.commited = true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {

        if ( !this.dummy && !this.commited ) {
            this.quotaService.undoReservation(this.rootId, this.reservation);
        }
    }
}
