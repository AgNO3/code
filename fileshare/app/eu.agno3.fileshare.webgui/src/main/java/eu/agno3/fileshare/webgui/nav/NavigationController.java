/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.nav;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.shared.util.ViewProtectionUtils;
import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.model.shortcut.Shortcut;
import eu.agno3.fileshare.webgui.service.file.UserTokenBean;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.subject.UserFavoritesBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "navigationController" )
public class NavigationController {

    private static final Logger log = Logger.getLogger(NavigationController.class);

    @Inject
    private FileRootSelectionBean fileRoot;

    @Inject
    private UserFavoritesBean userFavorites;

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private UserTokenBean userToken;


    /**
     * @return outcome
     */
    public String userManagement () {
        return "/admin/manageUsers.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * @return outcome
     */
    public String groupManagement () {
        return "/admin/manageGroups.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * @return outcome
     */
    public String files () {
        return "/index.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * @param ev
     */
    public void refresh ( SelectEvent ev ) {
        try {
            String redirectTo = getCurrentUri();
            if ( log.isDebugEnabled() ) {
                log.debug("Redirecting to " + redirectTo); //$NON-NLS-1$
            }
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectTo);
        }
        catch ( IOException e ) {
            log.warn("Failed to redirect user", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the uri of the current view
     * @throws UnsupportedEncodingException
     */
    public String getCurrentUri () throws UnsupportedEncodingException {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();

        String viewId = fc.getViewRoot().getViewId();

        StringBuilder sb = new StringBuilder();
        String cs = "UTF-8"; //$NON-NLS-1$

        if ( !StringUtils.isBlank(this.fileRoot.getSelectedType()) ) {
            sb.append("type="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.fileRoot.getSelectedType(), cs));
            sb.append('&');
        }

        if ( !StringUtils.isBlank(this.fileRoot.getSelectedRootType()) ) {
            sb.append("rootType="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.fileRoot.getSelectedRootType(), cs));
            sb.append('&');
        }

        if ( !StringUtils.isBlank(this.fileRoot.getSelectedRootIdEncoded()) ) {
            sb.append("root="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.fileRoot.getSelectedRootIdEncoded(), cs));
            sb.append('&');
        }

        if ( this.fileRoot.getSelectedRootGrantId() != null ) {
            sb.append("grant="); //$NON-NLS-1$
            sb.append(this.fileRoot.getSelectedRootGrantId());
            sb.append('&');
        }

        if ( !StringUtils.isEmpty(this.fileRoot.getQuery()) ) {
            sb.append("query="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.fileRoot.getQuery(), cs));
            sb.append('&');
        }

        if ( ViewProtectionUtils.isViewProtected(fc, viewId) ) {
            sb.append(ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM);
            sb.append('=');
            sb.append(URLEncoder.encode(fc.getRenderKit().getResponseStateManager().getCryptographicallyStrongTokenFromSession(fc), cs));
            sb.append('&');
        }

        String token = this.userToken.getTokenQueryArg();
        if ( !StringUtils.isEmpty(token) ) {
            sb.append(token);
        }

        if ( sb.length() > 0 && sb.charAt(sb.length() - 1) == '&' ) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String queryString = sb.toString();
        return request.getRequestURI() + ( !StringUtils.isEmpty(queryString) ? '?' + queryString : StringUtils.EMPTY );
    }


    /**
     * 
     * @param rootGrant
     * @return outcome
     */
    public String shareRootFiles ( Grant rootGrant ) {
        return String.format(
            "/index.xhtml?faces-redirect=true&type=share-root&rootType=share-root&root=%s&grant=%s%s", //$NON-NLS-1$
            rootGrant.getEntity().getEntityKey(),
            rootGrant.getId(),
            this.userToken.getTokenQueryArg());
    }


    /**
     * @return outcome
     */
    public String groups () {
        return "/index.xhtml?faces-redirect=true&type=groups-root&rootType=groups-root"; //$NON-NLS-1$
    }


    /**
     * 
     * @return outcome
     */
    public String peers () {
        return "/index.xhtml?faces-redirect=true&type=peers-root&rootType=peers-root"; //$NON-NLS-1$
    }


    /**
     * 
     * @return outcome
     */
    public String favorites () {
        return "/index.xhtml?faces-redirect=true&type=favorites&rootType=favorites"; //$NON-NLS-1$
    }


    /**
     * 
     * @return outcome
     */
    public String allSharedFiles () {
        return "/index.xhtml?faces-redirect=true&type=all-shared&rootType=all-shared"; //$NON-NLS-1$
    }


    /**
     * 
     * @param sc
     * @return outcome
     */
    public String shortcut ( Shortcut sc ) {

        if ( sc == null ) {
            return null;
        }

        switch ( sc.getType() ) {
        case MEMBER_GROUP:
            return "/index.xhtml?faces-redirect=true&type=group-root&rootType=group-root&root=" + sc.getTargetId(); //$NON-NLS-1$
        case FAVORITE:
            return favorite(sc);
        case PEER:
        case PEER_GROUP:
            return "/index.xhtml?faces-redirect=true&type=peer-root&rootType=peers-root&peer=subject-" + sc.getTargetId(); //$NON-NLS-1$
        case PEER_MAIL:
            return "/index.xhtml?faces-redirect=true&type=peer-root&rootType=peers-root&peer=mail-" + sc.getLabel(); //$NON-NLS-1$
        case PEER_LINK:
            return "/index.xhtml?faces-redirect=true&type=peer-root&rootType=peers-root&peer=token"; //$NON-NLS-1$
        default:
            return null;
        }
    }


    /**
     * 
     * @param sc
     * @return null
     */
    public String removeShortcut ( Shortcut sc ) {
        if ( sc == null ) {
            return null;
        }

        switch ( sc.getType() ) {
        case MEMBER_GROUP:
        case PEER:
        case PEER_GROUP:
            this.userFavorites.removeSubjectIdFavorite(sc.getSubjectId());
            return null;
        case PEER_MAIL:
            this.userFavorites.removeMailFavorite(sc.getLabel());
            return null;
        case PEER_LINK:
            this.userFavorites.unmarkLinksFavorite();
            return null;
        default:
            return null;
        }
    }


    /**
     * 
     * @param sc
     * @return whether the shortcut should be selected
     */
    public boolean isShortcutSelected ( Shortcut sc ) {
        if ( sc == null ) {
            return false;
        }

        EntityKey targetId = sc.getTargetId();
        UUID uuid = targetId != null ? ( (NativeEntityKey) targetId ).getId() : null;
        switch ( sc.getType() ) {
        case MEMBER_GROUP:
            return FileTreeConstants.GROUP_ROOT_TYPE.equals(this.fileRoot.getSelectedRootType())
                    && ( this.fileRoot.getSubjectRootContainer() != null && this.fileRoot.getSubjectRootContainer().getEntityKey().equals(targetId) );
        case PEER:
        case PEER_GROUP:
            if ( FileTreeConstants.SHARE_ROOT_TYPE.equals(this.fileRoot.getSelectedRootType()) ) {
                return this.fileRoot.getRootContainer() != null && this.fileRoot.getRootContainer().getOwner().getId().equals(uuid);
            }
            if ( FileTreeConstants.PEER_ROOT_TYPE.equals(this.fileRoot.getSelectedType())
                    && this.fileRoot.getPeerInfo() instanceof SubjectPeerInfo ) {
                return ( (SubjectPeerInfo) this.fileRoot.getPeerInfo() ).getSubject().getId().equals(uuid);
            }
            return false;
        case PEER_MAIL:
            if ( FileTreeConstants.PEER_ROOT_TYPE.equals(this.fileRoot.getSelectedType()) && this.fileRoot.getPeerInfo() instanceof MailPeerInfo ) {
                return ( (MailPeerInfo) this.fileRoot.getPeerInfo() ).getMailAddress().equals(sc.getLabel());
            }
            return false;
        case PEER_LINK:
            return FileTreeConstants.PEER_ROOT_TYPE.equals(this.fileRoot.getSelectedType()) && this.fileRoot.getPeerInfo() instanceof TokenPeerInfo;
        default:
            return false;
        }

    }


    /**
     * 
     * @return whether any peer favorite is selected
     */
    public boolean isPeerFavoriteSelected () {

        PeerInfo pi = this.fileRoot.getPeerInfo();
        if ( pi != null ) {
            if ( pi instanceof SubjectPeerInfo ) {
                return this.userFavorites.isSubjectFavorite( ( (SubjectPeerInfo) pi ).getSubject());
            }
            else if ( pi instanceof MailPeerInfo ) {
                return this.userFavorites.isMailFavorite( ( (MailPeerInfo) pi ).getMailAddress());
            }
            else if ( pi instanceof TokenPeerInfo ) {
                return this.currentUser.getCurrentUser().getLinksFavorite();
            }
        }

        Grant g = this.fileRoot.getRootGrant();

        if ( g != null && this.userFavorites.isSubjectFavorite(g.getEntity().getOwner()) ) {
            return true;
        }

        return false;
    }


    /**
     * @param sc
     * @return outcome
     */
    public String favorite ( Shortcut sc ) {
        this.userFavorites.trackEntityFavoriteUsage(sc.getTargetId());
        if ( sc.getGrantId() != null ) {
            return String.format("/index.xhtml?faces-redirect=true&type=dir&rootType=share-root&root=%s&grant=%s", sc.getTargetId(), sc.getGrantId()); //$NON-NLS-1$
        }
        else if ( sc.isFromGroup() ) {
            return String.format("/index.xhtml?faces-redirect=true&type=dir&rootType=group-root&root=%s", sc.getTargetId()); //$NON-NLS-1$
        }

        return String.format("/index.xhtml?faces-redirect=true&type=dir&rootType=user-root&root=%s", sc.getTargetId()); //$NON-NLS-1$
    }


    /**
     * @param at
     * @param item
     * @return the style class to add to the menu item
     */
    public String getStyleClass ( String at, String item ) {
        if ( at == null ) {
            return null;
        }

        if ( at.equals(item) ) {
            return "ui-state-active"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * @param cond
     * @return ui-state-active if condition is true
     */
    public String getSelectedClassIf ( Object cond ) {

        if ( ! ( cond instanceof Boolean ) || ! ( (boolean) cond ) ) {
            return StringUtils.EMPTY;
        }

        return "ui-state-active"; //$NON-NLS-1$
    }

}
