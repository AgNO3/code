/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;

import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.log4j.Logger;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.util.sid.SID;

import jcifs.dcerpc.DcerpcException;


/**
 * @author mbechler
 *
 */
public class ADLDAPOperations {

    private static final Logger log = Logger.getLogger(ADLDAPOperations.class);

    /**
     * 
     */
    private static final String USER_OBJECT_CLASS = "user"; //$NON-NLS-1$
    private static final String COMPUTER_OBJECT_CLASS = "computer"; //$NON-NLS-1$

    private static final String USER_ACCOUNT_CONTROL = "userAccountControl"; //$NON-NLS-1$
    private static final String CN = "cn"; //$NON-NLS-1$
    private static final String OBJECT_CLASS = "objectClass"; //$NON-NLS-1$
    private static final String OBJECT_SID = "objectSid"; //$NON-NLS-1$
    private static final String SAM_ACCOUNT_NAME = "sAMAccountName"; //$NON-NLS-1$
    private static final String SPN_ATTRIBUTE = "servicePrincipalName"; //$NON-NLS-1$
    private static final String DNS_HOSTNAME = "dNSHostName"; //$NON-NLS-1$
    private static final String KVNO_ATTRIBUTE = "msDS-KeyVersionNumber"; //$NON-NLS-1$
    private static final String ENCTYPE_ATTRIBUTE = "msDS-SupportedEncryptionTypes"; //$NON-NLS-1$

    private static final int ACCOUNT_FLAGS_DISABLE = 0x2;
    private static final int ACCOUNT_FLAGS_WSTRUST = 0x1000;

    private static final String HOST_INSTANCE = "HOST/"; //$NON-NLS-1$


    static int getKVNO ( LDAPClient connection, SID sid ) throws LDAPException, ADException {
        SearchResultEntry res = lookupMachineEntry(connection, sid, SAM_ACCOUNT_NAME, KVNO_ATTRIBUTE);
        return res.getAttributeValueAsInteger(KVNO_ATTRIBUTE);
    }


    /**
     * @param adminUser
     * @param adminPassword
     * @throws ADException
     */
    static void updateDNSHostname ( LDAPClient cl, SID sid, String hostname ) throws ADException, LDAPException {
        SearchResultEntry me = lookupMachineEntry(cl, sid, SPN_ATTRIBUTE, SAM_ACCOUNT_NAME);

        if ( log.isDebugEnabled() ) {
            log.debug("Trying to set dns hostname " + hostname); //$NON-NLS-1$
        }

        Modification addSPN = new Modification(ModificationType.REPLACE, DNS_HOSTNAME, hostname);
        cl.modify(me.getDN(), addSPN);
    }


    /**
     * @param servicePrincipal
     * @param sid
     * @throws ADException
     * @throws LDAPSearchException
     */
    static void ensureHostSPN ( LDAPClient connection, KerberosPrincipal servicePrincipal, SID sid, String machineAccountName )
            throws ADException, LDAPException {
        String princName = servicePrincipal.getName().substring(0, servicePrincipal.getName().lastIndexOf('@'));
        SearchResultEntry res = lookupMachineEntry(connection, sid, SPN_ATTRIBUTE, SAM_ACCOUNT_NAME);

        String accountDN = validateExistingAccount(res, machineAccountName);
        String[] spns = res.getAttributeValues(SPN_ATTRIBUTE);
        if ( log.isDebugEnabled() ) {
            log.debug("Found SPNs " + Arrays.toString(spns)); //$NON-NLS-1$
        }

        if ( matchSPNs(princName, spns) ) {
            // found, all done
            return;
        }

        validateNotDuplicateSPN(princName, connection);
        addSPN(princName, connection, accountDN);
    }


