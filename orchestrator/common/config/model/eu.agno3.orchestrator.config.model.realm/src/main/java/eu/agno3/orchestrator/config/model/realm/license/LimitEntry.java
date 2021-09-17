/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.license;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class LimitEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4286033210536558895L;

    private String key;
    private Long value;


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
    public Long getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( Long value ) {
        this.value = value;
    }
}
