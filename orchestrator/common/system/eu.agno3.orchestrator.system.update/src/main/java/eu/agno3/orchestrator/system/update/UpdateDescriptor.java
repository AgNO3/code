/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.beans.Transient;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class UpdateDescriptor implements Serializable, Comparable<UpdateDescriptor> {

    /**
     * 
     */
    private static final long serialVersionUID = -8027577352020742978L;

    private long sequence;
    private String imageType;

    private DateTime releaseDate;
    private URI changeLogRef;

    private List<UpdateDescriptorRef> includes = new ArrayList<>();
    private List<ServiceUpdateDescriptor> descriptors = new ArrayList<>();


    /**
     * 
     */
    public UpdateDescriptor () {}


    /**
     * @param root
     */
    public UpdateDescriptor ( UpdateDescriptor root ) {
        this.sequence = root.sequence;
        this.imageType = root.imageType;
        this.releaseDate = root.releaseDate;
        this.changeLogRef = root.changeLogRef;
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
     * @return the releaseDate
     */
    public DateTime getReleaseDate () {
        return this.releaseDate;
    }


    /**
     * @param releaseDate
     *            the releaseDate to set
     */
    public void setReleaseDate ( DateTime releaseDate ) {
        this.releaseDate = releaseDate;
    }


    /**
     * @return the changeLogRef
     */
    public URI getChangeLogRef () {
        return this.changeLogRef;
    }


    /**
     * @param changeLogRef
     *            the changeLogRef to set
     */
    public void setChangeLogRef ( URI changeLogRef ) {
        this.changeLogRef = changeLogRef;
    }


    /**
     * @return the includes
     */
    public List<UpdateDescriptorRef> getIncludes () {
        return this.includes;
    }


    /**
     * @param includes
     *            the includes to set
     */
    public void setIncludes ( List<UpdateDescriptorRef> includes ) {
        this.includes = includes;
    }


    /**
     * @return the descriptors
     */
    public List<ServiceUpdateDescriptor> getDescriptors () {
        return this.descriptors;
    }


    /**
     * @param descriptors
     *            the descriptors to set
     */
    public void setDescriptors ( List<ServiceUpdateDescriptor> descriptors ) {
        this.descriptors = descriptors;
    }


    /**
     * @param stream
     * @return a reference to this descriptor
     */
    @Transient
    public UpdateDescriptorRef getReference ( String stream ) {
        return new UpdateDescriptorRef(stream, this.imageType, this.sequence);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( UpdateDescriptor o ) {
        return Long.compare(this.getSequence(), o.getSequence());
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.imageType == null ) ? 0 : this.imageType.hashCode() );
        result = prime * result + (int) ( this.sequence ^ ( this.sequence >>> 32 ) );
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
        UpdateDescriptor other = (UpdateDescriptor) obj;
        if ( this.imageType == null ) {
            if ( other.imageType != null )
                return false;
        }
        else if ( !this.imageType.equals(other.imageType) )
            return false;
        if ( this.sequence != other.sequence )
            return false;
        return true;
    }

    // -GENERATED

}
