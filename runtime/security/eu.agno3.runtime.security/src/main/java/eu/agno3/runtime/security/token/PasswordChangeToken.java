/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.token;


/**
 * @author mbechler
 *
 */
public interface PasswordChangeToken {

    /**
     * @return the newPassword
     */
    public abstract String getNewPassword ();


    /**
     * @param newPassword
     */
    public abstract void setNewPassword ( String newPassword );

}