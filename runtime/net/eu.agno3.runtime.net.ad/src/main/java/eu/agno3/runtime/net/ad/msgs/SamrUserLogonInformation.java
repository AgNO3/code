/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.rpc.unicode_string;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class SamrUserLogonInformation extends SamrUserInformation {

    private unicode_string userName = new unicode_string();
    private unicode_string fullName = new unicode_string();

    private int userId;
    private int primaryGroupId;

    private unicode_string homeDirectory = new unicode_string();
    private unicode_string homeDirectoryDrive = new unicode_string();
    private unicode_string scriptPath = new unicode_string();
    private unicode_string profilePath = new unicode_string();
    private unicode_string workStations = new unicode_string();

    private DateTime lastLogon;
    private DateTime lastLogoff;

    private DateTime passwordLastSet;
    private DateTime passwordCanChange;
    private DateTime passwordMustChange;

    private LogonHours logonHours = new LogonHours();

    private int badPasswordCount;
    private int logonCount;

    private int userAccountInformation;


    @Override
    public short getLevel () {
        return 3;
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
     * 
     * @return the userName
     */
    public String getUserName () {
        return this.userName.toString();
    }


    /**
     * 
     * @return the full name
     */
    public String getFullName () {
        return this.fullName.toString();
    }


    /**
     * @return the userId
     */
    public int getUserId () {
        return this.userId;
    }


    /**
     * 
     * @return the primary group id
     */
    public int getPrimaryGroupId () {
        return this.primaryGroupId;
    }


    /**
     * 
     * @return the home directory
     */
    public String getHomeDirectory () {
        return this.homeDirectory.toString();
    }


    /**
     * 
     * @return the homeDirectoryDrive
     */
    public String getHomeDirectoryDrive () {
        return this.homeDirectoryDrive.toString();
    }


    /**
     * 
     * @return the scriptPath
     */
    public String getScriptPath () {
        return this.scriptPath.toString();
    }


    /**
     * 
     * @return the profile path
     */
    public String getProfilePath () {
        return this.profilePath.toString();
    }


    /**
     * 
     * @return the workstations
     */
    public String getWorkStations () {
        return this.workStations.toString();
    }


    /**
     * 
     * @return the lastLogon
     */
    public DateTime getLastLogon () {
        return this.lastLogon;
    }


    /**
     * 
     * @return the lastLogoff
     */
    public DateTime getLastLogoff () {
        return this.lastLogoff;
    }


    /**
     * 
     * @return the logonHours
     */
    public LogonHours getLogonHours () {
        return this.logonHours;
    }


    /**
     * 
     * @return the badPasswordCount
     */
    public int getBadPasswordCount () {
        return this.badPasswordCount;
    }


    /**
     * 
     * @return the logonCount
     */
    public int getLogonCount () {
        return this.logonCount;
    }


    /**
     * 
     * @return the userAccountInformation
     */
    public int getUserAccountInformation () {
        return this.userAccountInformation;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer buf ) throws NdrException {
        int efPtr = RPCUtil.decodeStringRef(buf, this.userName);
        int fnPtr = RPCUtil.decodeStringRef(buf, this.fullName);

        this.userId = buf.dec_ndr_long();
        this.primaryGroupId = buf.dec_ndr_long();

        int hdPtr = RPCUtil.decodeStringRef(buf, this.homeDirectory);
        int hddPtr = RPCUtil.decodeStringRef(buf, this.homeDirectoryDrive);
        int lsPtr = RPCUtil.decodeStringRef(buf, this.scriptPath);
        int ppPtr = RPCUtil.decodeStringRef(buf, this.profilePath);
        int wsPtr = RPCUtil.decodeStringRef(buf, this.workStations);

        this.lastLogon = RPCUtil.decodeFiletime(buf);
        this.lastLogoff = RPCUtil.decodeFiletime(buf);
        this.passwordLastSet = RPCUtil.decodeFiletime(buf);
        this.passwordCanChange = RPCUtil.decodeFiletime(buf);
        this.passwordMustChange = RPCUtil.decodeFiletime(buf);

        this.logonHours.decode(buf);

        this.badPasswordCount = buf.dec_ndr_short();
        this.logonCount = buf.dec_ndr_short();
        this.userAccountInformation = buf.dec_ndr_long();

        RPCUtil.decodeStringVal(buf, efPtr, this.userName);
        RPCUtil.decodeStringVal(buf, fnPtr, this.fullName);

        RPCUtil.decodeStringVal(buf, hdPtr, this.homeDirectory);
        RPCUtil.decodeStringVal(buf, hddPtr, this.homeDirectoryDrive);
        RPCUtil.decodeStringVal(buf, lsPtr, this.scriptPath);
        RPCUtil.decodeStringVal(buf, ppPtr, this.profilePath);
        RPCUtil.decodeStringVal(buf, wsPtr, this.workStations);

        this.logonHours.complete(buf);

    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer buf ) throws NdrException {
        // TODO Auto-generated method stub

    }

}
