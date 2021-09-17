/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.users.UserDetailsCacheBean;


/**
 * @author mbechler
 *
 */
@Named ( "subjectShareBean" )
@ViewScoped
public class SubjectShareBean extends AbstractSharesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 778181178555162731L;

    private static final Logger log = Logger.getLogger(SubjectShareBean.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private UserDetailsCacheBean userDetails;

    @Inject
    protected CurrentUserBean currentUser;

    @Inject
    protected ShareTabsBean shareTabs;

    private SubjectInfo shareTo;

    private boolean sendNotification;


    @Override
    @PostConstruct
    public void init () {
        if ( this.shareTabs.getPeerInfo() instanceof SubjectPeerInfo ) {
            this.shareTo = ( (SubjectPeerInfo) this.shareTabs.getPeerInfo() ).getSubject();
        }
        super.init();
    }


    /**
     * @param query
     * @return completion results
     */
    public List<SubjectQueryResult> completeSubjects ( String query ) {
        try {
            List<SubjectQueryResult> s = this.fsp.getSubjectService().querySubjects(query, 21);
            s.remove(SubjectQueryResult.fromSubject(this.currentUser.getCurrentUser()));
            return s;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * 
     * @return whether this user/group can be notified by mail
     */
    public boolean getCanSendNotification () {

        if ( this.fsp.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return false;
        }

        if ( this.shareTo instanceof UserInfo ) {
            return !StringUtils.isBlank(this.userDetails.getUserDetails(this.shareTo.getId()).getMailAddress());
        }
        else if ( this.shareTo instanceof GroupInfo ) {
            return true;
        }

        return false;
    }


    /**
     * @return the sendNotification
     */
    public boolean getSendNotification () {
        return this.sendNotification;
    }


    /**
     * @param sendNotification
     *            the sendNotification to set
     */
    public void setSendNotification ( boolean sendNotification ) {
        this.sendNotification = sendNotification;
    }


    /**
     * @return the notificationText
     */
    public String getMessage () {
        return this.getShareProperties().getMessage();
    }


    /**
     * @param notificationText
     *            the notificationText to set
     */
    public void setMessage ( String notificationText ) {
        this.getShareProperties().setMessage(notificationText);
    }


    /**
     * 
     * @return the notification subject
     */
    public String getSubject () {

        if ( this.getShareProperties().getNotificationSubject() == null ) {
            try {
                this.getShareProperties().setNotificationSubject(
                    this.fsp.getShareService().getSubjectShareSubject(
                        getSelectedEntity().getEntityKey(),
                        this.shareTo != null ? this.shareTo.getId() : null,
                        getShareProperties()));
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                this.getShareProperties().setNotificationSubject(StringUtils.EMPTY);
            }
        }
        return this.getShareProperties().getNotificationSubject();
    }


    /**
     * 
     * @param ev
     */
    public void invitedReturn ( SelectEvent ev ) {
        if ( ev == null || ev.getObject() == null ) {
            return;
        }
        log.debug("Invited " + ev.getObject()); //$NON-NLS-1$
        this.setShareTo((SubjectQueryResult) ev.getObject());
    }


    /**
     * 
     * @param subject
     */
    public void setSubject ( String subject ) {
        this.getShareProperties().setNotificationSubject(subject);
    }


    /**
     * @return the shareTo
     */
    public SubjectInfo getShareTo () {
        return this.shareTo;
    }


    /**
     * @param shareTo
     *            the shareTo to set
     */
    public void setShareTo ( SubjectQueryResult shareTo ) {
        this.shareTo = shareTo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.share.AbstractSharesBean#reset()
     */
    @Override
    public void reset () {
        super.reset();
        this.shareTo = null;
        this.sendNotification = false;
    }

}
