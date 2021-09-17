/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( {
    "javadoc"
} )
public class ObjectFactory {

    public ADAuthenticator createADAuthenticator () {
        return new ADAuthenticatorImpl();
    }


    public RADIUSAuthenticator createRADIUSAuthenticator () {
        return new RADIUSAuthenticatorImpl();
    }


    public AuthenticatorCollection createAuthenticatorCollection () {
        return new AuthenticatorCollectionImpl();
    }


    public FileShareConfiguration createFileShareConfiguration () {
        return new FileShareConfigurationImpl();
    }
}
