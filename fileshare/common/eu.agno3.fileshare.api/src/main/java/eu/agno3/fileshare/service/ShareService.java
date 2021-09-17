/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.exceptions.ShareNotFoundException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.ShareProperties;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TokenShare;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.runtime.security.password.PasswordGenerationException;


/**
 * @author mbechler
 *
 */
public interface ShareService {

    /**
     * Share an entity to a list of subjects
     * 
     * Access control:
     * - user is owner of the entity to share
     * - AND have share:subjects
     * 
     * @param entityId
     * @param subjectIds
     * @param props
     * @param msg
     * @param notify
     * @param subject
     * @param string
     * @throws FileshareException
     * @return issued grants
     */
    List<SubjectGrant> shareToSubjects ( EntityKey entityId, Collection<UUID> subjectIds, ShareProperties props, boolean notify )
            throws FileshareException;


    /**
     * Share an entity to a list of email recipients
     * 
     * Access control:
     * - user is owner of the entity to share
     * - AND have share:mail
     * 
     * @param entityId
     * @param recipients
     * @param props
     * @param resend
     *            resend mail if share already exists
     * @return issued grants
     * @throws FileshareException
     */
    List<MailGrant> shareByMail ( EntityKey entityId, Collection<MailRecipient> recipients, ShareProperties props, boolean resend )
            throws FileshareException;


    /**
     * Share an entity through a token access URL
     * 
     * Access control:
     * - user is owner of the entity to share
     * - AND have share:token
     * 
     * @param entityId
     * @param identifier
     * @param props
     * @return an access token
     * @throws FileshareException
     */
    TokenShare shareToken ( EntityKey entityId, String identifier, ShareProperties props ) throws FileshareException;


    /**
     * @param g
     * @param props
     * @return the token share
     * @throws FileshareException
     */
    TokenShare recreateTokenShare ( TokenGrant g, ShareProperties props ) throws FileshareException;


    /**
     * List applied grants
     * 
     * Access control:
     * - user is owner of the target entity
     * 
     * @param entityId
     * @param type
     * @return the effective grants on the object
     * @throws FileshareException
     */
    Set<Grant> getEffectiveGrants ( EntityKey entityId, GrantType type ) throws FileshareException;


    /**
     * @param entityID
     * @return all effective grants on the object
     * @throws FileshareException
     */
    Set<Grant> getEffectiveGrants ( EntityKey entityID ) throws FileshareException;


    /**
     * Revoke a share
     * 
     * Access control:
     * - user is owner of the shared entity
     * - AND have share:revoke
     * 
     * @param grantId
     * @throws FileshareException
     */
    void revokeShare ( UUID grantId ) throws FileshareException;


    /**
     * Get grant information
     * 
     * Access control:
     * - user is owner of shared entity
     * - OR user is grant target
     * 
     * 
     * @param id
     * @param selection
     * @return the grant object
     * @throws FileshareException
     * @throws ShareNotFoundException
     * @throws AccessDeniedException
     */
    Grant getGrant ( UUID id ) throws FileshareException;


    /**
     * Get grant information
     * 
     * Access control:
     * - none applied, but returns only very basic information
     * 
     * @param id
     * @return the grant object
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    Grant getGrantUnchecked ( UUID id ) throws EntityNotFoundException, FileshareException;


    /**
     * 
     * Access control:
     * - user is owner of shared entity
     * 
     * @param id
     * @param expiry
     * @throws FileshareException
     */
    void setExpiry ( UUID id, DateTime expiry ) throws FileshareException;


    /**
     * 
     * Access control:
     * - user is owner of shared entity
     * 
     * @param id
     * @param permissions
     * @throws FileshareException
     */
    void setPermissions ( UUID id, Set<GrantPermission> permissions ) throws FileshareException;


    /**
     * 
     * Access control:
     * - user is owner of shared entity
     * 
     * @param id
     * @param comment
     * @throws FileshareException
     */
    void updateComment ( UUID id, String comment ) throws FileshareException;


    /**
     * @param id
     * @return the number of grants on an entity
     * @throws FileshareException
     */
    int getGrantCount ( EntityKey id ) throws FileshareException;


    /**
     * @param id
     * @param limit
     * @return the first up to limit grants on an entity
     * @throws FileshareException
     */
    List<Grant> getFirstGrants ( EntityKey id, int limit ) throws FileshareException;


    /**
     * @param entityId
     * @param recpt
     * @param props
     * @return the default subject for the share mail
     * @throws FileshareException
     */
    String getMailShareSubject ( EntityKey entityId, MailRecipient recpt, ShareProperties props ) throws FileshareException;


    /**
     * @param entityId
     * @param recpt
     * @param props
     * @return the message preview for the share mail
     * @throws FileshareException
     */
    MimeMessage getMailSharePreview ( EntityKey entityId, MailRecipient recpt, ShareProperties props ) throws FileshareException;


    /**
     * @param entityId
     * @param subjectId
     * @param props
     * @return the default subject for the share mail
     * @throws FileshareException
     */
    String getSubjectShareSubject ( EntityKey entityId, UUID subjectId, ShareProperties props ) throws FileshareException;


    /**
     * @param entityId
     * @param subjectId
     * @param props
     * @return the message preview for the share mail
     * @throws FileshareException
     */
    MimeMessage getSubjectSharePreview ( EntityKey entityId, UUID subjectId, ShareProperties props ) throws FileshareException;


    /**
     * Authenticate a password protected grant
     * 
     * 
     * @param grantId
     * @param password
     * @throws FileshareException
     */
    void authToken ( UUID grantId, String password ) throws FileshareException;


    /**
     * @param id
     * @param identifier
     * @throws FileshareException
     */
    void updateIdentifier ( UUID id, String identifier ) throws FileshareException;


    /**
     * @param securityLabel
     * @param l
     *            user locale
     * @return a generated password
     * @throws PasswordGenerationException
     * @throws PolicyNotFoundException
     */
    String generateSharePassword ( SecurityLabel securityLabel, Locale l ) throws PasswordGenerationException, PolicyNotFoundException;

}
