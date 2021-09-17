/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class DHCPOption implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8716393520324788583L;

    private String key;
    private List<String> values = new ArrayList<>();


    /**
     * 
     */
    public DHCPOption () {}


    /**
     * 
     * @param key
     * @param values
     */
    public DHCPOption ( String key, String... values ) {
        this.key = key;
        this.values = new ArrayList<>(Arrays.asList(values));
    }


    /**
     * 
     * @param key
     * @param values
     */
    public DHCPOption ( String key, Collection<String> values ) {
        this.key = key;
        this.values = new ArrayList<>(values);
    }


    /**
     * @return the key
     */
    public String getKey () {
        return this.key;
    }


    /**
     * @param key
     *            the key to set
     */
    public void setKey ( String key ) {
        this.key = key;
    }


    /**
     * @return the values
     */
    public List<String> getValues () {
        return this.values;
    }


    /**
     * @param values
     *            the values to set
     */
    public void setValues ( List<String> values ) {
        this.values = values;
    }


    /**
     * 
     * @return single option value, null if option was not found
     * @throws IllegalArgumentException
     *             if the option is not single valued
     */
    public String getSingleValue () {
        List<String> val = this.values;
        if ( val == null ) {
            return null;
        }
        if ( val.size() != 1 ) {
            throw new IllegalArgumentException("Option is multi valued " + this.key); //$NON-NLS-1$
        }
        return val.iterator().next();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.values.size() > 1 ) {
            return String.format("%s=%s", this.key, this.values); //$NON-NLS-1$
        }
        else if ( this.values.size() == 1 ) {
            return String.format("%s=%s", this.key, this.values.get(0)); //$NON-NLS-1$
        }
        else {
            return this.key;
        }
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.key == null ) ? 0 : this.key.hashCode() );
        result = prime * result + ( ( this.values == null ) ? 0 : this.values.hashCode() );
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
        DHCPOption other = (DHCPOption) obj;
        if ( this.key == null ) {
            if ( other.key != null )
                return false;
        }
        else if ( !this.key.equals(other.key) )
            return false;
        if ( this.values == null ) {
            if ( other.values != null )
                return false;
        }
        else if ( !this.values.equals(other.values) )
            return false;
        return true;
    }
    // -GENERATED

}
