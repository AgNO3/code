/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.model.tokens;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class SingleUseToken implements Externalizable {

    /**
     * 
     */
    private static final long serialVersionUID = -5028273146200685433L;

    private UUID id;


    /**
     * @return the id
     */
    public UUID getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( UUID id ) {
        this.id = id;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        out.writeLong(this.id.getMostSignificantBits());
        out.writeLong(this.id.getLeastSignificantBits());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        long msb = in.readLong();
        long lsb = in.readLong();
        this.id = new UUID(msb, lsb);
    }

}
