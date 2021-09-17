/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class ExtensionData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5230466747314661742L;
    private String oid;
    private boolean critial;
    private String data;


    /**
     * @return extension oid
     */
    public String getOid () {
        return this.oid;
    }


    /**
     * @param oid
     *            the oid to set
     */
    public void setOid ( String oid ) {
        this.oid = oid;
    }


    /**
     * @return criticality
     */
    public boolean getCritical () {
        return this.critial;
    }


    /**
     * @param critial
     *            the critial to set
     */
    public void setCritical ( boolean critial ) {
        this.critial = critial;
    }


    /**
     * @return the extension data
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
}
