/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import javax.mail.internet.MimeMessage;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.TokenValidationException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.notify.MailRecipient;


/**
 * @author mbechler
 *
 */
public interface RegistrationService {

    /**
     * Send a registration verification mail to the given recipient
     * 
     * Access control:
     * - none
     * 
     * @param username
     * @param recpt
     * @param resend
     * @throws FileshareException
     */
    void register ( MailRecipient recpt, boolean resend ) throws FileshareException;


    /**
     * Send a invitation mail to the given recipient
     * 
     * Access control:
     * - have user:inviteUser permission
     * 
     * @param username
     * @param recpt
     * @param subject
     * @param resend
     * @param dateTime
     * @param message
     * @return the created user
     * @throws FileshareException
     */
    User invite ( MailRecipient recpt, String subject, boolean resend, DateTime dateTime ) throws FileshareException;


    /**
     * Completes the registration or invitation process
     * 
     * Access control:
     * - none
     * 
     * @param userName
     * @param newPassword
     * @param userDetails
     * @return the created user
     * @throws FileshareException
     */
    User completeRegistration ( String userName, String newPassword, UserDetails userDetails ) throws FileshareException;


    /**
     * Check current registration token und userName for registration
     * 
     * Access control:
     * - none
     * 
     * @param userName
     * @throws AccessDeniedException
     * @throws TokenValidationException
     * @throws UserNotFoundException
     * @throws UserExistsException
     * @throws FileshareException
     */
    void checkRegistration ( String userName ) throws AccessDeniedException, TokenValidationException, UserNotFoundException, UserExistsException,
            FileshareException;


    /**
     * Send a password reset configuration mail if a matching user exists
     * 
     * Access control:
     * - none
     * 
     * @param userName
     * @param mailAddress
     * @param resend
     * @throws FileshareException
     */
    void resetPassword ( String userName, String mailAddress, boolean resend ) throws FileshareException;


    /**
     * Reset the password
     * 
     * Access control:
     * - resets only the password for the user that is contained in the reset token
     * 
     * @param password
     * @throws FileshareException
     */
    void completePasswordReset ( String password ) throws FileshareException;


    /**
     * @param makeRecipient
     * @param message
     * @return message preview
     * @throws FileshareException
     */
    MimeMessage getInvitationPreview ( MailRecipient makeRecipient ) throws FileshareException;


    /**
     * @param recpt
     * @return the invitation subject
     * @throws FileshareException
     */
    String getInvitationSubject ( MailRecipient recpt ) throws FileshareException;
}
