/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.security.LocalUserService;
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


/**
 * @author mbechler
 *
 */
@Component ( service = GuiServiceContext.class )
public class GuiServiceContextImpl implements GuiServiceContext {

    private BrowseService browseService;

    private DirectoryService directoryService;

    private EntityService entityService;

    private LocalUserService localUserService;

    private GroupServiceMBean groupService;

    private UserServiceMBean userService;

    private SubjectServiceMBean subjectService;

    private ShareService shareService;

    private ConfigurationProvider configurationProvider;

    private PreferenceService preferenceService;

    private UploadService uploadService;

    private LinkService linkService;

    private RegistrationService registrationService;

    private QuotaService quotaService;

    private FlaggingService flaggingService;

    private ShortcutService shortcutService;

    private ChunkUploadService chunkUploadService;

    private AuditReaderService auditReaderService;


    @Reference
    protected synchronized void setBrowseService ( BrowseService bs ) {
        this.browseService = bs;
    }


    protected synchronized void unsetBrowseService ( BrowseService bs ) {
        if ( this.browseService == bs ) {
            this.browseService = null;
        }
    }


    @Reference
    protected synchronized void setDirectoryService ( DirectoryService ds ) {
        this.directoryService = ds;
    }


    protected synchronized void unsetDirectoryService ( DirectoryService ds ) {
        if ( this.directoryService == ds ) {
            this.directoryService = null;
        }
    }


    @Reference
    protected synchronized void setEntityService ( EntityService es ) {
        this.entityService = es;
    }


    protected synchronized void unsetEntityService ( EntityService es ) {
        if ( this.entityService == es ) {
            this.entityService = null;
        }
    }


    @Reference
    protected synchronized void setLocalUserService ( LocalUserService lus ) {
        this.localUserService = lus;
    }


    protected synchronized void unsetLocalUserService ( LocalUserService lus ) {
        if ( this.localUserService == lus ) {
            this.localUserService = null;
        }
    }


    @Reference
    protected synchronized void setGroupService ( GroupServiceMBean gs ) {
        this.groupService = gs;
    }


    protected synchronized void unsetGroupService ( GroupServiceMBean gs ) {
        if ( this.groupService == gs ) {
            this.groupService = null;
        }
    }


    @Reference
    protected synchronized void setUserService ( UserServiceMBean us ) {
        this.userService = us;
    }


    protected synchronized void unsetUserService ( UserServiceMBean us ) {
        if ( this.userService == us ) {
            this.userService = null;
        }
    }


    @Reference
    protected synchronized void setSubjectService ( SubjectServiceMBean ss ) {
        this.subjectService = ss;
    }


    protected synchronized void unsetSubjectService ( SubjectServiceMBean ss ) {
        if ( this.subjectService == ss ) {
            this.subjectService = null;
        }
    }


    @Reference
    protected synchronized void setShareService ( ShareService ss ) {
        this.shareService = ss;
    }


    protected synchronized void unsetShareService ( ShareService ss ) {
        if ( this.shareService == ss ) {
            this.shareService = null;
        }
    }


    @Reference
    protected synchronized void setConfigurationProvider ( ConfigurationProvider cp ) {
        this.configurationProvider = cp;
    }


    protected synchronized void unsetConfigurationProvider ( ConfigurationProvider cp ) {
        if ( this.configurationProvider == cp ) {
            this.configurationProvider = null;
        }
    }


    @Reference
    protected synchronized void setPreferenceService ( PreferenceService ps ) {
        this.preferenceService = ps;
    }


    protected synchronized void unsetPreferenceService ( PreferenceService ps ) {
        if ( this.preferenceService == ps ) {
            this.preferenceService = null;
        }
    }


    @Reference
    protected synchronized void setUploadService ( UploadService us ) {
        this.uploadService = us;
    }


    protected synchronized void unsetUploadService ( UploadService us ) {
        if ( this.uploadService == us ) {
            this.uploadService = null;
        }
    }


    @Reference
    protected synchronized void setLinkService ( LinkService ls ) {
        this.linkService = ls;
    }