    /**
     * @param princName
     * @param connection
     * @param accountDN
     * @throws LDAPException
     */
    private static void addSPN ( String princName, LDAPClient connection, String accountDN ) throws LDAPException {
        if ( log.isDebugEnabled() ) {
            log.debug("Trying to add SPN " + princName); //$NON-NLS-1$
        }

        Modification addSPN = new Modification(ModificationType.ADD, SPN_ATTRIBUTE, princName);

        try {
            LDAPResult res = connection.modify(accountDN, addSPN);

            if ( res.getResultCode() != ResultCode.SUCCESS ) {
                throw new LDAPException(res);
            }

        }
        catch ( LDAPException e ) {
            if ( e.getResultCode() == ResultCode.CONSTRAINT_VIOLATION || e.getResultCode() == ResultCode.INSUFFICIENT_ACCESS_RIGHTS ) {
                log.error("We don't have sufficient permissions for SPN registration, manual setup needed", e); //$NON-NLS-1$
            }
            else if ( e.getResultCode() != ResultCode.SUCCESS ) {
                throw e;
            }
        }
    }


    /**
     * @param princName
     * @param connection
     * @throws LDAPSearchException
     * @throws ADException
     */
    private static void validateNotDuplicateSPN ( String princName, LDAPClient connection ) throws LDAPSearchException, ADException {
        SearchResultEntry res;
        Filter filter = Filter
                .createANDFilter(Filter.createEqualityFilter(OBJECT_CLASS, USER_OBJECT_CLASS), Filter.createEqualityFilter(SPN_ATTRIBUTE, princName));

        res = connection.searchForEntry(
            "", //$NON-NLS-1$
            SearchScope.SUB,
            filter,
            SAM_ACCOUNT_NAME);

        if ( res != null ) {
            throw new ADException(String.format(
                "The user '%s' already has the SPN %s assigned: %s", //$NON-NLS-1$
                res.getAttributeValues(SAM_ACCOUNT_NAME),
                princName,
                res.getDN()));
        }
    }


    /**
     * Ensure that a machine account object exists under the target DN
     * 
     */
    static SearchResultEntry updateOrCreateMachineAccount ( LDAPClient connection, String accountName, String hostname, String netbiosHostname,
            String rootDN, Collection<String> allowEtypes ) throws LDAPException, ADException {
        SearchResultEntry foundMachineEntry = findMachineEntry(connection, accountName);
        if ( foundMachineEntry != null ) {
            return foundMachineEntry;
        }

        return createMachineAccount(connection, accountName, hostname, netbiosHostname, rootDN, allowEtypes);
    }


    static void setOperatingSystem ( LDAPClient connection, SID sid, ADOSInfo osinfo ) throws ADException, LDAPException {
        SearchResultEntry me = lookupMachineEntry(connection, sid);

        ModifyRequest mod = new ModifyRequest(
            me.getDN(),
            new Modification(ModificationType.REPLACE, "operatingSystem", osinfo.getOSName()), //$NON-NLS-1$
            new Modification(ModificationType.REPLACE, "operatingSystemVersion", osinfo.getOSVersion()), //$NON-NLS-1$
            new Modification(ModificationType.REPLACE, "operatingSystemServicePack", osinfo.getOSServicePack())); //$NON-NLS-1$

        LDAPResult res = connection.modify(mod);
        if ( res.getResultCode() != ResultCode.SUCCESS ) {
            throw new LDAPException(res);
        }
    }


    /**
     * @param connection
     * @param accountName
     * @param rootDN
     * @param netbiosHostname
     * @return
     * @throws LDAPException
     * @throws ADException
     */
    private static SearchResultEntry createMachineAccount ( LDAPClient connection, String accountName, String hostname, String netbiosHostname,
            String rootDN, Collection<String> allowEtypes ) throws LDAPException, ADException {
        DN accountDN = new DN(new RDN(CN, netbiosHostname), connection.relativeDN(rootDN));
        AddRequest req = new AddRequest(
            accountDN.toString(),
            new Attribute(
                OBJECT_CLASS,
                Arrays.asList(
                    "top", //$NON-NLS-1$
                    USER_OBJECT_CLASS,
                    COMPUTER_OBJECT_CLASS,
                    "person", //$NON-NLS-1$
                    "organizationalPerson") //$NON-NLS-1$
            ), new Attribute(CN, netbiosHostname), new Attribute(SAM_ACCOUNT_NAME, accountName), new Attribute(USER_ACCOUNT_CONTROL, String.valueOf(ACCOUNT_FLAGS_DISABLE | ACCOUNT_FLAGS_WSTRUST)), new Attribute(SPN_ATTRIBUTE, Arrays.asList(HOST_INSTANCE + netbiosHostname, HOST_INSTANCE + hostname)), new Attribute(ENCTYPE_ATTRIBUTE, String.valueOf(mapETypes(allowEtypes))));

        LDAPResult res = connection.add(req);
        if ( res.getResultCode() != ResultCode.SUCCESS ) {
            throw new LDAPException(res);
        }

        return connection.getEntry(accountDN.toString());

    }


