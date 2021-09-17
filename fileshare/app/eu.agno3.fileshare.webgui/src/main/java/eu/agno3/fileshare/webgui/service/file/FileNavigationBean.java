/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FavoritesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.GroupTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharedByTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharedToTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SearchResultTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareRootEntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareSubjectTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SharedTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "fileNavigationBean" )
@ApplicationScoped
public class FileNavigationBean {

    private static final Logger log = Logger.getLogger(FileNavigationBean.class);

    @Inject
    private UserTokenBean userToken;

    @Inject
    private FileRootSelectionBean rootSelection;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param node
     * @return the single level navigation target for the tree node
     */
    public String getSingleLevelTarget ( BrowseTreeNode node ) {

        String res = null;

        if ( node instanceof EntityTreeNode && node.getParent() instanceof FavoritesTreeNode ) {
            log.debug("Track favorite usage"); //$NON-NLS-1$
            try {
                this.fsp.getFlaggingService().trackEntityFavorityUsage( ( (EntityTreeNode) node ).getAttachedObject().getEntityKey());
            }
            catch ( FileshareException e ) {
                log.debug("Failed to track favorite usage", e); //$NON-NLS-1$
            }
        }

        if ( node instanceof ShareRootEntityTreeNode ) {
            res = getShareRootTarget((ShareRootEntityTreeNode) node);
        }
        else if ( node instanceof EntityTreeNode
                && ( FileTreeConstants.USER_ROOT_TYPE.equals(node.getType()) || FileTreeConstants.GROUP_ROOT_TYPE.equals(node.getType()) ) ) {
            res = getDirectoryTarget((VFSContainerEntity) ( (EntityTreeNode) node ).getAttachedObject(), null, node.getType(), node.getType());
        }
        else if ( node instanceof EntityTreeNode && ( (EntityTreeNode) node ).getAttachedObject() instanceof VFSContainerEntity ) {
            res = getDirectoryTarget(
                (VFSContainerEntity) ( (EntityTreeNode) node ).getAttachedObject(),
                ( (EntityTreeNode) node ).getGrant(),
                node.getType(),
                ( (EntityTreeNode) node ).getOverrideRootType());
        }
        else if ( node instanceof EntityTreeNode && ( (EntityTreeNode) node ).getAttachedObject() instanceof VFSFileEntity ) {
            res = getFileTarget(
                (VFSFileEntity) ( (EntityTreeNode) node ).getAttachedObject(),
                ( (EntityTreeNode) node ).getGrant(),
                node.getType(),
                ( (EntityTreeNode) node ).getOverrideRootType());
        }
        else if ( node instanceof GroupTreeNode || node instanceof SharedTreeNode ) {
            res = getTypeTarget(node.getType());
        }
        else if ( node instanceof ShareSubjectTreeNode ) {
            res = getShareSubjectTarget((ShareSubjectTreeNode) node);
        }
        else if ( node instanceof PeerSharesTreeNode ) {
            res = getPeerSharesTarget((PeerSharesTreeNode) node);
        }
        else if ( node instanceof PeerSharedByTreeNode || node instanceof PeerSharedToTreeNode ) {
            return null;
        }

        if ( res != null ) {
            return res;
        }

        throw new IllegalArgumentException("Unsupported target tree node " + node); //$NON-NLS-1$
    }


    /**
     * @param node
     * @return
     */
    private String getShareRootTarget ( ShareRootEntityTreeNode node ) {

        if ( node.getAttachedObject() instanceof VFSFileEntity ) {
            return getFileTarget(
                (VFSFileEntity) node.getAttachedObject(),
                node.getGrant(),
                FileTreeConstants.SHARE_ROOT_FILE_TYPE,
                FileTreeConstants.SHARE_ROOT_TYPE);
        }
        else if ( node.getAttachedObject() instanceof VFSContainerEntity ) {
            return getDirectoryTarget(
                (VFSContainerEntity) node.getAttachedObject(),
                node.getGrant(),
                FileTreeConstants.DIR_TYPE,
                FileTreeConstants.SHARE_ROOT_TYPE);
        }

        return null;
    }


    /**
     * @param node
     * @return the single level navigation frame target for the tree node
     */
    public String getSingleLevelFrameTarget ( BrowseTreeNode node ) {
        return "_self"; //$NON-NLS-1$
    }


    /**
     * @param node
     * @param g
     * @param type
     * @param rootType
     * @return the directory view outcome
     */
    public String getDirectoryTarget ( VFSContainerEntity node, Grant g, String type, String rootType ) {

        return String.format(
            "/index.xhtml?faces-redirect=true&root=%s&type=%s%s%s%s%s", //$NON-NLS-1$
            node.getEntityKey(),
            type,
            makeGrantQueryArg(g),
            this.userToken.getTokenQueryArg(),
            this.makeSelectedRootTypeArg(rootType),
            makeQueryArg(this.rootSelection.getQuery(), this.rootSelection.getOffset()));
    }


