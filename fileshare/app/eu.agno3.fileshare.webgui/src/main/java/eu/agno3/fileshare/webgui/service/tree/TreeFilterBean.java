/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.io.Serializable;
import java.util.regex.Pattern;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.subject.UserHidingBean;
import eu.agno3.runtime.util.matching.PatternUtil;


/**
 * @author mbechler
 *
 */
@Named ( "treeFilterBean" )
@ViewScoped
public class TreeFilterBean implements Serializable {

    private static final Logger log = Logger.getLogger(TreeFilterBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 7686070085175971494L;
    private TreeFilterMode filterMode = TreeFilterMode.VISIBLE;

    @Inject
    private UserHidingBean userHidingBean;

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private FileTreeBean fileTree;

    private String filter;

    private transient Pattern patternCache;


    /**
     * 
     * @param e
     * @return the available filter modes
     */
    public TreeFilterMode[] getModes ( VFSEntity e ) {

        if ( !this.currentUser.isAuthenticated() ) {
            return new TreeFilterMode[] {
                TreeFilterMode.NON_EMPTY, TreeFilterMode.ALL
            };
        }

        return TreeFilterMode.values();
    }


    /**
     * 
     * @return null
     */
    public String showAll () {
        this.filterMode = TreeFilterMode.ALL;
        this.fileTree.refresh();
        return null;
    }


    /**
     * 
     * @return null
     */
    public String resetFilter () {
        this.filter = null;
        this.fileTree.refresh();
        return null;
    }


    /**
     * @return the filter
     */
    public String getFilter () {
        return this.filter;
    }


    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter ( String filter ) {
        this.filter = filter;
        this.patternCache = null;
    }


    /**
     * 
     * @param o
     * @return a display string for the mode
     */
    public String translateMode ( Object o ) {
        if ( ! ( o instanceof TreeFilterMode ) ) {
            return null;
        }

        return FileshareMessages.get("filter." + ( (TreeFilterMode) o ).name()); //$NON-NLS-1$
    }


    /**
     * @return whether the view includes hidden elements
     */
    public boolean includesHidden () {
        if ( !this.currentUser.isAuthenticated() ) {
            return false;
        }
        return this.filterMode == TreeFilterMode.ALL;
    }


    /**
     * @return the filterMode
     */
    public TreeFilterMode getFilterMode () {
        if ( !this.currentUser.isAuthenticated() && this.filterMode == TreeFilterMode.VISIBLE ) {
            return TreeFilterMode.ALL;
        }
        return this.filterMode;
    }


    /**
     * @param filterMode
     *            the filterMode to set
     */
    public void setFilterMode ( TreeFilterMode filterMode ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Setting filter mode " + filterMode); //$NON-NLS-1$
        }
        if ( filterMode != null ) {
            this.filterMode = filterMode;
        }
    }


    /**
     * @param o
     * @return whether the element should be hidden
     */
    public boolean isHidden ( VFSEntity o ) {

        if ( this.filterMode == TreeFilterMode.ALL ) {
            return false;
        }
        else if ( o instanceof VFSContainerEntity && this.filterMode == TreeFilterMode.NON_EMPTY && ( (VFSContainerEntity) o ).isEmpty() ) {
            return true;
        }
        else if ( this.filterMode != TreeFilterMode.ALL && this.userHidingBean.isEntityHidden(o) ) {
            return true;
        }

        return false;
    }


    /**
     * @param o
     * @return whether the element is filtered
     */
    public boolean isFiltered ( VFSEntity o ) {
        if ( StringUtils.isBlank(this.filter) ) {
            return false;
        }

        if ( o.getLocalName() != null && !matchFilter(o.getLocalName()) ) {
            return true;
        }
        return false;
    }


    /**
     * @param subject
     * @return whether the element should be hidden
     */
    public boolean isHidden ( SubjectInfo subject ) {

        if ( this.filterMode == TreeFilterMode.ALL ) {
            return false;
        }
        else if ( this.filterMode != TreeFilterMode.ALL && this.userHidingBean.isSubjectHidden(subject) ) {
            return true;
        }

        return false;
    }


    /**
     * @param o
     * @return whether the element is filtered
     */
    public boolean isFiltered ( SubjectInfo o ) {
        if ( StringUtils.isBlank(this.filter) ) {
            return false;
        }

        if ( o instanceof UserInfo ) {
            if ( !matchFilter( ( (UserInfo) o ).getUserDisplayName()) ) {
                return true;
            }
        }
        else if ( o instanceof GroupInfo ) {
            if ( !matchFilter( ( (GroupInfo) o ).getName()) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param s
     * @return whether the peer is hidden
     */
    public boolean isHidden ( PeerInfo s ) {
        if ( this.filterMode == TreeFilterMode.ALL ) {
            return false;
        }
        else if ( this.filterMode != TreeFilterMode.ALL && this.userHidingBean.isPeerHidden(s) ) {
            return true;
        }

        return false;
    }


    /**
     * @param o
     * @return whether the element is filtered
     */
    public boolean isFiltered ( PeerInfo o ) {
        if ( StringUtils.isBlank(this.filter) ) {
            return false;
        }

        if ( o instanceof SubjectPeerInfo ) {
            if ( isFiltered( ( (SubjectPeerInfo) o ).getSubject()) ) {
                return true;
            }
        }
        else if ( o instanceof MailPeerInfo ) {
            if ( !matchFilter( ( (MailPeerInfo) o ).getMailAddress()) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param data
     * @return
     */
    private boolean matchFilter ( String data ) {
        if ( StringUtils.isEmpty(data) ) {
            return false;
        }

        return getMatcher().matcher(data).matches();
    }


    /**
     * @param data
     * @return
     */
    private Pattern getMatcher () {
        if ( this.patternCache != null ) {
            return this.patternCache;
        }
        String pattern = PatternUtil.makeSubstringQuery(this.filter);

        Pattern regex = PatternUtil.getGlobPattern(pattern, true);
        this.patternCache = regex;
        return regex;
    }


    /**
     * @param subject
     * @return whether the element should be hidden
     */
    public boolean isHiddenGroupMembership ( Subject subject ) {
        if ( this.filterMode == TreeFilterMode.ALL ) {
            return false;
        }
        else if ( this.filterMode == TreeFilterMode.NON_EMPTY
                && ( subject.getSubjectRoot() == null || subject.getSubjectRoot().getElements().isEmpty() ) ) {
            return true;
        }
        else if ( this.filterMode != TreeFilterMode.ALL && this.userHidingBean.isSubjectHidden(subject) ) {
            return true;
        }

        return false;
    }


    /**
     * @param o
     * @return whether the element is filtered
     */
    public boolean isFilteredGroupMembership ( Subject o ) {
        if ( StringUtils.isBlank(this.filter) ) {
            return false;
        }

        return isFiltered(o);
    }
}
