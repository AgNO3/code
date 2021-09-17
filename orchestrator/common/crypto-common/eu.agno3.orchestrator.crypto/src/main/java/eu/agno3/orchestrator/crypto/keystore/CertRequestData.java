/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class CertRequestData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 259608512421323101L;
    private String requestPassword;
    private String subject;
    private Set<ExtensionData> extensions = new HashSet<>();
    private int lifetimeDays;


    /**
     * @return the request password
     */
    public String getRequestPassword () {
        return this.requestPassword;
    }


    /**
     * @param requestPassword
     *            the requestPassword to set
     */
    public void setRequestPassword ( String requestPassword ) {
        this.requestPassword = requestPassword;
    }


    /**
     * @return the subject
     */
    public String getSubject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject ( String subject ) {
        this.subject = subject;
    }


    /**
     * @return the requested extensions
     */
    public Set<ExtensionData> getExtensions () {
        return this.extensions;
    }


    /**
     * @param extensions
     *            the extensions to set
     */
    public void setExtensions ( Set<ExtensionData> extensions ) {
        this.extensions = extensions;
    }


    /**
     * @return the desired certificate lifetime (only for self signed certs)
     */
    public int getLifetimeDays () {
        return this.lifetimeDays;
    }


    /**
     * @param lifetimeDays
     *            the lifetimeDays to set
     */
    public void setLifetimeDays ( int lifetimeDays ) {
        this.lifetimeDays = lifetimeDays;
    }
}
