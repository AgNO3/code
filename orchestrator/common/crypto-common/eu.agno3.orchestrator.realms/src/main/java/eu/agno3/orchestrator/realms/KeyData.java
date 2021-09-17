/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * @author mbechler
 *
 */
@Embeddable
public class KeyData implements Serializable, Comparable<KeyData> {

    /**
     * 
     */
    private static final long serialVersionUID = 7431222176431329992L;
    private String principal;
    private long kvno;
    private String algorithm;
    private String data;


    /**
     * @return the principal
     */
    @Column ( nullable = false )
    public String getPrincipal () {
        return this.principal;
    }


    /**
     * @param principal
     *            the principal to set
     */
    public void setPrincipal ( String principal ) {
        this.principal = principal;
    }


    /**
     * @return the kvno
     */
    public long getKvno () {
        return this.kvno;
    }


    /**
     * @param kvno
     *            the kvno to set
     */
    public void setKvno ( long kvno ) {
        this.kvno = kvno;
    }


    /**
     * @return the algorithm
     */
    @Column ( nullable = false )
    public String getAlgorithm () {
        return this.algorithm;
    }


    /**
     * @param algorithm
     *            the algorithm to set
     */
    public void setAlgorithm ( String algorithm ) {
        this.algorithm = algorithm;
    }


    /**
     * @return the data
     */
    public String getData () {
        return this.data;
    }


    /**
     * @param data
     *            the data to set
     */
    public void setData ( String data ) {
        this.data = data;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // + GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.algorithm == null ) ? 0 : this.algorithm.hashCode() );
        result = prime * result + (int) ( this.kvno ^ ( this.kvno >>> 32 ) );
        result = prime * result + ( ( this.principal == null ) ? 0 : this.principal.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // + GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        KeyData other = (KeyData) obj;
        if ( this.algorithm == null ) {
            if ( other.algorithm != null )
                return false;
        }
        else if ( !this.algorithm.equals(other.algorithm) )
            return false;
        if ( this.kvno != other.kvno )
            return false;
        if ( this.principal == null ) {
            if ( other.principal != null )
                return false;
        }
        else if ( !this.principal.equals(other.principal) )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( KeyData o ) {
        if ( this.principal != null ) {
            int res = this.principal.compareTo(this.principal);
            if ( res != 0 ) {
                return res;
            }
        }
        else {
            return 1;
        }

        int res = Long.compare(this.kvno, o.kvno);
        if ( res != 0 ) {
            return res;
        }

        if ( this.algorithm != null ) {
            res = this.algorithm.compareTo(o.algorithm);
            if ( res != 0 ) {
                return res;
            }
        }

        return 0;
    }


    /**
     * @param keyImportEntries
     * @return cloned key data
     */
    public static Set<KeyData> clone ( Set<KeyData> keyImportEntries ) {
        Set<KeyData> cloned = new HashSet<>();
        for ( KeyData k : keyImportEntries ) {
            cloned.add(clone(k));
        }
        return cloned;
    }


    /**
     * @param k
     * @return
     */
    private static KeyData clone ( KeyData k ) {
        KeyData cloned = new KeyData();
        cloned.algorithm = k.algorithm;
        cloned.data = k.data;
        cloned.kvno = k.kvno;
        cloned.principal = k.principal;
        return cloned;
    }

}
