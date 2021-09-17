/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class KeytabInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1559657633411802488L;

    private String id;
    private List<KeyInfo> keys = new ArrayList<>();


    /**
     * @return the id
     */
    public String getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( String id ) {
        this.id = id;
    }


    /**
     * @return the keys
     */
    public List<KeyInfo> getKeys () {
        return this.keys;
    }


    /**
     * @param keys
     *            the keys to set
     */
    public void setKeys ( List<KeyInfo> keys ) {
        this.keys = keys;
    }
}
