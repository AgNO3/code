/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.util.HashSet;
import java.util.Set;

import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.rpc.sid_t;
import jcifs.dcerpc.rpc.unicode_string;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class NetlogonValidationSamInfo extends ValidationInformation {

    private DateTime logonTime;
    private DateTime logoffTime;
    private DateTime kickoffTime;
    private DateTime passwordLastSet;
    private DateTime passwordCanChange;
    private DateTime passwordMustChange;

    private unicode_string effectiveName = new unicode_string();
    private unicode_string fullName = new unicode_string();
    private unicode_string logonScript = new unicode_string();
    private unicode_string profilePath = new unicode_string();
    private unicode_string homeDirectory = new unicode_string();
    private unicode_string homeDirectoryDrive = new unicode_string();

    private int logonCount;
    private int badPasswordCount;

    private int userId;
    private int primaryGroupId;
    private int userFlags;

    private byte[] userSessionKey = new byte[16];

    private unicode_string logonServer = new unicode_string();
    private unicode_string logonDomainName = new unicode_string();

    private sid_t logonDomain = new sid_t();
    private Set<GroupEntry> groups = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.ValidationInformation#getLevel()
     */
    @Override
    public int getLevel () {
        return 2;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer buf ) throws NdrException {
        this.logonTime = RPCUtil.decodeFiletime(buf);
        this.logoffTime = RPCUtil.decodeFiletime(buf);
        this.kickoffTime = RPCUtil.decodeFiletime(buf);
        this.passwordLastSet = RPCUtil.decodeFiletime(buf);
        this.passwordCanChange = RPCUtil.decodeFiletime(buf);
        this.passwordMustChange = RPCUtil.decodeFiletime(buf);

        int efPtr = RPCUtil.decodeStringRef(buf, this.effectiveName);
        int fnPtr = RPCUtil.decodeStringRef(buf, this.fullName);
        int lsPtr = RPCUtil.decodeStringRef(buf, this.logonScript);
        int ppPtr = RPCUtil.decodeStringRef(buf, this.profilePath);
        int hdPtr = RPCUtil.decodeStringRef(buf, this.homeDirectory);
        int hddPtr = RPCUtil.decodeStringRef(buf, this.homeDirectoryDrive);

        this.logonCount = buf.dec_ndr_short();
        this.badPasswordCount = buf.dec_ndr_short();
        this.userId = buf.dec_ndr_long();
        this.primaryGroupId = buf.dec_ndr_long();

        int groupCount = buf.dec_ndr_long();
        int groupPtr = buf.dec_ndr_long();

        this.userFlags = buf.dec_ndr_long();

        buf.readOctetArray(this.userSessionKey, 0, 16);

        int lsrvPtr = RPCUtil.decodeStringRef(buf, this.logonServer);
        int ldPtr = RPCUtil.decodeStringRef(buf, this.logonDomainName);

        int domainPtr = buf.dec_ndr_long();

        buf.advance(40);

        RPCUtil.decodeStringVal(buf, efPtr, this.effectiveName);
        RPCUtil.decodeStringVal(buf, fnPtr, this.fullName);
        RPCUtil.decodeStringVal(buf, lsPtr, this.logonScript);
        RPCUtil.decodeStringVal(buf, ppPtr, this.profilePath);
        RPCUtil.decodeStringVal(buf, hdPtr, this.homeDirectory);
        RPCUtil.decodeStringVal(buf, hddPtr, this.homeDirectoryDrive);

        if ( groupPtr > 0 ) {
            groupCount = buf.dec_ndr_long();
            this.groups.clear();

            for ( int i = 0; i < groupCount; i++ ) {
                int gid = buf.dec_ndr_long();
                int attrs = buf.dec_ndr_long();
                this.groups.add(new GroupEntry(gid, attrs));
            }
        }

        RPCUtil.decodeStringVal(buf, lsrvPtr, this.logonServer);
        RPCUtil.decodeStringVal(buf, ldPtr, this.logonDomainName);

        if ( domainPtr != 0 ) {
            this.logonDomain.decode(buf);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer arg0 ) throws NdrException {

    }


    /**
     * @return the effectiveName
     */
    public String getEffectiveName () {
        return unwrapString(this.effectiveName);
    }


    /**
     * @return the fullName
     */
    public String getFullName () {
        return unwrapString(this.fullName);
    }


    /**
     * @return the logonScript
     */
    public String getLogonScript () {
        return unwrapString(this.logonScript);
    }


    /**
     * @return the profilePath
     */
    public String getProfilePath () {
        return unwrapString(this.profilePath);
    }


    /**
     * @return the homeDirectory
     */
    public String getHomeDirectory () {
        return unwrapString(this.homeDirectory);
    }


    /**
     * @return the homeDirectoryDrive
     */
    public String getHomeDirectoryDrive () {
        return unwrapString(this.homeDirectoryDrive);
    }


    /**
     * @return the domain name
     */
    public String getLogonDomainName () {
        return unwrapString(this.logonDomainName);
    }


    /**
     * @return the logonServer
     */
    public String getLogonServer () {
        return unwrapString(this.logonServer);
    }


    /**
     * @return the logonDomain
     */
    public sid_t getLogonDomainSID () {
        return this.logonDomain;
    }


    /**
     * @return the badPasswordCount
     */
    public int getBadPasswordCount () {
        return this.badPasswordCount;
    }


    /**
     * @return the logonCount
     */
    public int getLogonCount () {
        return this.logonCount;
    }


    /**
     * @return the groups
     */
    public Set<GroupEntry> getGroups () {
        return this.groups;
    }


    /**
     * @return the kickoffTime
     */
    public DateTime getKickoffTime () {
        return this.kickoffTime;
    }


    /**
     * @return the logoffTime
     */
    public DateTime getLogoffTime () {
        return this.logoffTime;
    }


    /**
     * @return the logonTime
     */
    public DateTime getLogonTime () {
        return this.logonTime;
    }


    /**
     * @return the passwordCanChange
     */
    public DateTime getPasswordCanChange () {
        return this.passwordCanChange;
    }


    /**
     * @return the passwordLastSet
     */
    public DateTime getPasswordLastSet () {
        return this.passwordLastSet;
    }


    /**
     * @return the passwordMustChange
     */
    public DateTime getPasswordMustChange () {
        return this.passwordMustChange;
    }


    /**
     * @return the primaryGroupId
     */
    public int getPrimaryGroupId () {
        return this.primaryGroupId;
    }


    /**
     * @return the userFlags
     */
    public int getUserFlags () {
        return this.userFlags;
    }


    /**
     * @return the userId
     */
    public int getUserId () {
        return this.userId;
    }


    /**
     * @return the userSessionKey
     */
    public byte[] getUserSessionKey () {
        return this.userSessionKey;
    }


    /**
     * @param logonDomainName2
     * @return
     */
    private static String unwrapString ( unicode_string str ) {
        return new UnicodeString(str, false).toString();
    }

    /**
     * 
     * @author mbechler
     *
     */
    public static class GroupEntry {

        private int attrs;
        private int gid;


        /**
         * @param gid
         * @param attrs
         */
        public GroupEntry ( int gid, int attrs ) {
            this.gid = gid;
            this.attrs = attrs;
        }


        /**
         * @return the gid
         */
        public int getGid () {
            return this.gid;
        }


        /**
         * @return the attrs
         */
        public int getAttrs () {
            return this.attrs;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals ( Object obj ) {
            if ( obj instanceof GroupEntry ) {
                return ( (GroupEntry) obj ).gid == this.gid;
            }
            return super.equals(obj);
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode () {
            return this.gid;
        }

    }
}
