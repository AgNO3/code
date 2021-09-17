/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author mbechler
 * 
 */
public class MessageContextEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3859825545118044330L;
    private String key;
    private String value;


    /**
     * 
     */
    public MessageContextEntry () {}


    /**
     * @param k
     * @param v
     */
    public MessageContextEntry ( String k, String v ) {
        this.key = k;
        this.value = v;
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
     * @return the value
     */
    public String getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( String value ) {
        this.value = value;
    }


    /**
     * @param stateContext
     * @return the converted value
     */
    public static Set<MessageContextEntry> fromMap ( Map<String, String> stateContext ) {
        Set<MessageContextEntry> res = new HashSet<>();
        for ( Entry<String, String> e : stateContext.entrySet() ) {
            res.add(new MessageContextEntry(e.getKey(), e.getValue()));
        }
        return res;
    }


    /**
     * 
     * @param entries
     * @return the converted value
     */
    public static Map<String, String> toMap ( Set<MessageContextEntry> entries ) {
        Map<String, String> res = new HashMap<>();
        for ( MessageContextEntry e : entries ) {
            res.put(e.getKey(), e.getValue());
        }
        return res;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.key == null ) ? 0 : this.key.hashCode() );
        result = prime * result + ( ( this.value == null ) ? 0 : this.value.hashCode() );
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
        if ( ! ( obj instanceof MessageContextEntry ) )
            return false;
        MessageContextEntry other = (MessageContextEntry) obj;
        if ( this.key == null ) {
            if ( other.key != null )
                return false;
        }
        else if ( !this.key.equals(other.key) )
            return false;
        if ( this.value == null ) {
            if ( other.value != null )
                return false;
        }
        else if ( !this.value.equals(other.value) )
            return false;
        return true;
    }
    // -GENERATED

}
