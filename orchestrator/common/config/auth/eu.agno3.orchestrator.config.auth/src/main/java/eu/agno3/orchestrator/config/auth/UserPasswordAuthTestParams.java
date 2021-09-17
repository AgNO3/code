/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class UserPasswordAuthTestParams implements ConfigTestParams {

    /**
     * 
     */
    private static final long serialVersionUID = -7917150794375410296L;

    private String username;
    private String password;


    /**
     * @return the username
     */
    public String getUsername () {
        return this.username;
    }


    /**
     * @param username
     *            the username to set
     */
    public void setUsername ( String username ) {
        this.username = username;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.password = password;
    }

}
