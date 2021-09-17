/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Table ( name = "security_level" )
@SafeSerialization
public class SecurityLabel implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4649327900534704265L;
    private String label;


    /**
     * @return the label
     */
    @Id
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.label == null ) ? 0 : this.label.hashCode() );
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
        SecurityLabel other = (SecurityLabel) obj;
        if ( this.label == null ) {
            if ( other.label != null )
                return false;
        }
        else if ( !this.label.equals(other.label) )
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
        return this.label;
    }
}
