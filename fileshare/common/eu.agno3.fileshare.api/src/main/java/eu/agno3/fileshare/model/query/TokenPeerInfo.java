/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class TokenPeerInfo implements PeerInfo, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1871505256243332145L;
    private static final UUID TOKEN_PEER_ID = UUID.fromString("23a87a50-09a4-4725-89f2-85ceebbf1242"); //$NON-NLS-1$

    private UUID id = TOKEN_PEER_ID;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#getId()
     */
    @Override
    public UUID getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#haveSharedFrom()
     */
    @Override
    public boolean haveSharedFrom () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.query.PeerInfo#haveSharedTo()
     */
    @Override
    public boolean haveSharedTo () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "token"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return obj instanceof TokenPeerInfo;
    }
}
