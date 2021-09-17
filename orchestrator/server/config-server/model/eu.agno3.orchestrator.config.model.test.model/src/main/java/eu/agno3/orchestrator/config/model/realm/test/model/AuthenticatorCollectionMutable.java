/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import java.util.List;


/**
 * @author mbechler
 * 
 */
public interface AuthenticatorCollectionMutable extends AuthenticatorCollection {

    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.AuthenticatorCollection#getAuthenticators()
     */
    @Override
    public List<AuthenticatorMutable> getAuthenticators ();


    /**
     * @param authenticators
     *            the authenticators to set
     */
    public void setAuthenticators ( List<AuthenticatorMutable> authenticators );
}