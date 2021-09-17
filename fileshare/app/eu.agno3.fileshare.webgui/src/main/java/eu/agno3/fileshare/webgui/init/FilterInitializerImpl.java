/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.init;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.mgt.AuthorizingSecurityManager;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.fileshare.service.ChunkUploadService;
import eu.agno3.fileshare.service.FileDownloadFilter;
import eu.agno3.fileshare.service.VFSService;
import eu.agno3.fileshare.service.config.FrontendConfiguration;
import eu.agno3.fileshare.webdav.FileshareWebDAVServlet;
import eu.agno3.fileshare.webgui.chunkupload.IncompleteUploadRequestListener;
import eu.agno3.fileshare.webgui.oc.OCCompatServlet;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.security.DynamicModularRealmAuthenticator;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.terms.TermsFilter;
import eu.agno3.runtime.security.web.SecurityHeadersFilterConfig;
import eu.agno3.runtime.security.web.SecurityInitializer;
import eu.agno3.runtime.security.web.filter.HTTPAuthenticationFilter;
import eu.agno3.runtime.security.web.filter.SecurityHeadersFilter;
import eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl;
import eu.agno3.runtime.security.web.login.WebLoginConfig;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;
import eu.agno3.runtime.security.web.login.token.URLTokenAuthenticationFilter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FilterInitializerImpl extends AbstractModularRealmSecurityInitializerImpl implements SecurityInitializer {

    private static final Logger log = Logger.getLogger(FilterInitializerImpl.class);
    /**
     * 
     */
    private static final String ALL_PATTERN = "/**"; //$NON-NLS-1$
    private static final String FILES_PATTERN = "/files/**"; //$NON-NLS-1$
    private static final String DAV_PATTERN = "/dav/**"; //$NON-NLS-1$
    private static final String OC_COMPATDAV_PATTERN = "/remote.php/webdav/**"; //$NON-NLS-1$
    private static final String OC_COMPAT_PATTERN = "/ocs/**"; //$NON-NLS-1$
    private static final String OC_COMPAT_ALT_PATTERN = "/index.php/**"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String[] HTTP_AUTH_CHAINS = new String[] {
        DAV_PATTERN, OC_COMPATDAV_PATTERN, OC_COMPAT_PATTERN, OC_COMPAT_ALT_PATTERN
    };

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private DynamicModularRealmAuthenticator modAuth;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private DynamicModularRealmAuthorizer modAuthz;

    @Inject
    private WebLoginConfig webLoginConfig;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private TokenGenerator cryptoTokenValidator;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private FileDownloadFilter downloadFilter;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private UADetector uaDetector;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private HTTPAuthenticationFilter httpAuthFilter;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private FileshareWebDAVServlet webDavServlet;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private ChunkUploadService chunkUploadService;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private SecurityHeadersFilterConfig hdrConfig;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private VFSService vfs;

    @Inject
    @OsgiService ( dynamic = true, timeout = 400 )
    private FrontendConfiguration frontendConfiguration;

    @Inject
    private OCCompatServlet ocCompatServlet;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#initWebEnvironment(org.apache.shiro.web.env.WebEnvironment)
     */
    @Override
    public void initWebEnvironment ( WebEnvironment environment ) {
        super.initWebEnvironment(environment);

        log.debug("Adding WebDAV servlet"); //$NON-NLS-1$
        Dynamic reg = environment.getServletContext().addServlet("WebDAVServlet", this.webDavServlet); //$NON-NLS-1$
        reg.addMapping("/dav/*"); //$NON-NLS-1$
        reg.addMapping("/remote.php/webdav/*"); //$NON-NLS-1$
        reg.setMultipartConfig(new MultipartConfigElement("/tmp", -1, -1, 16384)); //$NON-NLS-1$

        reg = environment.getServletContext().addServlet("OCCompatServlet", this.ocCompatServlet); //$NON-NLS-1$
        reg.addMapping("/ocs/*"); //$NON-NLS-1$
        reg.addMapping("/index.php/*"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#setupFilterChains(eu.agno3.runtime.security.web.login.WebLoginConfig,
     *      org.apache.shiro.web.filter.mgt.FilterChainManager, org.apache.shiro.mgt.AuthorizingSecurityManager)
     */
    @Override
    protected void setupFilterChains ( WebLoginConfig config, FilterChainManager fcm, AuthorizingSecurityManager asm ) {
        super.setupFilterChains(config, fcm, asm);
        URLTokenAuthenticationFilter tokenFilter = new URLTokenAuthenticationFilter(this.cryptoTokenValidator);
        tokenFilter.processPathConfig(ALL_PATTERN, null);
        TermsFilter tf = getTermsFilter();
        NamedFilterList allChain = fcm.getChain(ALL_PATTERN);
        allChain.add(0, new IncompleteUploadRequestListener(this.chunkUploadService, this.vfs));
        allChain.add(1, tokenFilter);
        if ( tf != null ) {
            allChain.add(tf);
        }

        for ( String httAuthChain : HTTP_AUTH_CHAINS ) {
            NamedFilterList fc = fcm.getChain(httAuthChain);
            fc.add(0, this.httpAuthFilter);
            if ( tf != null ) {
                fc.add(1, tf);
            }
        }

        NamedFilterList filesChain = fcm.getChain(FILES_PATTERN);
        filesChain.add(0, tokenFilter);
        if ( tf != null ) {
            filesChain.add(1, tf);
        }
        filesChain.add(this.downloadFilter);

        SecurityHeadersFilter hdrFilter = new SecurityHeadersFilter(this.uaDetector, this.hdrConfig);
        for ( String chainName : fcm.getChainNames() ) {
            NamedFilterList chain = fcm.getChain(chainName);
            hdrFilter.processPathConfig(chainName, null);
            chain.add(0, hdrFilter);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#getAuthRealm()
     */
    @Override
    protected Authenticator getAuthRealm () {
        return this.modAuth;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#getAuthzRealm()
     */
    @Override
    protected Authorizer getAuthzRealm () {
        return this.modAuthz;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.gui.init.AbstractModularRealmSecurityInitializerImpl#getWebLoginConfig()
     */
    @Override
    protected WebLoginConfig getWebLoginConfig () {
        return this.webLoginConfig;
    }
}
