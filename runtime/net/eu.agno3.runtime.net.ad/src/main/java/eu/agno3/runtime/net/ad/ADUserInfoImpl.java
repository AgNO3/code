/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.runtime.net.ad.msgs.NetlogonValidationSamInfo;
import eu.agno3.runtime.net.ad.msgs.NetlogonValidationSamInfo.GroupEntry;
import eu.agno3.runtime.util.sid.SID;

import jcifs.SmbConstants;
import jcifs.pac.PacLogonInfo;


/**
 * @author mbechler
 *
 */
public class ADUserInfoImpl implements ADUserInfo, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 118251737162332661L;

    private String domainName;
    private String accountName;
    private SID userSid;
    private SID primaryGroupSid;
    private String logonServer;
    private int userFlags;
    private int badPasswordCount;
    private int logonCount;
    private DateTime lastLogon;
    private DateTime logoffTime;
    private DateTime kickoffTime;
    private DateTime pwLastChange;
    private DateTime pwCanChange;
    private DateTime pwMustChange;
    private String displayName;
    private Set<SID> groupSids = new HashSet<>();
    private boolean smartcardLoginRequired;


    /**
     * @return the domainName
     */
    @Override
    public String getDomainName () {
        return this.domainName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADUserInfo#getAccountName()
     */
    @Override
    public String getAccountName () {
        return this.accountName;
    }


    /**
     * @return the logonServer
     */
    @Override
    public String getLogonServer () {
        return this.logonServer;
    }


    /**
     * @return the userSid
     */
    @Override
    public SID getUserSid () {
        return this.userSid;
    }


    /**
     * @return the userFlags
     */
    @Override
    public int getUserFlags () {
        return this.userFlags;
    }


    /**
     * @return the primaryGroupSid
     */
    @Override
    public SID getPrimaryGroupSid () {
        return this.primaryGroupSid;
    }


    /**
     * @return the badPasswordCount
     */
    @Override
    public int getBadPasswordCount () {
        return this.badPasswordCount;
    }


    /**
     * @return the logonCount
     */
    @Override
    public int getLogonCount () {
        return this.logonCount;
    }


    /**
     * @return the lastLogon
     */
    @Override
    public DateTime getLastLogon () {
        return this.lastLogon;
    }


    /**
     * @return the logoffTime
     */
    @Override
    public DateTime getLogoffTime () {
        return this.logoffTime;
    }


    /**
     * @return the kickoffTime
     */
    @Override
    public DateTime getKickoffTime () {
        return this.kickoffTime;
    }


    /**
     * @return the pwLastChange
     */
    @Override
    public DateTime getPwLastChange () {
        return this.pwLastChange;
    }


    /**
     * @return the pwCanChange
     */
    @Override
    public DateTime getPwCanChange () {
        return this.pwCanChange;
    }


    /**
     * @return the pwMustChange
     */
    @Override
    public DateTime getPwMustChange () {
        return this.pwMustChange;
    }


    /**
     * @return the displayName
     */
    @Override
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * @return the groupSids
     */
    @Override
    public Set<SID> getGroupSids () {
        return this.groupSids;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADUserInfo#isSmartCardLoginRequired()
     */
    @Override
    public boolean isSmartCardLoginRequired () {
        return this.smartcardLoginRequired;
    }


    /**
     * @param logonInfo
     * @return the extracted user information
     */
    public static ADUserInfo fromPAC ( PacLogonInfo logonInfo ) {
        ADUserInfoImpl info = new ADUserInfoImpl();
        info.domainName = logonInfo.getDomainName();
        info.accountName = logonInfo.getUserName();
        info.displayName = logonInfo.getUserDisplayName();
        info.logonServer = logonInfo.getServerName();
        info.userFlags = logonInfo.getUserFlags();

        DateTime logonTime = fromDate(logonInfo.getLogonTime());
        if ( logonTime != null && logonTime.getMillis() != -SmbConstants.MILLISECONDS_BETWEEN_1970_AND_1601 ) {
            info.lastLogon = logonTime;
        }
        info.logoffTime = fromDate(logonInfo.getLogoffTime());
        info.kickoffTime = fromDate(logonInfo.getKickOffTime());

        info.pwLastChange = fromDate(logonInfo.getPwdLastChangeTime());
        info.pwCanChange = fromDate(logonInfo.getPwdCanChangeTime());
        info.pwMustChange = fromDate(logonInfo.getPwdMustChangeTime());

        info.badPasswordCount = logonInfo.getBadPasswordCount();
        info.logonCount = logonInfo.getLogonCount();

        info.userSid = SID.fromBinary(logonInfo.getUserSid().toByteArray());
        info.primaryGroupSid = SID.fromBinary(logonInfo.getGroupSid().toByteArray());

        for ( jcifs.smb.SID e : logonInfo.getGroupSids() ) {
            info.groupSids.add(SID.fromBinary(e.toByteArray()));
        }

        info.smartcardLoginRequired = ( logonInfo.getUserAccountControl() & 0x00040000 ) == 0x00040000;

        return info;
    }


    /**
     * @param logoffTime2
     * @return
     */
    private static DateTime fromDate ( Date time ) {
        if ( time == null ) {
            return null;
        }
        return new DateTime(time);
    }


    /**
     * @param samInfo
     * @return the extracted user information
     */
    public static ADUserInfo fromValidationSAMInfo ( NetlogonValidationSamInfo samInfo ) {
        ADUserInfoImpl info = new ADUserInfoImpl();
        info.domainName = samInfo.getLogonDomainName();
        info.accountName = samInfo.getEffectiveName();
        info.displayName = samInfo.getFullName();
        info.logonServer = samInfo.getLogonServer();
        info.userFlags = samInfo.getUserFlags();

        if ( samInfo.getLogonTime() != null && samInfo.getLogonTime().getMillis() != -SmbConstants.MILLISECONDS_BETWEEN_1970_AND_1601 ) {
            info.lastLogon = samInfo.getLogonTime();
        }
        info.logoffTime = samInfo.getLogoffTime();
        info.kickoffTime = samInfo.getKickoffTime();

        info.pwLastChange = samInfo.getPasswordLastSet();
        info.pwCanChange = samInfo.getPasswordCanChange();
        info.pwMustChange = samInfo.getPasswordMustChange();

        info.badPasswordCount = samInfo.getBadPasswordCount();
        info.logonCount = samInfo.getLogonCount();

        SID domainSid = JCIFSSID.fromJCIFS(samInfo.getLogonDomainSID());
        info.userSid = new SID(domainSid, samInfo.getUserId());
        info.primaryGroupSid = new SID(domainSid, samInfo.getPrimaryGroupId());

        for ( GroupEntry e : samInfo.getGroups() ) {
            info.groupSids.add(new SID(domainSid, e.getGid()));
        }

        return info;
    }
}
