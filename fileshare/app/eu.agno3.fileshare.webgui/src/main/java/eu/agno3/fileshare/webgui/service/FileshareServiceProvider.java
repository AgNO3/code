/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.fileshare.service.AuditReaderService;
import eu.agno3.fileshare.service.BrowseService;
import eu.agno3.fileshare.service.ChunkUploadService;
import eu.agno3.fileshare.service.ConfigurationProvider;
import eu.agno3.fileshare.service.DirectoryService;
import eu.agno3.fileshare.service.EntityService;
import eu.agno3.fileshare.service.FlaggingService;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.PreferenceService;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.service.RegistrationService;
import eu.agno3.fileshare.service.ShareService;
import eu.agno3.fileshare.service.ShortcutService;
import eu.agno3.fileshare.service.UploadService;
import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.fileshare.service.gui.GuiServiceContext;
import eu.agno3.runtime.http.ua.UADetector;
import eu.agno3.runtime.i18n.ResourceBundleService;
import eu.agno3.runtime.security.password.PasswordGenerator;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.ratelimit.LoginRateLimiter;
import eu.agno3.runtime.security.terms.TermsService;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;


/**
 * @author mbechler
 *
 */
@RequestScoped
public class FileshareServiceProvider {

    @Inject
    @OsgiService ( timeout = 200 )
    private GuiServiceContext ctx;

    @Inject
    @OsgiService ( timeout = 200 )
    private UADetector uaDetector;

    @Inject
    @OsgiService ( timeout = 200 )
    private TokenGenerator tokenGenerator;

    @Inject
    @OsgiService ( timeout = 200 )
    private PasswordPolicyChecker passwordPolicy;

    @Inject
    @OsgiService ( timeout = 200 )
    private ResourceBundleService resouceBundleService;

    @Inject
    @OsgiService ( timeout = 200 )
    private LoginRateLimiter loginRateLimiter;

    @Inject
    @OsgiService ( timeout = 200 )
    private PasswordGenerator passwordGenerator;

    @Inject
    @OsgiService ( timeout = 400 )
    private TermsService termsService;


    /**
     * @return the browseService
     */
    public BrowseService getBrowseService () {
        return this.ctx.getBrowseService();
    }


    /**
     * @return the directoryService
     * 
     */
    public DirectoryService getDirectoryService () {
        return this.ctx.getDirectoryService();
    }


    /**
     * @return the entityService
     */
    public EntityService getEntityService () {
        return this.ctx.getEntityService();
    }


    /**
     * @return the group service
     * 
     */
    public GroupServiceMBean getGroupService () {
        return this.ctx.getGroupService();
    }


    /**
     * @return the user service
     */
    public UserServiceMBean getUserService () {
        return this.ctx.getUserService();
    }


    /**
     * @return the subject service
     */
    public SubjectServiceMBean getSubjectService () {
        return this.ctx.getSubjectService();
    }


    /**
     * @return the share service
     */
    public ShareService getShareService () {
        return this.ctx.getShareService();
    }


    /**
     * @return the configuration provider
     */
    public ConfigurationProvider getConfigurationProvider () {
        return this.ctx.getConfigurationProvider();
    }


    /**
     * 
     * @return the chunk upload service
     */
    public ChunkUploadService getChunkUploadService () {
        return this.ctx.getChunkUploadService();
    }


    /**
     * @return the user agent detector
     */
    public UADetector getUserAgentDetector () {
        return this.uaDetector;
    }


    /**
     * @return the tokenGenerator
     */
    public TokenGenerator getTokenGenerator () {
        return this.tokenGenerator;
    }


    /**
     * @return the preference service
     */
    public PreferenceService getPreferenceService () {
        return this.ctx.getPreferenceService();
    }


    /**
     * @return the upload service
     */
    public UploadService getUploadService () {
        return this.ctx.getUploadService();
    }


    /**
     * @return the link service
     */
    public LinkService getLinkService () {
        return this.ctx.getLinkService();
    }


    /**
     * @return the registration service
     */
    public RegistrationService getRegistrationService () {
        return this.ctx.getRegistrationService();
    }


    /**
     * @return the passwordPolicy
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.passwordPolicy;
    }


    /**
     * @return the resouceBundleService
     */
    public ResourceBundleService getResouceBundleService () {
        return this.resouceBundleService;
    }


    /**
     * @return the quota service
     * 
     */
    public QuotaService getQuotaService () {
        return this.ctx.getQuotaService();
    }


    /**
     * 
     * @return the flagging service
     */
    public FlaggingService getFlaggingService () {
        return this.ctx.getFlaggingService();
    }


    /**
     * @return the shortcut service
     * 
     */
    public ShortcutService getShortcutService () {
        return this.ctx.getShortcutService();
    }


    /**
     * @return the login rate limiter
     */
    public LoginRateLimiter getLoginRateLimiter () {
        return this.loginRateLimiter;
    }


    /**
     * @return the passwordGenerator
     */
    public PasswordGenerator getPasswordGenerator () {
        return this.passwordGenerator;
    }


    /**
     * @return the audit reader service
     */
    public AuditReaderService getAuditReaderService () {
        return this.ctx.getAuditReaderService();
    }


    /**
     * @return the termsService
     */
    public TermsService getTermsService () {
        return this.termsService;
    }
}
