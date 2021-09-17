/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FileDisplayBean;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeBean;


/**
 * @author mbechler
 *
 */
@Named ( "userHidingBean" )
@SessionScoped
public class UserHidingBean implements Serializable {

    private static final Logger log = Logger.getLogger(UserHidingBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 681179161302544432L;

    @Inject
    private FileTreeBean fileTree;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private CurrentUserBean currentUser;

    private boolean entitiesHiddenLoaded = false;
    private Set<EntityKey> entitiesHidden = new HashSet<>();
    private boolean subjectHiddenLoaded = false;
    private Set<UUID> subjectsHidden = new HashSet<>();
    private boolean mailHiddenLoaded = false;
    private Set<String> mailHidden = new HashSet<>();


    /**
     * 
     */
    private void ensureEntityHiddenLoaded () {
        if ( !this.entitiesHiddenLoaded ) {
            log.debug("Loading hidden entities"); //$NON-NLS-1$
            try {
                this.entitiesHiddenLoaded = true;
                this.entitiesHidden = new HashSet<>(this.fsp.getFlaggingService().getHiddenEntities());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }

        }
    }


    /**
     * 
     */
    private void ensureSubjectHiddenLoaded () {
        if ( !this.subjectHiddenLoaded ) {
            log.debug("Loading hidden subjects"); //$NON-NLS-1$
            try {
                this.subjectHiddenLoaded = true;
                this.subjectsHidden = new HashSet<>(this.fsp.getFlaggingService().getHiddenSubjects());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }
    }


    /**
     * 
     */
    private void ensureMailHiddenLoaded () {
        if ( !this.mailHiddenLoaded ) {
            log.debug("Loading hidden mail addrs"); //$NON-NLS-1$
            try {
                this.mailHiddenLoaded = true;
                this.mailHidden = new HashSet<>(this.fsp.getFlaggingService().getHiddenMails());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }
    }


    /**
     * 
     * @param e
     * @return whether the entity is hidden by the user
     */
    public boolean isEntityHidden ( VFSEntity e ) {
        if ( e == null ) {
            return false;
        }
        ensureEntityHiddenLoaded();
        return this.entitiesHidden.contains(e.getEntityKey());
    }


    /**
     * 
     * @param entities
     * @return whether all elements are hidden
     */
    public boolean allEntitiesHidden ( Collection<VFSEntity> entities ) {
        for ( VFSEntity e : entities ) {
            if ( !isEntityHidden(e) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * 
     * @param s
     * @return whether the subject is hidden by the user
     */
    public boolean isSubjectHidden ( SubjectInfo s ) {
        if ( s == null ) {
            return false;
        }
        ensureSubjectHiddenLoaded();
        return this.subjectsHidden.contains(s.getId());
    }


    /**
     * @param mailAddress
     * @return whether the mail address is hidden
     */
    public boolean isMailHidden ( String mailAddress ) {
        if ( StringUtils.isBlank(mailAddress) ) {
            return false;
        }
        ensureMailHiddenLoaded();
        return this.mailHidden.contains(mailAddress);
    }


    /**
     * @param peer
     * @return whether the peer is hidden
     */
    public boolean isPeerHidden ( PeerInfo peer ) {

        if ( peer instanceof SubjectPeerInfo ) {
            return isSubjectHidden( ( (SubjectPeerInfo) peer ).getSubject());
        }
        else if ( peer instanceof MailPeerInfo ) {
            return isMailHidden( ( (MailPeerInfo) peer ).getMailAddress());
        }
        else if ( peer instanceof TokenPeerInfo ) {
            return this.currentUser.getCurrentUser().getLinksHidden();
        }

        return false;
    }


    /**
     * 
     * @param subjs
     * @return whether all elements are hidden
     */
    public boolean allSubjectsHidden ( Collection<SubjectInfo> subjs ) {
        for ( SubjectInfo s : subjs ) {
            if ( !isSubjectHidden(s) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * 
     * @param n
     * @return whether the element can be hidden
     */
    public boolean isHideable ( TreeNode n ) {
        if ( FileDisplayBean.isPeerNode(n) || FileDisplayBean.isSubjectNode(n) ) {
            return true;
        }

        if ( FileDisplayBean.isDirectoryNode(n) || FileDisplayBean.isFileNode(n) ) {
            EntityTreeNode en = (EntityTreeNode) n;
            return !StringUtils.isBlank(en.getAttachedObject().getLocalName());
        }

        return false;
    }


    /**
     * 
     * @param nodes
     * @return whether all tree nodes are hidden
     */
    public boolean allHidden ( TreeNode[] nodes ) {
        if ( nodes == null || nodes.length == 0 ) {
            return true;
        }

        for ( TreeNode n : nodes ) {
            if ( !isHidden(n) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * 
     * @param nodes
     * @return whether all tree nodes are hidden
     */
    public boolean allVisible ( TreeNode[] nodes ) {
        if ( nodes == null || nodes.length == 0 ) {
            return true;
        }

        for ( TreeNode n : nodes ) {
            if ( isHidden(n) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param n
     * @return whether the tree node is hidden
     */
    public boolean isHidden ( TreeNode n ) {
        if ( FileDisplayBean.isPeerNode(n) ) {
            return isPeerHidden(FileDisplayBean.getPeer(n));
        }
        if ( FileDisplayBean.isSubjectNode(n) ) {
            return isSubjectHidden(FileDisplayBean.getSubject(n));
        }
        else if ( ( n instanceof EntityTreeNode ) ) {
            return isEntityHidden( ( (EntityTreeNode) n ).getAttachedObject());
        }

        return false;
    }


    /**
     * 
     * @param e
     * @return null
     */
    public String makeEntityHidden ( VFSEntity e ) {
        if ( e == null ) {
            return null;
        }
        ensureEntityHiddenLoaded();
        markEntitiesHidden(Arrays.asList(e.getEntityKey()));
        this.entitiesHidden.add(e.getEntityKey());
        this.fileTree.clearSelection();
        return null;
    }


    /**
     * 
     * @param es
     */
    private void makeEntitiesHidden ( Collection<VFSEntity> es ) {
        if ( es == null || es.isEmpty() ) {
            return;
        }
        ensureEntityHiddenLoaded();
        Set<EntityKey> entityIds = new HashSet<>();
        for ( VFSEntity e : es ) {
            if ( StringUtils.isBlank(e.getLocalName()) ) {
                continue;
            }
            entityIds.add(e.getEntityKey());
        }
        markEntitiesHidden(entityIds);
        this.entitiesHidden.addAll(entityIds);
    }


    /**
     * @param es
     */
    private void markEntitiesHidden ( Collection<EntityKey> es ) {
        try {
            this.fsp.getFlaggingService().markEntitiesHidden(es);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param subjs
     */
    private void makeSubjectsHidden ( Collection<SubjectInfo> subjs ) {
        if ( subjs == null || subjs.isEmpty() ) {
            return;
        }
        ensureEntityHiddenLoaded();

        Set<UUID> subjIds = new HashSet<>();
        for ( SubjectInfo s : subjs ) {
            subjIds.add(s.getId());
        }
        this.markSubjectsHidden(subjIds);
        this.subjectsHidden.addAll(subjIds);
    }


    /**
     * 
     * @param s
     * @return null
     */
    public String makeSubjectHidden ( SubjectInfo s ) {
        if ( s == null ) {
            return null;
        }
        ensureSubjectHiddenLoaded();
        markSubjectsHidden(Arrays.asList(s.getId()));
        this.subjectsHidden.add(s.getId());
        this.fileTree.clearSelection();
        return null;
    }


    /**
     * @param subjIds
     */
    private void markSubjectsHidden ( Collection<UUID> subjIds ) {
        try {
            this.fsp.getFlaggingService().markSubjectsHidden(subjIds);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param nodes
     * @return null
     */
    public String makeHidden ( TreeNode[] nodes ) {

        if ( nodes == null || nodes.length == 0 ) {
            return null;
        }

        Set<VFSEntity> entities = new HashSet<>();
        Set<SubjectInfo> subjects = new HashSet<>();
        Set<String> mailAddresses = new HashSet<>();
        boolean haveLink = false;

        for ( TreeNode n : nodes ) {
            if ( FileDisplayBean.isPeerNode(n) ) {
                PeerInfo pi = FileDisplayBean.getPeer(n);

                if ( pi instanceof SubjectPeerInfo ) {
                    subjects.add( ( (SubjectPeerInfo) pi ).getSubject());
                }
                else if ( pi instanceof MailPeerInfo ) {
                    mailAddresses.add( ( (MailPeerInfo) pi ).getMailAddress());
                }
                else if ( pi instanceof TokenPeerInfo ) {
                    haveLink = true;
                }
            }
            else if ( FileDisplayBean.isSubjectNode(n) ) {
                subjects.add(FileDisplayBean.getSubject(n));
            }
            else if ( ( n instanceof EntityTreeNode ) ) {
                EntityTreeNode entityTreeNode = (EntityTreeNode) n;
                if ( StringUtils.isBlank(entityTreeNode.getAttachedObject().getLocalName()) ) {
                    continue;
                }
                entities.add(entityTreeNode.getAttachedObject());
            }
        }
        makeEntitiesHidden(entities);
        makeSubjectsHidden(subjects);
        makeMailHidden(mailAddresses);

        if ( haveLink && !this.currentUser.getCurrentUser().getLinksHidden() ) {
            try {
                this.fsp.getFlaggingService().markLinksHidden();
                this.currentUser.getCurrentUser().setLinksHidden(true);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

        this.fileTree.clearSelection();
        this.fileTree.refresh();
        return null;
    }


    /**
     * 
     * @param n
     * @return null
     */
    public String makeSingleHidden ( TreeNode n ) {
        return makeHidden(new TreeNode[] {
            n
        });
    }


    /**
     * 
     * @param e
     * @return null
     */
    public String makeEntityVisible ( VFSEntity e ) {
        if ( e == null ) {
            return null;
        }
        ensureEntityHiddenLoaded();
        this.markEntitiesVisible(Arrays.asList(e.getEntityKey()));
        this.entitiesHidden.remove(e.getEntityKey());
        return null;
    }


    /**
     * 
     * @param es
     * @return null
     */
    public String makeEntitiesVisible ( Collection<VFSEntity> es ) {
        if ( es == null || es.isEmpty() ) {
            return null;
        }
        ensureEntityHiddenLoaded();

        Set<EntityKey> entityIds = new HashSet<>();
        for ( VFSEntity e : es ) {
            entityIds.add(e.getEntityKey());
        }
        markEntitiesVisible(entityIds);
        this.entitiesHidden.removeAll(entityIds);
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param es
     */
    private void markEntitiesVisible ( Collection<EntityKey> es ) {
        try {
            this.fsp.getFlaggingService().markEntitiesVisible(es);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * @param s
     * @return null
     */
    public String makeSubjectVisible ( SubjectInfo s ) {
        if ( s == null ) {
            return null;
        }
        ensureSubjectHiddenLoaded();
        this.markSubjectsVisible(Arrays.asList(s.getId()));
        this.subjectsHidden.remove(s.getId());
        return null;
    }


    /**
     * 
     * @param subjs
     * @return null
     */
    public String makeSubjectsVisible ( Collection<SubjectInfo> subjs ) {
        if ( subjs == null || subjs.isEmpty() ) {
            return null;
        }
        ensureSubjectHiddenLoaded();

        Set<UUID> subjIds = new HashSet<>();
        for ( SubjectInfo s : subjs ) {
            subjIds.add(s.getId());
        }
        this.markSubjectsVisible(subjIds);
        this.subjectsHidden.removeAll(subjIds);
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param subjs
     */
    private void markSubjectsVisible ( Collection<UUID> subjs ) {
        try {
            this.fsp.getFlaggingService().markSubjectsVisible(subjs);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * @param mailAddresses
     */
    private void makeMailHidden ( Set<String> mailAddresses ) {
        ensureMailHiddenLoaded();

        for ( String mailAddr : mailAddresses ) {
            try {
                this.fsp.getFlaggingService().markMailHidden(mailAddr);
                this.mailHidden.add(mailAddr);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

    }


    /**
     * 
     * @param nodes
     * @return null
     */
    public String makeVisible ( TreeNode[] nodes ) {

        if ( nodes == null || nodes.length == 0 ) {
            return null;
        }

        Set<VFSEntity> entities = new HashSet<>();
        Set<SubjectInfo> subjects = new HashSet<>();
        Set<String> mailAddresses = new HashSet<>();
        boolean haveLink = false;

        for ( TreeNode n : nodes ) {
            if ( FileDisplayBean.isPeerNode(n) ) {
                PeerInfo pi = FileDisplayBean.getPeer(n);

                if ( pi instanceof SubjectPeerInfo ) {
                    subjects.add( ( (SubjectPeerInfo) pi ).getSubject());
                }
                else if ( pi instanceof MailPeerInfo ) {
                    mailAddresses.add( ( (MailPeerInfo) pi ).getMailAddress());
                }
                else if ( pi instanceof TokenPeerInfo ) {
                    haveLink = true;
                }
            }
            else if ( FileDisplayBean.isSubjectNode(n) ) {
                subjects.add(FileDisplayBean.getSubject(n));
            }
            else if ( ( n instanceof EntityTreeNode ) ) {
                entities.add( ( (EntityTreeNode) n ).getAttachedObject());
            }
        }
        makeEntitiesVisible(entities);
        makeSubjectsVisible(subjects);
        makeMailsVisible(mailAddresses);

        if ( haveLink && this.currentUser.getCurrentUser().getLinksHidden() ) {
            try {
                this.fsp.getFlaggingService().unmarkLinksHidden();
                this.currentUser.getCurrentUser().setLinksHidden(false);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

        this.fileTree.refresh();
        return null;
    }


    /**
     * 
     * @param n
     * @return null
     */
    public String makeSingleVisible ( TreeNode n ) {
        return makeVisible(new TreeNode[] {
            n
        });
    }


    /**
     * @param mailAddresses
     */
    private void makeMailsVisible ( Set<String> mailAddresses ) {
        for ( String mailAddr : mailAddresses ) {
            try {
                this.fsp.getFlaggingService().unmarkMailHidden(mailAddr);
                this.mailHidden.remove(mailAddr);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

    }

}
