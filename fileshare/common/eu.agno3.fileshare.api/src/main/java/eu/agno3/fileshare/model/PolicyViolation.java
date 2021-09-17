/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.Arrays;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class PolicyViolation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8258184736360584295L;
    private final Serializable[] arguments;
    private final String key;


    /**
     * @param key
     * @param arguments
     * 
     */
    public PolicyViolation ( String key, Serializable... arguments ) {
        this.key = key;
        this.arguments = arguments;

    }


    /**
     * 
     * @return an identifying key for the violation
     */
    public String getKey () {
        return this.key;
    }


    /**
     * 
     * @return optional arguments for message formatting
     */
    public Object[] getArguments () {
        return this.arguments;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.arguments);
        result = prime * result + ( ( this.key == null ) ? 0 : this.key.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        PolicyViolation other = (PolicyViolation) obj;
        if ( !Arrays.equals(this.arguments, other.arguments) )
            return false;
        if ( this.key == null ) {
            if ( other.key != null )
                return false;
        }
        else if ( !this.key.equals(other.key) )
            return false;
        return true;
    }
    // -GENERATED

}