    /**
     * @param node
     * @param g
     * @param type
     * @param rootType
     * @return the file view outcome
     */
    public String getFileTarget ( VFSFileEntity node, Grant g, String type, String rootType ) {
        return String.format(
            "/actions/viewFile.xhtml?faces-redirect=true&file=%s&preview=true&showBack=true%s%s", //$NON-NLS-1$
            node.getEntityKey(),
            makeGrantQueryArg(g),
            this.userToken.getTokenQueryArg());
    }


    /**
     * @return
     */
    private static Object makeQueryArg ( String query, int offset ) {

        if ( StringUtils.isBlank(query) ) {
            return StringUtils.EMPTY;
        }
        try {
            return String.format(
                "&offset=%d&query=%s", //$NON-NLS-1$
                offset,
                query != null ? URLEncoder.encode(query, "UTF-8") : StringUtils.EMPTY);//$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * @return
     */
    private String makeGrantQueryArg ( Grant g ) {

        Grant grant;
        if ( g != null ) {
            grant = g;
        }
        else {
            if ( this.rootSelection.getSelectedRootGrantId() == null ) {
                return StringUtils.EMPTY;
            }
            grant = this.rootSelection.getRootGrant();
        }

        if ( grant != null ) {
            return String.format("&grant=%s", grant.getId()); //$NON-NLS-1$
        }

        return StringUtils.EMPTY;
    }


    private String makeSelectedRootTypeArg ( String rootType ) {

        String effectiveRootType = null;

        if ( !StringUtils.isBlank(rootType) ) {
            effectiveRootType = rootType;
        }
        else if ( !StringUtils.isBlank(this.rootSelection.getSelectedRootType()) ) {
            effectiveRootType = this.rootSelection.getSelectedRootType();
        }

        if ( effectiveRootType != null ) {
            return String.format("&rootType=%s", effectiveRootType); //$NON-NLS-1$
        }

        return StringUtils.EMPTY;
    }


    /**
     * @param type
     * @return outcome for generic virtual root type
     */
    public static String getTypeTarget ( String type ) {
        return String.format("/index.xhtml?faces-redirect=true&type=%s&rootType=%s", type, type); //$NON-NLS-1$
    }


    /**
     * 
     * @param query
     * @param offset
     * @return outcome for search result
     */
    public static String getSearchResultTarget ( String query, int offset ) {
        return String.format(
            "/index.xhtml?faces-redirect=true&type=search-result&rootType=search-result%s", //$NON-NLS-1$
            makeQueryArg(query, offset));

    }


    /**
     * @return next result page
     */
    public String nextResultPage () {
        if ( ! ( this.rootSelection.getRoot() instanceof SearchResultTreeNode ) ) {
            return null;
        }

        SearchResultTreeNode res = (SearchResultTreeNode) this.rootSelection.getRoot();
        if ( !res.getHaveMoreResults() ) {
            return null;
        }
        return getSearchResultTarget(res.getQuery(), res.getOffset() + res.getLimit());
    }


    /**
     * @return previous result page
     */
    public String prevResultPage () {
        if ( ! ( this.rootSelection.getRoot() instanceof SearchResultTreeNode ) ) {
            return null;
        }

        SearchResultTreeNode res = (SearchResultTreeNode) this.rootSelection.getRoot();
        return getSearchResultTarget(res.getQuery(), Math.max(0, res.getOffset() - res.getLimit()));
    }


    /**
     * @param subj
     * @param type
     * @return outcome for show subject's shares
     */
    public static String getShareSubjectTarget ( SubjectInfo subj, String type ) {
        return String.format("/index.xhtml?faces-redirect=true&root=%s&type=%s&rootType=%s", subj.getId(), type, type); //$NON-NLS-1$
    }


    /**
     * @param node
     * @return outcome for show subject's shares
     */
    public static String getShareSubjectTarget ( ShareSubjectTreeNode node ) {
        return getShareSubjectTarget(node.getAttachedObject(), node.getType());
    }


    /**
     * @param node
     * @return outcome for show subject's shares
     * @throws UnsupportedEncodingException
     */
    private static String getPeerSharesTarget ( PeerSharesTreeNode node ) {
        return getPeerRootTarget(node.getAttachedObject());
    }


    /**
     * @param pi
     * @return outcome for show show peer shares
     */
    public static String getPeerRootTarget ( PeerInfo pi ) {
        try {
            if ( pi != null ) {
                return String.format(
                    "/index.xhtml?faces-redirect=true&type=peer-root&peer=%s&rootType=peers-root", //$NON-NLS-1$
                    URLEncoder.encode(pi.toString(), "UTF-8")); //$NON-NLS-1$
            }
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }

}
