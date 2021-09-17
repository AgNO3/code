/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class CertificateInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5647974716900453229L;
    private String certificateData;


    /**
     * @return the certificateData
     */
    public String getCertificateData () {
        return this.certificateData;
    }


    /**
     * @param data
     */
    public void setCertificateData ( String data ) {
        this.certificateData = data;
    }

}
