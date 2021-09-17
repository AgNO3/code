/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.util;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.EntityNameBadCharactersException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InvalidEntityException;
import eu.agno3.fileshare.exceptions.NamingConflictException;
import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.fileshare.service.internal.ShareServiceImpl;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public final class ServiceUtil {

    private static Set<Character> NAME_BLACKLIST_CHARACTERS = new HashSet<>(Arrays.asList('?', '*', '/', '\\'));
    private static final int MAX_NAME_TRIES = 10;

    private static final Logger log = Logger.getLogger(ServiceUtil.class);


    /**
     * 
     */
    private ServiceUtil () {}


    /**
     * @param newName
     * @throws InvalidEntityException
     */
    public static void checkFileName ( String newName ) throws InvalidEntityException {
        if ( StringUtils.isBlank(newName) ) {
            throw new InvalidEntityException("Empty file name"); //$NON-NLS-1$
        }

        Set<Character> badChars = new HashSet<>();
        for ( char c : newName.toCharArray() ) {
            if ( NAME_BLACKLIST_CHARACTERS.contains(c) ) {
                badChars.add(c);
            }
        }

        if ( !badChars.isEmpty() ) {
            String badCharsString = StringUtils.join(NAME_BLACKLIST_CHARACTERS, ' ');
            throw new EntityNameBadCharactersException(newName, badCharsString);
        }
    }


    /**
     * @param localName
     * @return encoded file name
     */
    public static String encodeDispositionFilename ( String localName ) {
        // TODO: implement something useful
        //
        // http://greenbytes.de/tech/tc2231/

        /*
         * https://blog.fastmail.com/2011/06/24/download-non-english-filenames/:
         * Putting that together, if you want to send a file called foo-ä.html, then setting a header of:
         * Content-Disposition: attachment; filename="foo-%c3%a4.html"; filename*=UTF-8''foo-%c3%a4.html
         * Will cause IE8+, Opera, Chrome, FF4+ (but not Safari) to correctly save a file named foo-ä.html. This should
         * be easy to do with a URL escaping library that encodes UTF-8 octets not in the unreserved character set.
         */
        try {
            String percentCoded = URLEncoder.encode(localName, "UTF-8"); //$NON-NLS-1$
            return "filename=\"" + //$NON-NLS-1$
                    percentCoded + "\"; filename*=UTF-8''" + //$NON-NLS-1$
                    percentCoded;
        }
        catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException("Unsupported encoding", e); //$NON-NLS-1$
        }
    }


    /**
     * @param tx
     * @param label
     * @return the security label
     */
    public static SecurityLabel getOrCreateSecurityLabel ( EntityTransactionContext tx, String label ) {
        if ( label == null ) {
            return null;
        }
        EntityManager em = tx.getEntityManager();
        SecurityLabel found = em.find(SecurityLabel.class, label);
        if ( found == null ) {
            found = new SecurityLabel();
            found.setLabel(label);
            em.persist(found);
        }
        return found;
    }


    /**
     * @param tx
     * @param entity
     */
    public static void cleanEntityReferences ( EntityTransactionContext tx, ContentEntity entity ) {
        Collection<Grant> grants = entity.getGrants();
        for ( Grant g : new LinkedList<>(grants) ) {
            ShareServiceImpl.cleanupGrant(tx, g, true);
            tx.getEntityManager().remove(g);
        }
        grants.clear();

        Set<User> favoriteBy = entity.getFavoriteBy();
        for ( User u : new LinkedList<>(favoriteBy) ) {
            u.getFavoriteEntities().remove(entity);
            tx.getEntityManager().persist(u);
        }
        favoriteBy.clear();

        Set<User> hiddenBy = entity.getHiddenBy();
        for ( User u : new LinkedList<>(hiddenBy) ) {
            u.getHiddenEntities().remove(entity);
            tx.getEntityManager().persist(u);
        }
        hiddenBy.clear();

    }


    /**
     * @param t
     * @param currentUser
     * @param now
     * @param v
     * @param entity
     * @param grant
     */
    public static void updateLastModified ( RecursiveModificationTimeTracker t, User currentUser, DateTime now, VFSContext v, VFSEntity entity,
            Grant grant ) {
        entity.setLastModified(now);
        if ( currentUser != null || grant != null ) {
            entity.setLastModifier(currentUser);
            entity.setLastModifiedGrant(grant);
        }

        if ( entity.getEntityKey() != null && v != null && t != null ) {
            try {
                t.trackUpdate(v, entity);
            }
            catch ( FileshareException e ) {
                log.error("Failed to track update", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param currentUser
     * @param now
     * @param entity
     * @param grant
     */
    public static void setCreated ( User currentUser, DateTime now, VFSEntity entity, Grant grant ) {
        entity.setCreator(currentUser);
        entity.setCreated(now);
        entity.setCreatorGrant(grant);
    }


    /**
     * @param localName
     * @param elementNames
     * @param target
     * @param currentUser
     * @param grant
     * @return a new, conflict-free name for the file
     * @throws NamingConflictException
     */
    public static String uniquifyLocalNameConflict ( String localName, Set<String> elementNames, VFSContainerEntity target, User currentUser,
            Grant grant ) throws NamingConflictException {
        if ( elementNames.contains(localName) ) {
            return ServiceUtil.findFreeName(localName, target, currentUser, elementNames, grant);
        }

        return localName;
    }


    /**
     * @param localName
     * @param target
     * @param currentUser
     * @param elementNames
     * @param grant
     * @return a free name
     * @throws NamingConflictException
     */
    public static String findFreeName ( String localName, VFSContainerEntity target, User currentUser, Set<String> elementNames, Grant grant )
            throws NamingConflictException {
        boolean isOwner = target.getOwner().equals(currentUser);
        String userName = ServiceUtil.getUserName(currentUser, grant);

        String name = findFreeName(localName, elementNames, isOwner, userName);

        if ( name != null ) {
            return name;
        }

        throw new NamingConflictException(localName, target.getLocalName(), "Provided name does already exist and no free name could be found"); //$NON-NLS-1$
    }


    /**
     * @param v
     * @param localName
     * @param target
     * @throws FileshareException
     */
    public static void checkLocalNameConflict ( VFSContext v, String localName, VFSContainerEntity target ) throws FileshareException {
        if ( ServiceUtil.getDirectoryElementNames(v, target).keySet().contains(localName) ) {
            throw new NamingConflictException(localName, target.getLocalName(), "Target name does already exist"); //$NON-NLS-1$
        }
    }


    /**
     * @param localName
     * @param elementNames
     * @param isOwner
     * @param userName
     * @return a free name
     */
    public static String findFreeName ( String localName, Set<String> elementNames, boolean isOwner, String userName ) {
        String baseName;
        String suffix;
        int firstSep = localName.indexOf('.');

        if ( firstSep > 0 ) {
            baseName = localName.substring(0, firstSep);
            suffix = localName.substring(firstSep);
        }
        else {
            baseName = localName;
            suffix = StringUtils.EMPTY;
        }

        for ( int i = 1; i <= MAX_NAME_TRIES; i++ ) {
            String tryName;
            if ( isOwner ) {
                tryName = String.format("%s (%d)%s", baseName, i, suffix); //$NON-NLS-1$
            }
            else {
                tryName = String.format("%s (%s-%d)%s", baseName, userName, i, suffix); //$NON-NLS-1$
            }
            if ( !elementNames.contains(tryName) ) {
                return tryName;
            }
        }

        return null;
    }


    /**
     * @param currentUser
     * @param grant
     * @return a username
     */
    public static String getUserName ( User currentUser, Grant grant ) {

        if ( currentUser != null ) {
            return currentUser.getPrincipal().getUserName();
        }
        else if ( grant instanceof MailGrant ) {
            String mailAddr = ( (MailGrant) grant ).getMailAddress();
            int addrSep = mailAddr.indexOf('@');
            if ( addrSep >= 0 ) {
                mailAddr.substring(0, addrSep);
            }
            return mailAddr;
        }

        return "anonymous"; //$NON-NLS-1$

    }


    /**
     * @param tx
     * @param policyConfig
     * @param target
     * @param userSecurityLabel
     * @return the security label to use for the target
     * @throws PolicyNotFoundException
     */
    public static SecurityLabel deriveSecurityLabel ( EntityTransactionContext tx, SecurityPolicyConfiguration policyConfig, VFSEntity target,
            SecurityLabel userSecurityLabel ) throws PolicyNotFoundException {
        SecurityLabel targetLabel = target != null ? target.getSecurityLabel() : null;

        if ( targetLabel != null && userSecurityLabel != null && !targetLabel.equals(userSecurityLabel) ) {

            if ( policyConfig.compareLabels(targetLabel, userSecurityLabel) > 0 ) {
                return targetLabel;
            }
            return userSecurityLabel;
        }
        else if ( userSecurityLabel != null ) {
            return userSecurityLabel;
        }
        else if ( targetLabel != null ) {
            return targetLabel;
        }

        return getOrCreateSecurityLabel(tx, policyConfig.getDefaultLabel());
    }


    /**
     * @param localName
     * @return a normalized path name
     * @throws InvalidEntityException
     */
    public static String normalizeFileName ( String localName ) throws InvalidEntityException {
        String newName = localName;
        if ( localName.charAt(0) == '/' ) {
            // strip UNIX path
            int lastSep = localName.lastIndexOf('/');
            newName = localName.substring(lastSep + 1);
        }
        else if ( ( localName.length() >= 3 && localName.charAt(1) == ':' && localName.charAt(2) == '\\' )
                || ( localName.length() >= 2 && localName.charAt(0) == '\\' && localName.charAt(1) == '\\' ) ) {
            // strip Windows path
            int lastSep = localName.lastIndexOf('\\');
            newName = localName.substring(lastSep + 1);
        }

        checkFileName(newName);
        return newName;
    }


    /**
     * @param tx
     * @param t
     * @param subj
     * @param currentUser
     * @param secConfig
     * @return a new subject root
     */
    public static ContainerEntity createSubjectRoot ( EntityTransactionContext tx, RecursiveModificationTimeTracker t, Subject subj, User currentUser,
            SecurityPolicyConfiguration secConfig ) {
        ContainerEntity subjectRoot = new ContainerEntity();
        subjectRoot.setOwner(subj);
        subjectRoot.setAllowFileOverwrite(true);

        String rootContainerLabel = secConfig.getRootContainerLabel();

        if ( !StringUtils.isBlank(rootContainerLabel) ) {
            subjectRoot.setSecurityLabel(getOrCreateSecurityLabel(tx, rootContainerLabel));
        }
        else if ( subj instanceof User ) {
            subjectRoot.setSecurityLabel( ( (User) subj ).getSecurityLabel());
        }

        if ( subjectRoot.getSecurityLabel() == null ) {
            subjectRoot.setSecurityLabel(getOrCreateSecurityLabel(tx, secConfig.getDefaultLabel()));
        }

        DateTime now = DateTime.now();

        setCreated(currentUser, now, subjectRoot, null);
        updateLastModified(t, currentUser, now, null, subjectRoot, null);

        return subjectRoot;
    }


    /**
     * @param ctx
     * @param target
     * @return a set of all names in the directory
     * @throws FileshareException
     */
    public static Map<String, VFSEntity> getDirectoryElementNames ( VFSContext ctx, VFSContainerEntity target ) throws FileshareException {
        Map<String, VFSEntity> elementNames = new HashMap<>();

        for ( VFSEntity e : ctx.getChildren(target) ) {
            elementNames.put(e.getLocalName(), e);
        }

        return elementNames;
    }


    /**
     * @param tx
     * @param vs
     * @param entity
     * @return the vfs entity
     * @throws FileshareException
     * @throws EntityNotFoundException
     */
    public static VFSEntity unwrapEntity ( EntityTransactionContext tx, VFSServiceInternal vs, ContentEntity entity )
            throws EntityNotFoundException, FileshareException {
        if ( entity instanceof MappedVFSEntity ) {
            MappedVFSEntity e = (MappedVFSEntity) entity;
            if ( e.getDelegate() != null ) {
                try ( VFSContext v = vs.getVFS(e.getEntityKey()).begin(tx) ) {
                    return v.getVfsEntity(e);
                }
            }

            if ( e.getVfs() == null ) {
                throw new EntityNotFoundException("No VFS found"); //$NON-NLS-1$
            }

            try ( VFSContext v = vs.getVFS(e.getVfs().getVfs()).begin(tx) ) {
                return v.getVfsEntity(e);
            }
        }

        return entity;
    }


    /**
     * @param tx
     * @param vs
     * @param g
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    public static void enhanceVFSGrant ( EntityTransactionContext tx, VFSServiceInternal vs, Grant g )
            throws EntityNotFoundException, FileshareException {
        if ( g.getEntity() instanceof MappedVFSEntity ) {
            MappedVFSEntity v = (MappedVFSEntity) g.getEntity();
            v.setDelegate(unwrapEntity(tx, vs, g.getEntity()));
        }
    }

}
