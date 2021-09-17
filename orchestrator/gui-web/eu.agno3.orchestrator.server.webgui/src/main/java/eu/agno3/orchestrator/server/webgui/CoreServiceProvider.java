/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ValidatorFactory;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.caching.CacheService;
import eu.agno3.runtime.i18n.ResourceBundleService;
import eu.agno3.runtime.security.credentials.CredentialWrapper;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;

import net.sf.ehcache.Ehcache;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class CoreServiceProvider {

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private XmlMarshallingService xmlMs;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private XmlParserFactory xmlParserFactory;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private ValidatorFactory validatorFactory;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private CacheService cacheService;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private ResourceBundleService localizationService;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private PasswordPolicyChecker passwordPolicy;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private CredentialWrapper credWrapper;


    /**
     * @return the validatorFactory
     */
    public ValidatorFactory getValidatorFactory () {
        return this.validatorFactory;
    }


    /**
     * @return the xmlMs
     */
    public XmlMarshallingService getMarshallingService () {
        return this.xmlMs;
    }


    /**
     * @return the xmlParserFactory
     */
    public XmlParserFactory getXmlParserFactory () {
        return this.xmlParserFactory;
    }


    /**
     * @return the passwordPolicy
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.passwordPolicy;
    }


    /**
     * @return the cacheService
     */
    public CacheService getCacheService () {
        return this.cacheService;
    }


    /**
     * 
     * @return the primary webgui cache instance
     */
    public Ehcache getCache () {
        return this.cacheService.getCache("server-webgui"); //$NON-NLS-1$
    }


    public CredentialWrapper getCredentialWrapper () {
        return this.credWrapper;
    }


    /**
     * @return the localizationService
     */
    public ResourceBundleService getLocalizationService () {
        return this.localizationService;
    }
}
