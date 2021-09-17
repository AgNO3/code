/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class UpdateDescriptorRef implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2644455289240267526L;

    private String stream;
    private long sequence;
    private String imageType;


    /**
     * 
     */
    public UpdateDescriptorRef () {}


    /**
     * @param stream
     * @param imageType
     * @param sequence
     */
    public UpdateDescriptorRef ( String stream, String imageType, long sequence ) {
        this.stream = stream;
        this.imageType = imageType;
        this.sequence = sequence;
    }


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


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.imageType == null ) ? 0 : this.imageType.hashCode() );
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
        UpdateDescriptorRef other = (UpdateDescriptorRef) obj;
        if ( this.imageType == null ) {
            if ( other.imageType != null )
                return false;
        }
        else if ( !this.imageType.equals(other.imageType) )
            return false;
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
}