    protected synchronized void unsetLinkService ( LinkService ls ) {
        if ( this.linkService == ls ) {
            this.linkService = null;
        }
    }


    @Reference
    protected synchronized void setRegistrationService ( RegistrationService rs ) {
        this.registrationService = rs;
    }


    protected synchronized void unsetRegistrationService ( RegistrationService rs ) {
        if ( this.registrationService == rs ) {
            this.registrationService = null;
        }
    }


    @Reference
    protected synchronized void setQuotaService ( QuotaService qs ) {
        this.quotaService = qs;
    }


    protected synchronized void unsetQuotaService ( QuotaService qs ) {
        if ( this.quotaService == qs ) {
            this.quotaService = null;
        }
    }


    @Reference
    protected synchronized void setFlaggingService ( FlaggingService fs ) {
        this.flaggingService = fs;
    }


    protected synchronized void unsetFlaggingService ( FlaggingService fs ) {
        if ( this.flaggingService == fs ) {
            this.flaggingService = null;
        }
    }


    @Reference
    protected synchronized void setShortcutService ( ShortcutService ss ) {
        this.shortcutService = ss;
    }


    protected synchronized void unsetShortcutService ( ShortcutService ss ) {
        if ( this.shortcutService == ss ) {
            this.shortcutService = null;
        }
    }


    @Reference
    protected synchronized void setChunkUploadService ( ChunkUploadService cus ) {
        this.chunkUploadService = cus;
    }


    protected synchronized void unsetChunkUploadService ( ChunkUploadService cus ) {
        if ( this.chunkUploadService == cus ) {
            this.chunkUploadService = null;
        }
    }


    @Reference
    protected synchronized void setAuditReaderService ( AuditReaderService ars ) {
        this.auditReaderService = ars;
    }


    protected synchronized void unsetAuditReaderService ( AuditReaderService ars ) {
        if ( this.auditReaderService == ars ) {
            this.auditReaderService = ars;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getBrowseService()
     */
    @Override
    public BrowseService getBrowseService () {
        return this.browseService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getDirectoryService()
     */
    @Override
    public DirectoryService getDirectoryService () {
        return this.directoryService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getEntityService()
     */
    @Override
    public EntityService getEntityService () {
        return this.entityService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getLocalUserService()
     */
    @Override
    public LocalUserService getLocalUserService () {
        return this.localUserService;
    }


    /**
     * @return the groupService
     */
    @Override
    public GroupServiceMBean getGroupService () {
        return this.groupService;
    }


    @Override
    public UserServiceMBean getUserService () {
        return this.userService;
    }


    /**
     * @return the subjectService
     */
    @Override
    public SubjectServiceMBean getSubjectService () {
        return this.subjectService;
    }


    /**
     * @return the shareService
     */
    @Override
    public ShareService getShareService () {
        return this.shareService;
    }


    /**
     * @return the configurationProvider
     */
    @Override
    public ConfigurationProvider getConfigurationProvider () {
        return this.configurationProvider;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getPreferenceService()
     */
    @Override
    public PreferenceService getPreferenceService () {
        return this.preferenceService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getUploadService()
     */
    @Override
    public UploadService getUploadService () {
        return this.uploadService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getLinkService()
     */
    @Override
    public LinkService getLinkService () {
        return this.linkService;
    }


    /**
     * @return the registrationService
     */
    @Override
    public RegistrationService getRegistrationService () {
        return this.registrationService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getQuotaService()
     */
    @Override
    public QuotaService getQuotaService () {
        return this.quotaService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getFlaggingService()
     */
    @Override
    public FlaggingService getFlaggingService () {
        return this.flaggingService;
    }


    /**
     * @return the shortcutService
     */
    @Override
    public ShortcutService getShortcutService () {
        return this.shortcutService;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.gui.GuiServiceContext#getChunkUploadService()
     */
    @Override
    public ChunkUploadService getChunkUploadService () {
        return this.chunkUploadService;
    }


    /**
     * @return the auditReaderService
     */
    @Override
    public AuditReaderService getAuditReaderService () {
        return this.auditReaderService;
    }
}
