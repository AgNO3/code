/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update;


import java.io.Serializable;
import java.util.UUID;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class UpdateInstallation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -789551344165969995L;

    private String stream;
    private long sequence;
    private DateTime installDate;

    private UUID referenceBackupId;


    /**
     * @return the stream
     */
    public String getStream () {
        return this.stream;
    }


    /**
     * @param stream
     *            the stream to set
     */
    public void setStream ( String stream ) {
        this.stream = stream;
    }


    /**
     * @return the sequence
     */
    public long getSequence () {
        return this.sequence;
    }


    /**
     * @param sequence
     *            the sequence to set
     */
    public void setSequence ( long sequence ) {
        this.sequence = sequence;
    }


    /**
     * @return the installDate
     */
    public DateTime getInstallDate () {
        return this.installDate;
    }


    /**
     * @param installDate
     *            the installDate to set
     */
    public void setInstallDate ( DateTime installDate ) {
        this.installDate = installDate;
    }


    /**
     * @param uuid
     */
    public void setReferenceBackupId ( UUID uuid ) {
        this.referenceBackupId = uuid;
    }


    /**
     * @return the referenceBackupId
     */
    public UUID getReferenceBackupId () {
        return this.referenceBackupId;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) ( this.sequence ^ ( this.sequence >>> 32 ) );
        result = prime * result + ( ( this.stream == null ) ? 0 : this.stream.hashCode() );
        return result;
    }
    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        UpdateInstallation other = (UpdateInstallation) obj;
        if ( this.sequence != other.sequence )
            return false;
        if ( this.stream == null ) {
            if ( other.stream != null )
                return false;
        }
        else if ( !this.stream.equals(other.stream) )
            return false;
        return true;
    }
    // -GENERATED


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s (%s)", this.sequence, this.stream); //$NON-NLS-1$
    }

}
