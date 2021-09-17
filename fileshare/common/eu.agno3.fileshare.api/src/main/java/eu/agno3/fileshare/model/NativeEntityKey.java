/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class NativeEntityKey implements EntityKey, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 143988084793641343L;

    private final UUID id;


    /**
     * @param id
     */
    public NativeEntityKey ( UUID id ) {
        if ( id == null ) {
            throw new NullPointerException();
        }
        this.id = id;
    }


    /**
     * @return the id
     */
    public UUID getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.id == null ) {
            return StringUtils.EMPTY;
        }
        return this.id.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( EntityKey o ) {

        if ( o instanceof NativeEntityKey ) {
            NativeEntityKey k = (NativeEntityKey) o;

            if ( this.id == null && k.id == null ) {
                return 0;
            }
            else if ( this.id == null ) {
                return -1;
            }
            else if ( k.id == null ) {
                return 1;
            }

            return this.id.compareTo(k.id);
        }

        return -1;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof NativeEntityKey ) {
            NativeEntityKey k = (NativeEntityKey) obj;
            if ( this.id == null && k.id == null ) {
                return true;
            }
            else if ( this.id == null || k.id == null ) {
                return false;
            }

            return this.id.equals(k.id);
        }

        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {

        if ( this.id == null ) {
            return 0;
        }

        return this.id.hashCode();
    }
}
