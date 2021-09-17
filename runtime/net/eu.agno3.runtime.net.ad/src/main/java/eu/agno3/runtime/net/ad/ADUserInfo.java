/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.runtime.util.sid.SID;


/**
 * @author mbechler
 *
 */
public interface ADUserInfo {

    /**
     * 
     */
    static final int GUEST = 1;

    /**
     * 
     */
    static final int NTLM_NO_ENCRYPTION = 1 << 1;

    /**
     * 
     */
    static final int NTLM_LANMANAGER = 1 << 4;

    /**
     * 
     */
    static final int NTLM_SUBAUTH = 1 << 7;

    /**
     * 
     */
    static final int NTLM_MACHINE = 1 << 8;

    /**
     * 
     */
    static final int NTLM_V2_SUPPORT = 1 << 9;

    /**
     * 
     */
    static final int NTLM_PROFILE_PATH = 1 << 11;

    /**
     * 
     */
    static final int NTLM_NTLMv2 = 1 << 12;

    /**
     * 
     */
    static final int NTLM_LMv2 = 1 << 13;

    /**
     * 
     */
    static final int NTLM_NTLMv2SESSION = 1 << 14;


    /**
     * @return the user domain name
     */
    String getDomainName ();


    /**
     * @return the account name
     */
    String getAccountName ();


    /**
     * 
     * @return the group membership SIDs
     */
    Set<SID> getGroupSids ();


    /**
     * 
     * @return the user display name
     */
    String getDisplayName ();


    /**
     * 
     * @return the date at which the password expires
     */
    DateTime getPwMustChange ();


    /**
     * 
     * @return the date until which the password can be changed
     */
    DateTime getPwCanChange ();


    /**
     * 
     * @return the date the password was last changed
     */
    DateTime getPwLastChange ();


    /**
     * 
     * @return the last login timestamp
     */
    DateTime getLastLogon ();


    /**
     * 
     * @return user login counter
     */
    int getLogonCount ();


    /**
     * 
     * @return bad password counter
     */
    int getBadPasswordCount ();


    /**
     * 
     * @return primary group SID
     */
    SID getPrimaryGroupSid ();


    /**
     * 
     * @return the user flags
     */
    int getUserFlags ();


    /**
     * 
     * @return the user SID
     */
    SID getUserSid ();


    /**
     * 
     * @return the server used for logon
     */
    String getLogonServer ();


    /**
     * @return requested logon expiration time
     */
    DateTime getLogoffTime ();


    /**
     * @return requested forced logout time
     */
    DateTime getKickoffTime ();


    /**
     * 
     * @return whether smartcard logon is required
     */
    boolean isSmartCardLoginRequired ();

}