    /**
     * @param etypes
     * @return
     */
    @SuppressWarnings ( "nls" )
    private static int mapETypes ( Collection<String> etypes ) {
        int val = 0;
        for ( String etype : etypes ) {
            switch ( etype ) {
            case "aes256-cts-hmac-sha1-96":
                val |= 1 << 4;
                break;
            case "aes128-cts-hmac-sha1-96":
                val |= 1 << 3;
                break;
            case "arcfour-hmac-md5":
                val |= 1 << 2;
                break;
            case "des-cbc-md5":
                val |= 1 << 1;
                break;
            // no des-cbc-crc
            }
        }
        return val;
    }


    /**
     * @param cl
     * @param machineSid
     * @return whether the machine account was deleted
     * @throws LDAPException
     * @throws ADException
     */
    public static boolean deleteMachineAccount ( LDAPClient cl, SID machineSid ) throws LDAPException, ADException {
        SearchResultEntry lookupMachineEntry = lookupMachineEntry(cl, machineSid);
        try {
            cl.delete(new DeleteRequest(lookupMachineEntry.getDN()));
            return true;
        }
        catch ( LDAPException e ) {
            if ( e.getResultCode() == ResultCode.INSUFFICIENT_ACCESS_RIGHTS ) {
                log.debug("Could not remove machine account", e); //$NON-NLS-1$
                return false;
            }
            throw e;
        }
    }


    /**
     * @param cl
     * @param machineSid
     * @throws ADException
     * @throws LDAPException
     */
    public static void disableMachineAccount ( LDAPClient cl, SID machineSid ) throws LDAPException, ADException {
        SearchResultEntry lookupMachineEntry = lookupMachineEntry(cl, machineSid);

        ModifyRequest req = new ModifyRequest(
            lookupMachineEntry.getDN(),
            new Modification(ModificationType.REPLACE, String.valueOf(ACCOUNT_FLAGS_DISABLE | ACCOUNT_FLAGS_WSTRUST)));

        cl.modify(req);
    }


    /**
     * @param cl
     * @param machineSid
     * @param passwordToSet
     * @param oldPassword
     * @param allowEtypes
     * @return the new kvno
     * @throws ADException
     * @throws UnsupportedEncodingException
     * @throws LDAPException
     */
    public static int changeMachinePassword ( LDAPClient cl, SID machineSid, String passwordToSet, String oldPassword,
            Collection<String> allowEtypes ) throws ADException, UnsupportedEncodingException, LDAPException {
        return changePassword(cl, lookupMachineEntry(cl, machineSid), passwordToSet, oldPassword, allowEtypes);
    }


    /**
     * @param cl
     * @param realm
     * @param passwordToSet
     * @param oldPassword
     * @param allowEtypes
     * @return the new kvno
     * @throws ADException
     * @throws LDAPException
     * @throws IOException
     * @throws DcerpcException
     */
    public static JoinResult joinWithResetPassword ( LDAPClient cl, ADRealm realm, String passwordToSet, String oldPassword,
            Collection<String> allowEtypes ) throws ADException, LDAPException, DcerpcException, IOException {
        SearchResultEntry findMachineEntry = findMachineEntry(cl, realm.getMachineAccount());
        Attribute attribute = findMachineEntry.getAttribute(OBJECT_SID);

        byte[] sid = attribute.getValueByteArray();

        if ( sid == null ) {
            throw new ADException("No SID found"); //$NON-NLS-1$
        }

        SID machineSid;
        SID domainSid;
        try {
            machineSid = SID.fromBinary(sid);
            domainSid = machineSid.getParent();
        }
        catch ( IllegalArgumentException e ) {
            throw new ADException("Invalid SID", e); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Machine SID " + machineSid.toString()); //$NON-NLS-1$
        }

        int kvno = changePassword(cl, findMachineEntry, passwordToSet, oldPassword, allowEtypes);
        return new JoinResult(passwordToSet, domainSid, machineSid, kvno);

    }


