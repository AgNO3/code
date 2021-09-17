/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.server.data;


import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "orchestrator" )
@Entity
@Table ( name = "update_desc_cache", indexes = {
    @Index ( columnList = "imageType,stream,sequence", unique = true )
} )
public class DescriptorCache implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4622810278596068858L;

    private UUID id;
    private long version;

    private String imageType;
    private String stream;
    private long sequence;

    private DateTime lastUpdated;
    private byte[] data;


    /**
     * 
     * @return the object id
     */
    @Id
    @GeneratedValue ( generator = "system-uuid" )
    @GenericGenerator ( name = "system-uuid", strategy = "uuid2" )
    @Column ( length = 16 )
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
     * 
     * @return the version
     */
    @Version
    public long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( long version ) {
        this.version = version;
    }


    /**
     * 
     * @return the release stream of this descriptor
     */
    public String getStream () {
        return this.stream;
    }


    /**
     * 
     * @param stream
     */
    public void setStream ( String stream ) {
        this.stream = stream;
    }


    /**
     * @return the imageType
     */
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }


    /**
     * 
     * @return the descriptor sequence number
     */
    public long getSequence () {
        return this.sequence;
    }


    /**
     * 
     * @param sequence
     */
    public void setSequence ( long sequence ) {
        this.sequence = sequence;
    }


    /**
     * 
     * @return the time this entry was created
     */
    public DateTime getLastUpdated () {
        return this.lastUpdated;
    }


    /**
     * 
     * @param lastUpdated
     */
    public void setLastUpdated ( DateTime lastUpdated ) {
        this.lastUpdated = lastUpdated;
    }


    /**
     * @return the descriptor data
     */
    @Lob
    public byte[] getData () {
        return this.data;
    }


    /**
     * @param data
     */
    public void setData ( byte[] data ) {
        this.data = data;
    }


    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        return result;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        DescriptorCache other = (DescriptorCache) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        return true;
    }
}
