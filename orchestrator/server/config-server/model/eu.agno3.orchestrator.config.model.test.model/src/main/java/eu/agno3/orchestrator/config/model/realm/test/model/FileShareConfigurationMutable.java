/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( FileShareConfiguration.class )
public interface FileShareConfigurationMutable extends FileShareConfiguration {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.FileShareConfiguration#getAuthenticators()
     */
    @Override
    AuthenticatorCollectionMutable getAuthenticators ();


    /**
     * @param authenticators
     *            the authenticators to set
     */
    void setAuthenticators ( AuthenticatorCollectionMutable authenticators );

}