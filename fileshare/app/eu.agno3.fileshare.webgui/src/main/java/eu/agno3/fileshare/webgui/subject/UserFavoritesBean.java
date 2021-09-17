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
import eu.agno3.fileshare.webgui.service.shortcut.ShortcutBean;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "userFavoritesBean" )
@SessionScoped
public class UserFavoritesBean implements Serializable {

    private static final Logger log = Logger.getLogger(UserFavoritesBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 681179161302544432L;

    @Inject
    private FileTreeBean fileTree;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private ShortcutBean shortcuts;

    @Inject
    private CurrentUserBean currentUser;

    private boolean entityFavoritesLoaded;
    private Set<EntityKey> entityFavorites = new HashSet<>();

    private boolean subjectFavoritesLoaded;
    private Set<UUID> subjectFavorites = new HashSet<>();

    private boolean mailFavoritesLoaded;
    private Set<String> mailFavorites = new HashSet<>();


    /**
     * @param targetId
     */
    public void trackEntityFavoriteUsage ( EntityKey targetId ) {
        try {
            this.fsp.getFlaggingService().trackEntityFavorityUsage(targetId);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     */
    private void ensureEntityFavoritesLoaded () {
        if ( !this.entityFavoritesLoaded ) {
            log.debug("Loading entity favorites"); //$NON-NLS-1$

            try {
                this.entityFavoritesLoaded = true;
                this.entityFavorites = new HashSet<>(this.fsp.getFlaggingService().getFavoriteEntityIds());
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
    private void ensureSubjectFavoritesLoaded () {
        if ( !this.subjectFavoritesLoaded ) {
            log.debug("Loading subject favorites"); //$NON-NLS-1$
            try {
                this.subjectFavoritesLoaded = true;
                this.subjectFavorites = new HashSet<>(this.fsp.getFlaggingService().getFavoriteSubjectIds());
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
    private void ensureMailFavoritesLoaded () {
        if ( !this.mailFavoritesLoaded ) {
            log.debug("Loading mail favorites"); //$NON-NLS-1$
            try {
                this.mailFavoritesLoaded = true;
                this.mailFavorites = new HashSet<>(this.fsp.getFlaggingService().getFavoriteMails());
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
     * @return whether the entity is a user favorite
     */
    public boolean isEntityFavorite ( VFSEntity e ) {
        if ( e == null ) {
            return false;
        }
        ensureEntityFavoritesLoaded();
        return this.entityFavorites.contains(e.getEntityKey());
    }


    /**
     * 
     * @param es
     * @return whether all entities are favorites
     */
    public boolean allEntitiesFavorite ( Collection<VFSEntity> es ) {
        if ( es == null || es.isEmpty() ) {
            return true;
        }

        for ( VFSEntity e : es ) {
            if ( !isEntityFavorite(e) ) {
                return false;
            }
        }

        return true;
    }


    /**
     * 
     * @param s
     * @return whether the subject is a user favorite
     */
    public boolean isSubjectFavorite ( SubjectInfo s ) {
        if ( s == null ) {
            return false;
        }
        ensureSubjectFavoritesLoaded();
        return this.subjectFavorites.contains(s.getId());
    }


    /**
     * 
     * @param pi
     * @return whether the peer is a favorite
     */
    public boolean isPeerFavorite ( PeerInfo pi ) {
        if ( pi instanceof SubjectPeerInfo ) {
            return isSubjectFavorite( ( (SubjectPeerInfo) pi ).getSubject());
        }
        else if ( pi instanceof MailPeerInfo ) {
            return isMailFavorite( ( (MailPeerInfo) pi ).getMailAddress());
        }
        else if ( pi instanceof TokenPeerInfo ) {
            return this.currentUser.getCurrentUser().getLinksFavorite();
        }

        return false;
    }


    /**
     * 
     * @param pi
     * @return null
     */
    public String makePeerFavorite ( PeerInfo pi ) {
        if ( pi instanceof SubjectPeerInfo ) {
            return makeSubjectFavorite( ( (SubjectPeerInfo) pi ).getSubject());
        }
        else if ( pi instanceof MailPeerInfo ) {
            return makeMailFavorite( ( (MailPeerInfo) pi ).getMailAddress());
        }
        else if ( pi instanceof TokenPeerInfo ) {
            markLinksFavorite();
        }

        return null;
    }


    /**
     * 
     */
    public void markLinksFavorite () {
        try {
            this.fsp.getFlaggingService().markLinksFavorite();
            this.currentUser.getCurrentUser().setLinksFavorite(true);
            this.shortcuts.refresh();
            this.fileTree.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param pi
     * @return null
     */
    public String removePeerFavorite ( PeerInfo pi ) {
        if ( pi instanceof SubjectPeerInfo ) {
            return removeSubjectFavorite( ( (SubjectPeerInfo) pi ).getSubject());
        }
        else if ( pi instanceof MailPeerInfo ) {
            return removeMailFavorite( ( (MailPeerInfo) pi ).getMailAddress());
        }
        else if ( pi instanceof TokenPeerInfo ) {
            unmarkLinksFavorite();
        }

        return null;
    }


    /**
     * 
     */
    public void unmarkLinksFavorite () {
        try {
            this.fsp.getFlaggingService().unmarkLinksFavorite();
            this.currentUser.getCurrentUser().setLinksFavorite(false);
            this.shortcuts.refresh();
            this.fileTree.refresh();
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
     * @return whether all subjects are favorites
     */
    public boolean allSubjectsFavorite ( Collection<SubjectInfo> subjs ) {
        if ( subjs == null || subjs.isEmpty() ) {
            return true;
        }

        for ( SubjectInfo s : subjs ) {
            if ( !isSubjectFavorite(s) ) {
                return false;
            }
        }

        return true;
    }


    /**
     * 
     * @param peers
     * @return whether all subjects are favorites
     */
    public boolean allPeersFavorite ( Collection<PeerInfo> peers ) {
        if ( peers == null || peers.isEmpty() ) {
            return true;
        }

        for ( PeerInfo s : peers ) {
            if ( !isPeerFavorite(s) ) {
                return false;
            }
        }

        return true;
    }


    /**
     * 
     * @param addr
     * @return whether this is a mail favorite
     */
    public boolean isMailFavorite ( String addr ) {
        if ( StringUtils.isEmpty(addr) ) {
            return false;
        }
        ensureMailFavoritesLoaded();
        return this.mailFavorites.contains(addr);
    }


    /**
     * 
     * @param e
     * @return null
     */
    public String makeEntityFavorite ( VFSEntity e ) {
        if ( e == null ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        markEntitiesFavorite(Arrays.asList(e.getEntityKey()));
        this.shortcuts.refresh();
        this.entityFavorites.add(e.getEntityKey());
        return null;
    }


    /**
     * 
     * @param e
     * @return dialog close
     */
    public String makeEntityFavoriteDialog ( VFSEntity e ) {
        makeEntityFavorite(e);
        return DialogContext.closeDialog(true);
    }


    /**
     * 
     * @param es
     * @return null
     */
    public String makeEntitiesFavorite ( Collection<VFSEntity> es ) {
        if ( es == null || es.isEmpty() ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        Set<EntityKey> entityIds = new HashSet<>();
        for ( VFSEntity e : es ) {
            entityIds.add(e.getEntityKey());
        }

        markEntitiesFavorite(entityIds);
        this.entityFavorites.addAll(entityIds);
        this.shortcuts.refresh();
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param entityIds
     */
    private void markEntitiesFavorite ( Collection<EntityKey> entityIds ) {
        try {
            this.fsp.getFlaggingService().markEntitiesFavorite(entityIds);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param s
     * @return null
     */
    public String makeSubjectFavorite ( SubjectInfo s ) {
        if ( s == null ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        markSubjectsFavorite(Arrays.asList(s.getId()));
        this.subjectFavorites.add(s.getId());
        this.shortcuts.refresh();
        return null;
    }


    /**
     * 
     * @param subjs
     * @return null
     */
    public String makeSubjectsFavorite ( Collection<SubjectInfo> subjs ) {
        if ( subjs == null ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        Set<UUID> subjIds = new HashSet<>();
        for ( SubjectInfo s : subjs ) {
            subjIds.add(s.getId());

        }
        markSubjectsFavorite(subjIds);
        this.subjectFavorites.addAll(subjIds);
        this.shortcuts.refresh();
        this.fileTree.refresh();
        return null;
    }


    /**
     * 
     * @param peers
     * @return null
     */
    public String makePeersFavorite ( Collection<PeerInfo> peers ) {
        if ( peers == null ) {
            return null;
        }

        Set<UUID> subjects = new HashSet<>();
        Set<String> mailAddrs = new HashSet<>();
        boolean haveLinks = false;
        for ( PeerInfo pi : peers ) {
            if ( pi instanceof SubjectPeerInfo ) {
                subjects.add( ( (SubjectPeerInfo) pi ).getSubject().getId());
            }
            else if ( pi instanceof MailPeerInfo ) {
                mailAddrs.add( ( (MailPeerInfo) pi ).getMailAddress());
            }
            else if ( pi instanceof TokenPeerInfo ) {
                haveLinks = true;
            }
        }

        if ( !subjects.isEmpty() ) {
            markSubjectsFavorite(subjects);
            this.subjectFavorites.addAll(subjects);
        }

        if ( !mailAddrs.isEmpty() ) {
            for ( String addr : mailAddrs ) {
                markMailFavorite(addr);
            }
            this.mailFavorites.addAll(mailAddrs);
        }

        if ( haveLinks && !this.currentUser.getCurrentUser().getLinksFavorite() ) {
            try {
                this.fsp.getFlaggingService().markLinksFavorite();
                this.currentUser.getCurrentUser().setLinksFavorite(true);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

        this.shortcuts.refresh();
        this.fileTree.refresh();

        return null;
    }


    /**
     * 
     * @param peers
     * @return null
     */
    public String removePeersFavorite ( Collection<PeerInfo> peers ) {
        if ( peers == null ) {
            return null;
        }

        Set<UUID> subjects = new HashSet<>();
        Set<String> mailAddrs = new HashSet<>();
        boolean haveLinks = false;
        for ( PeerInfo pi : peers ) {
            if ( pi instanceof SubjectPeerInfo ) {
                subjects.add( ( (SubjectPeerInfo) pi ).getSubject().getId());
            }
            else if ( pi instanceof MailPeerInfo ) {
                mailAddrs.add( ( (MailPeerInfo) pi ).getMailAddress());
            }
            else if ( pi instanceof TokenPeerInfo ) {
                haveLinks = true;
            }
        }

        if ( !subjects.isEmpty() ) {
            unmarkSubjectsFavorite(subjects);
            this.subjectFavorites.removeAll(subjects);
        }

        if ( !mailAddrs.isEmpty() ) {
            for ( String addr : mailAddrs ) {
                unmarkMailFavorite(addr);
            }
            this.mailFavorites.removeAll(mailAddrs);
        }

        if ( haveLinks && this.currentUser.getCurrentUser().getLinksFavorite() ) {
            try {
                this.fsp.getFlaggingService().unmarkLinksFavorite();
                this.currentUser.getCurrentUser().setLinksFavorite(false);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

        this.shortcuts.refresh();
        this.fileTree.refresh();

        return null;
    }


    /**
     * @param subjIds
     */
    private void markSubjectsFavorite ( Collection<UUID> subjIds ) {
        try {
            this.fsp.getFlaggingService().markSubjectsFavorite(subjIds);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param addr
     * @return null
     */
    public String makeMailFavorite ( String addr ) {
        if ( StringUtils.isBlank(addr) ) {
            return null;
        }

        ensureMailFavoritesLoaded();
        markMailFavorite(addr);
        this.mailFavorites.add(addr);
        this.shortcuts.refresh();
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param addr
     */
    private void markMailFavorite ( String addr ) {
        try {
            this.fsp.getFlaggingService().markMailFavorite(addr);
            this.shortcuts.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param e
     * @return null
     */
    public String removeEntityFavorite ( VFSEntity e ) {
        if ( e == null ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        unmarkEntitiesFavorite(Arrays.asList(e.getEntityKey()));
        this.entityFavorites.remove(e.getEntityKey());
        this.shortcuts.refresh();
        return null;
    }


    /**
     * 
     * @param es
     * @return null
     */
    public String removeEntitiesFavorite ( Collection<VFSEntity> es ) {
        if ( es == null || es.isEmpty() ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        Set<EntityKey> entityIds = new HashSet<>();
        for ( VFSEntity e : es ) {
            entityIds.add(e.getEntityKey());
        }
        unmarkEntitiesFavorite(entityIds);
        this.entityFavorites.removeAll(entityIds);
        this.shortcuts.refresh();
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param entityIds
     */
    private void unmarkEntitiesFavorite ( Collection<EntityKey> entityIds ) {
        try {
            this.fsp.getFlaggingService().unmarkEntitiesFavorite(entityIds);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param s
     * @return null
     */
    public String removeSubjectFavorite ( SubjectInfo s ) {
        if ( s == null ) {
            return null;
        }
        ensureSubjectFavoritesLoaded();
        unmarkSubjectsFavorite(Arrays.asList(s.getId()));
        this.subjectFavorites.remove(s.getId());
        this.shortcuts.refresh();
        return null;
    }


    /**
     * @param subjectId
     */
    public void removeSubjectIdFavorite ( UUID subjectId ) {
        if ( subjectId == null ) {
            return;
        }
        ensureSubjectFavoritesLoaded();
        unmarkSubjectsFavorite(Arrays.asList(subjectId));
        this.subjectFavorites.remove(subjectId);
        this.shortcuts.refresh();
    }


    /**
     * 
     * @param subjs
     * @return null
     */
    public String removeSubjectsFavorite ( Collection<SubjectInfo> subjs ) {
        if ( subjs == null ) {
            return null;
        }
        ensureEntityFavoritesLoaded();
        Set<UUID> subjIds = new HashSet<>();
        for ( SubjectInfo s : subjs ) {
            subjIds.add(s.getId());

        }
        unmarkSubjectsFavorite(subjIds);
        this.subjectFavorites.removeAll(subjIds);
        this.shortcuts.refresh();
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param subjIds
     */
    private void unmarkSubjectsFavorite ( Collection<UUID> subjIds ) {
        try {
            this.fsp.getFlaggingService().unmarkSubjectsFavorite(subjIds);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param addr
     * @return null
     */
    public String removeMailFavorite ( String addr ) {
        if ( StringUtils.isBlank(addr) ) {
            return null;
        }

        ensureMailFavoritesLoaded();
        unmarkMailFavorite(addr);
        this.mailFavorites.remove(addr);
        this.shortcuts.refresh();
        this.fileTree.refresh();
        return null;
    }


    /**
     * @param addr
     */
    private void unmarkMailFavorite ( String addr ) {
        try {
            this.fsp.getFlaggingService().unmarkMailFavorite(addr);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }

}
