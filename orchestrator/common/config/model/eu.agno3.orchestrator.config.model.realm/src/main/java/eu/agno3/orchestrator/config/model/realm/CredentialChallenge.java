/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public class CredentialChallenge extends BaseChallenge {

    /**
     * 
     */
    private static final long serialVersionUID = -8260224295572224717L;

    private String username;
    private transient String password;
    private WrappedCredentials wrapped;


    /**
     * 
     */
    public CredentialChallenge () {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigApplyChallenge#getType()
     */
    @Override
    public String getType () {
        return "credential"; //$NON-NLS-1$
    }


    /**
     * 
     * @param key
     * @param required
     */
    public CredentialChallenge ( String key, boolean required ) {
        super(key, required);
    }


    /**
     * 
     * @param src
     * @param resp
     */
    public CredentialChallenge ( CredentialChallenge src, WrappedCredentials resp ) {
        super(src.getKey(), src.isRequired());
        this.wrapped = resp;
    }


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


    /**
     * @return the wrapped
     */
    public WrappedCredentials getWrapped () {
        return this.wrapped;
    }


    /**
     * @param wrapped
     *            the wrapped to set
     */
    public void setWrapped ( WrappedCredentials wrapped ) {
        this.wrapped = wrapped;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.BaseChallenge#verify()
     */
    @Override
    public boolean verify () {
        if ( !isRequired() ) {
            return true;
        }

        if ( this.wrapped != null ) {
            return true;
        }

        if ( this.username != null && this.password != null ) {
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.BaseChallenge#toString()
     */
    @Override
    public String toString () {
        if ( this.wrapped != null ) {
            return super.toString() + "[WRAPPED]"; //$NON-NLS-1$
        }
        return super.toString() + '=' + this.getUsername();
    }
}
