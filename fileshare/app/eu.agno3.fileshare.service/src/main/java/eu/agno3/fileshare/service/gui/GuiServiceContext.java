/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.gui;


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


/**
 * @author mbechler
 *
 */
public interface GuiServiceContext {

    /**
     * @return the browseService
     */
    BrowseService getBrowseService ();


    /**
     * @return the directoryService
     * 
     */
    DirectoryService getDirectoryService ();


    /**
     * @return the entityService
     */
    EntityService getEntityService ();


    /**
     * @return the localUserService
     */
    LocalUserService getLocalUserService ();


    /**
     * @return the group service
     */
    GroupServiceMBean getGroupService ();


    /**
     * @return the user service
     */
    UserServiceMBean getUserService ();


    /**
     * @return the subject service
     */
    SubjectServiceMBean getSubjectService ();


    /**
     * @return the share service
     */
    ShareService getShareService ();


    /**
     * @return the configuration provider
     */
    ConfigurationProvider getConfigurationProvider ();


    /**
     * @return the preference service
     */
    PreferenceService getPreferenceService ();


    /**
     * @return the upload service
     */
    UploadService getUploadService ();


    /**
     * @return the link service
     */
    LinkService getLinkService ();


    /**
     * @return the registration service
     */
    RegistrationService getRegistrationService ();


    /**
     * @return the quota service
     */
    QuotaService getQuotaService ();


    /**
     * @return the flagging service
     */
    FlaggingService getFlaggingService ();


    /**
     * @return the shortcut service
     */
    ShortcutService getShortcutService ();


    /**
     * 
     * @return the chunk upload service
     */
    ChunkUploadService getChunkUploadService ();


    /**
     * @return the audit reader service
     */
    AuditReaderService getAuditReaderService ();

}