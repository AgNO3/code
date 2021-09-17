/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.ViewProtectionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.joda.time.DateTime;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.tokens.AccessToken;
import eu.agno3.fileshare.model.tokens.AnonymousGrantToken;
import eu.agno3.fileshare.model.tokens.SessionIntentToken;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.runtime.jsf.types.uri.URIUtil;
import eu.agno3.runtime.security.token.RealmTokenToken;
import eu.agno3.runtime.security.web.login.token.TokenCreationException;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fileDownloadController" )
public class FileDownloadController {

    private static final Logger log = Logger.getLogger(FileDownloadController.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private UserTokenBean userToken;


    /**
     * @param file
     * @param token
     * @return the download link
     */
    public String getDownloadLink ( VFSFileEntity file, Object token ) {
        try {
            return this.fsp.getLinkService().makeDownloadLink(file, makeTokenArg(token), URIUtil.getCurrentBaseUri(), false);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * 
     * @param file
     * @param g
     * @param token
     * @return null
     */
    public String download ( VFSFileEntity file, Grant g, Object token ) {
        String downloadLink = getDownloadLinkWithIntent(file, g, token);
        if ( downloadLink == null ) {
            return null;
        }

        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Redirect to download url " + downloadLink); //$NON-NLS-1$
            }
            FacesContext.getCurrentInstance().getExternalContext().redirect(downloadLink);
        }
        catch ( IOException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param file
     * @param g
     * @param token
     * @return the download link, only use if the user has shown intent to open the file
     */
    public String getDownloadLinkWithIntent ( VFSFileEntity file, Grant g, Object token ) {
        Object realToken = wrapTokenWithIntent(token, g);

        String downloadLink;
        try {
            downloadLink = this.fsp.getLinkService().makeDownloadLink(file, makeTokenArg(realToken), URIUtil.getCurrentBaseUri(), false);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
        return downloadLink;
    }


    /**
     * 
     * @param selection
     * @param g
     * @param token
     * @return null
     */
    public String downloadAll ( FileSelectionBean selection, Grant g, Object token ) {

        TreeNode[] sel = selection.getSelection();
        if ( sel == null || sel.length == 0 ) {
            return null;
        }

        Set<VFSEntity> entities = new HashSet<>();

        for ( TreeNode n : sel ) {
            if ( ! ( n instanceof EntityTreeNode ) ) {
                continue;
            }
            EntityTreeNode etn = (EntityTreeNode) n;
            entities.add(etn.getAttachedObject());
        }

        String downloadLink = getDownloadAllLinkWithIntent(entities, ArchiveType.ZIP, g, token);

        if ( downloadLink == null ) {
            return null;
        }

        if ( downloadLink.length() >= 8100 ) {
            ExceptionHandler.addMessage(FacesMessage.SEVERITY_ERROR, "errors.tooManyFiles"); //$NON-NLS-1$
            return null;
        }

        try {
            log.debug("Redirect to download all url"); //$NON-NLS-1$
            FacesContext.getCurrentInstance().getExternalContext().redirect(downloadLink);
        }
        catch ( IOException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param selection
     * @param t
     * @param g
     * @param token
     * @return null
     */
    public String downloadAll ( URLFileSelectionBean selection, ArchiveType t, Grant g, Object token ) {
        Set<VFSEntity> dedup = new HashSet<>(selection.getMultiSelection());
        String downloadLink = getDownloadAllLinkWithIntent(dedup, t, g, token);
        if ( downloadLink == null ) {
            return null;
        }

        if ( downloadLink.length() >= 8100 ) {
            ExceptionHandler.addMessage(FacesMessage.SEVERITY_ERROR, "errors.tooManyFiles"); //$NON-NLS-1$
            return null;
        }

        try {
            log.debug("Redirect to download all url"); //$NON-NLS-1$
            FacesContext.getCurrentInstance().getExternalContext().redirect(downloadLink);
        }
        catch ( IOException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param entities
     * @param object
     * @param token
     * @return
     */
    private String getDownloadAllLinkWithIntent ( Set<VFSEntity> entities, ArchiveType type, Grant g, Object token ) {
        Object realToken = wrapTokenWithIntent(token, g);

        String downloadLink;
        try {
            downloadLink = this.fsp.getLinkService().makeDownloadAllLink(entities, type, makeTokenArg(realToken), URIUtil.getCurrentBaseUri());
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
        return downloadLink;
    }


    /**
     * @param file
     * @param g
     * @param token
     * @return the view link
     */
    public String getViewLink ( VFSFileEntity file, Grant g, Object token ) {
        if ( file == null ) {
            return null;
        }

        Object realToken = wrapTokenWithIntent(token, g);

        try {
            return this.fsp.getLinkService().makeBackendViewLink(file, makeTokenArg(realToken), URIUtil.getCurrentBaseUri());
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * @param file
     * @param token
     * @return the view link
     */
    public String getViewLinkWithoutIntent ( VFSFileEntity file, Object token ) {
        if ( file == null ) {
            return null;
        }

        try {
            return this.fsp.getLinkService().makeBackendViewLink(file, makeTokenArg(token), URIUtil.getCurrentBaseUri());
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * @param token
     * @param g
     * @return a token query
     */
    public String makeTokenWithIntent ( Object token, Grant g ) {
        return makeTokenArg(wrapTokenWithIntent(token, g));

    }


    /**
     * 
     * @param container
     * @param archiveType
     * @param token
     * @return the directory archive link
     */
    public String getDirectoryArchiveLink ( VFSContainerEntity container, ArchiveType archiveType, Object token ) {
        try {
            return this.fsp.getLinkService()
                    .makeDirectoryArchiveLink(container, archiveType, makeTokenArg(token), URIUtil.getCurrentBaseUri(), false);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * @param tn
     * @param contextType
     * @return the view URL
     * @throws UnsupportedEncodingException
     */
    public String makePreviewURL ( TreeNode tn, String contextType ) throws UnsupportedEncodingException {

        if ( !FileDisplayBean.isFileNode(tn) ) {
            return null;
        }
        EntityTreeNode en = (EntityTreeNode) tn;

        String grantString = StringUtils.EMPTY;
        String viewProtString = StringUtils.EMPTY;
        String contextString = StringUtils.EMPTY;
        String view = "/actions/viewFileDialog.xhtml"; //$NON-NLS-1$

        String cs = "UTF-8"; //$NON-NLS-1$
        FacesContext fc = FacesContext.getCurrentInstance();
        if ( ViewProtectionUtils.isViewProtected(fc, view) ) { // $NON-NLS-1$
            viewProtString = "&" + ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM + '=' //$NON-NLS-1$
                    + URLEncoder.encode(fc.getRenderKit().getResponseStateManager().getCryptographicallyStrongTokenFromSession(fc), cs);
        }
        if ( en.getGrant() != null ) {
            grantString = "&grant=" + en.getGrant().getId(); //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(contextType) ) {
            contextString = "&rootType=" + URLEncoder.encode(contextType, cs); //$NON-NLS-1$
        }

        String u = String.format(
            "%s%s?file=%s&closable=true&preview=true%s%s%s%s", //$NON-NLS-1$
            fc.getExternalContext().getRequestContextPath(),
            view,
            en.getAttachedObject().getEntityKey().toString(),
            grantString,
            contextString,
            viewProtString,
            this.userToken.getTokenQueryArg());
        return u;
    }


    /**
     * @param file
     * @param g
     * @param archiveType
     * @param token
     * @return null
     */
    public String downloadDirectory ( VFSContainerEntity file, Grant g, ArchiveType archiveType, Object token ) {

        Object realToken = wrapTokenWithIntent(token, g);

        String downloadLink;
        try {
            downloadLink = this.fsp.getLinkService()
                    .makeDirectoryArchiveLink(file, archiveType, makeTokenArg(realToken), URIUtil.getCurrentBaseUri(), false);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(downloadLink);
        }
        catch ( IOException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param token
     * @return
     */
    private static String makeTokenArg ( Object token ) {

        if ( token == null ) {
            return "?"; //$NON-NLS-1$
        }

        if ( token instanceof UserTokenBean ) {
            return ( (UserTokenBean) token ).getTokenQueryString();
        }
        else if ( token instanceof String ) {
            return "?token=" + token; //$NON-NLS-1$
        }

        return "?"; //$NON-NLS-1$
    }


    /**
     * @param token
     * @return
     */
    private Object wrapTokenWithIntent ( Object token, Grant g ) {

        String oldTokenVal = null;

        if ( token instanceof UserTokenBean ) {
            oldTokenVal = ( (UserTokenBean) token ).getToken();
        }
        else if ( token instanceof String ) {
            oldTokenVal = (String) token;
        }

        Serializable sessionId = SecurityUtils.getSubject().getSession().getId();
        AccessToken wrapped = null;
        if ( oldTokenVal != null ) {
            log.debug("Wrapping anonymous token"); //$NON-NLS-1$
            wrapped = wrapAnonToken(oldTokenVal, sessionId, wrapped, g);
        }
        else {
            log.debug("Creating session intent token"); //$NON-NLS-1$
            wrapped = new SessionIntentToken(sessionId);
        }

        try {
            DateTime expiry = DateTime.now().plus(this.fsp.getConfigurationProvider().getFrontendConfiguration().getIntentTimeout());
            if ( log.isDebugEnabled() ) {
                log.debug("Creating intent token with expiry " + expiry); //$NON-NLS-1$
            }
            return this.fsp.getTokenGenerator().createToken(wrapped, expiry);

        }
        catch ( TokenCreationException e ) {
            log.warn("Failed to create token with intent", e); //$NON-NLS-1$
            ExceptionHandler.handleException(e);
        }
        return token;

    }


    /**
     * @param oldTokenVal
     * @param sessionId
     * @param wrapped
     * @param g
     * @return
     */
    private AccessToken wrapAnonToken ( String oldTokenVal, Serializable sessionId, AccessToken wrapped, Grant g ) {
        RealmTokenToken tok = RealmTokenToken.createFromString(oldTokenVal);
        if ( tok == null ) {
            return wrapped;
        }

        PrincipalCollection validate = this.fsp.getTokenGenerator().validate(tok);
        TokenPrincipal validated = validate.oneByType(TokenPrincipal.class);

        if ( ! ( validated.getData() instanceof AnonymousGrantToken ) ) {
            log.warn("Not an anonymous grant token"); //$NON-NLS-1$
            return wrapped;
        }

        AnonymousGrantToken agt = (AnonymousGrantToken) validated.getData();
        return new AnonymousGrantToken(agt.getNonce(), sessionId, getGrantPassword(g));
    }


    /**
     * @param g
     */
    private static String getGrantPassword ( Grant g ) {
        if ( ! ( g instanceof TokenGrant ) ) {
            log.warn("Did not find token grant " + g); //$NON-NLS-1$
        }
        else if ( ( g instanceof TokenGrant ) && ( (TokenGrant) g ).getPasswordProtected() ) {
            log.debug("Grant is password protected"); //$NON-NLS-1$
            Session s = SecurityUtils.getSubject().getSession(false);
            if ( s != null ) {
                String grantPw = (String) s.getAttribute("grantpw_" + g.getId()); //$NON-NLS-1$
                if ( !StringUtils.isBlank(grantPw) ) {
                    return grantPw;
                }
            }

        }
        return null;
    }
}