    /**
     * @param cl
     * @param entry
     * @param passwordToSet
     * @return
     * @throws UnsupportedEncodingException
     * @throws LDAPException
     * @throws ADException
     */
    private static int changePassword ( LDAPClient cl, SearchResultEntry entry, String passwordToSet, String oldPassword,
            Collection<String> allowEtypes ) throws UnsupportedEncodingException, LDAPException, ADException {
        final byte[] quotedPassword = String.format("\"%s\"", passwordToSet). //$NON-NLS-1$
                getBytes("UTF-16LE"); //$NON-NLS-1$

        final byte[] oldQuotedPassword = String.format("\"%s\"", oldPassword). //$NON-NLS-1$
                getBytes("UTF-16LE"); //$NON-NLS-1$

        ModifyRequest setEtypes = new ModifyRequest(
            entry.getDN(),
            new Modification(ModificationType.REPLACE, ENCTYPE_ATTRIBUTE, String.valueOf(mapETypes(allowEtypes))));
        cl.modify(setEtypes);

        ModifyRequest pwChange = new ModifyRequest(
            entry.getDN(),
            new Modification(ModificationType.DELETE, "unicodePwd", oldQuotedPassword), //$NON-NLS-1$
            new Modification(ModificationType.ADD, "unicodePwd", quotedPassword)); //$NON-NLS-1$
        LDAPResult r = cl.modify(pwChange);

        if ( r.getResultCode() != ResultCode.SUCCESS ) {
            throw new LDAPException(r);
        }

        SearchResultEntry newEntry = cl.getEntry(entry.getDN(), KVNO_ATTRIBUTE);
        if ( newEntry == null ) {
            throw new ADException("User entry went missing"); //$NON-NLS-1$
        }

        return Integer.parseInt(newEntry.getAttributeValue(KVNO_ATTRIBUTE));
    }


    private static SearchResultEntry findMachineEntry ( LDAPClient connection, String accountName ) throws LDAPSearchException {

        Filter filter = Filter.createANDFilter(
            Filter.createEqualityFilter(OBJECT_CLASS, COMPUTER_OBJECT_CLASS),
            Filter.createEqualityFilter(SAM_ACCOUNT_NAME, accountName));

        SearchResultEntry res = connection.searchForEntry(
            "", //$NON-NLS-1$
            SearchScope.SUB,
            filter,
            SAM_ACCOUNT_NAME,
            OBJECT_SID);

        return res;
    }


    /**
     * @param connection
     * @param attrs
     * @return
     * @throws ADException
     * @throws LDAPException
     */
    private static SearchResultEntry lookupMachineEntry ( LDAPClient connection, SID sid, String... attrs ) throws ADException, LDAPException {
        Filter filter = Filter.createANDFilter(
            Filter.createEqualityFilter(OBJECT_CLASS, COMPUTER_OBJECT_CLASS),
            Filter.createEqualityFilter(OBJECT_SID, sid.toString()));

        SearchResultEntry res = connection.searchForEntry(
            "", //$NON-NLS-1$
            SearchScope.SUB,
            filter,
            attrs);

        if ( res == null ) {
            throw new ADException("Failed to locate machine account with SID " + //$NON-NLS-1$
                    sid.toString() + " in root " + connection.getBaseDN()); //$NON-NLS-1$
        }

        return res;
    }


    /**
     * @param res
     * @return
     * @throws ADException
     */
    private static String validateExistingAccount ( SearchResultEntry res, String machineAccountName ) throws ADException {
        String accountDN = res.getDN();
        String accountName = res.getAttributeValue(SAM_ACCOUNT_NAME);

        if ( !accountName.equalsIgnoreCase(machineAccountName) ) {
            throw new ADException("Mismatch between account names"); //$NON-NLS-1$
        }
        return accountDN;
    }


    /**
     * @param princName
     * @param spns
     */
    private static boolean matchSPNs ( String princName, String[] spns ) {
        if ( spns == null ) {
            return false;
        }
        for ( String spn : spns ) {
            if ( spn.equalsIgnoreCase(princName) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("SPN already exists " + spn); //$NON-NLS-1$
                }
                return true;
            }
        }
        return false;
    }

}
