/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import javax.security.auth.RefreshFailedException;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.KeyTab;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import sun.security.jgss.krb5.Krb5Util;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbAsReqBuilder;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.PAData;
import sun.security.krb5.internal.PAData.SaltAndParams;
import sun.security.krb5.internal.crypto.EType;


/**
 * @author mbechler
 *
 */
public class Krb5SubjectUtil {

    private static final Logger log = Logger.getLogger(Krb5SubjectUtil.class);


    /**
     * @param keytab
     * @param serviceName
     * @param realm
     * @return a subject with the kerberos accept credentials
     * @throws KerberosException
     * 
     */
    public static Subject getAcceptorSubject ( KeyTab keytab, final String serviceName, final String realm ) throws KerberosException {
        String fullServicePrincipal = String.format("%s@%s", serviceName, realm); //$NON-NLS-1$
        KerberosPrincipal principal = new KerberosPrincipal(fullServicePrincipal, KerberosPrincipal.KRB_NT_PRINCIPAL);
        return getAcceptorSubject(keytab, principal);
    }


    /**
     * 
     * @param principal
     * @param passwords
     * @param overrideSalts
     * @param ticketCacheName
     * @param renewTGT
     * @return a subject with both initiator and acceptor credentials
     * @throws KerberosException
     */
    public static Subject getInitiateAndAcceptSubject ( KerberosPrincipal principal, SortedMap<Integer, String> passwords,
            Map<Integer, String> overrideSalts, String ticketCacheName, boolean renewTGT ) throws KerberosException {
        Set<Object> privCreds = new HashSet<>();
        if ( passwords.isEmpty() ) {
            throw new KerberosException("Missing password"); //$NON-NLS-1$
        }
        KerberosTicket tkt = getKerberosTicket(principal, passwords.get(passwords.lastKey()), ticketCacheName, renewTGT);
        // the proper, actual server provided salt could be extracted from the AS-REP
        privCreds.addAll(getAcceptorKeys(principal, passwords, overrideSalts));
        privCreds.add(tkt);
        return new Subject(true, new HashSet<>(Arrays.asList((Principal) principal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param servicePrincipal
     * @param passwords
     * @param overrideSalts
     * @return a subject with the kerberos accept credentials
     * @throws KerberosException
     */
    public static Subject getAcceptorSubject ( javax.security.auth.kerberos.KerberosPrincipal servicePrincipal, SortedMap<Integer, String> passwords,
            Map<Integer, String> overrideSalts ) throws KerberosException {
        Set<Object> privCreds = new HashSet<>();
        if ( passwords.isEmpty() ) {
            throw new KerberosException("Missing password"); //$NON-NLS-1$
        }
        privCreds.addAll(getAcceptorKeys(servicePrincipal, passwords, overrideSalts));
        return new Subject(true, new HashSet<>(Arrays.asList((Principal) servicePrincipal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param servicePrincipal
     * @param password
     * @return
     * @throws KerberosException
     */
    private static List<KerberosKey> getAcceptorKeys ( javax.security.auth.kerberos.KerberosPrincipal servicePrincipal,
            SortedMap<Integer, String> passwords, Map<Integer, String> overrideSalts ) throws KerberosException {
        String[] algos = new String[] {
            "DES", //$NON-NLS-1$
            "DESede", //$NON-NLS-1$
            "AES128", //$NON-NLS-1$
            "AES256", //$NON-NLS-1$
            "ArcFourHmac" //$NON-NLS-1$
        };
        List<KerberosKey> keys = new LinkedList<>();

        try {
            PrincipalName princ = new PrincipalName(servicePrincipal.getName());
            for ( Entry<Integer, String> e : passwords.entrySet() ) {
                for ( String algo : algos ) {
                    int etype = ETypesUtil.getEtypeFromAlgo(algo);
                    EncryptionKey key = deriveKey(princ, e, algo, overrideSalts.get(etype));
                    keys.add(new KerberosKey(servicePrincipal, key.getBytes(), etype, e.getKey()));
                }
            }
        }
        catch (
            RealmException |
            KrbCryptoException e ) {
            throw new KerberosException("Failed to get principal keys", e); //$NON-NLS-1$
        }
        return keys;
    }


    private static EncryptionKey deriveKey ( PrincipalName princ, Entry<Integer, String> e, String algo, String overrideSalt )
            throws KrbCryptoException {
        if ( overrideSalt != null ) {
            return new EncryptionKey(e.getValue().toCharArray(), overrideSalt, algo);
        }
        return new EncryptionKey(e.getValue().toCharArray(), princ.getSalt(), algo);
    }


    /**
     * @param keytab
     * @param servicePrincipal
     * @return a subject with appropriate service keys
     * @throws KerberosException
     */
    public static Subject getAcceptorSubject ( KeyTab keytab, KerberosPrincipal servicePrincipal ) throws KerberosException {
        KerberosKey[] keys = keytab.getKeys(servicePrincipal);

        if ( keys == null || keys.length == 0 ) {
            throw new KerberosException("The keytab does not contain the requested principal " + servicePrincipal.getName()); //$NON-NLS-1$
        }

        Set<Object> privCreds = new HashSet<>();
        privCreds.add(keytab);
        privCreds.addAll(Arrays.asList(keys));
        return new Subject(true, new HashSet<>(Arrays.asList((Principal) servicePrincipal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param keytab
     * @param principal
     * @param ticketCacheName
     * @param renewTGT
     * @return a subjct with the kerberos initiate credentials (TGT)
     * @throws KerberosException
     */
    public static Subject getInitiatorSubject ( KeyTab keytab, final KerberosPrincipal principal, String ticketCacheName, boolean renewTGT )
            throws KerberosException {
        KerberosTicket ticket = getKerberosTicket(keytab, principal, ticketCacheName, renewTGT);
        Set<Object> privCreds = new HashSet<>();
        privCreds.add(ticket);
        return new Subject(false, new HashSet<>(Arrays.asList((Principal) principal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param keytab
     * @param principal
     * @param ticketCacheName
     * @param renewTGT
     * @return
     * @throws KerberosException
     */
    private static KerberosTicket getKerberosTicket ( KeyTab keytab, final KerberosPrincipal principal, String ticketCacheName, boolean renewTGT )
            throws KerberosException {
        PrincipalName principalName;
        try {
            principalName = new PrincipalName(principal.toString(), PrincipalName.KRB_NT_PRINCIPAL);
        }
        catch ( RealmException e ) {
            throw new KerberosException("Failed to parse principal name", e); //$NON-NLS-1$
        }

        Credentials creds = getCredentialsFromCache(ticketCacheName, renewTGT, principalName);
        if ( creds != null ) {
            principalName = creds.getClient();
        }

        if ( creds == null ) {
            EncryptionKey[] keys = Krb5Util.keysFromJavaxKeyTab(keytab, principalName);

            if ( keys == null || keys.length == 0 ) {
                throw new KerberosException("Could not find any keys in keytab for " + principalName); //$NON-NLS-1$
            }

            try {
                KrbAsReqBuilder builder = new KrbAsReqBuilder(principalName, keytab);
                creds = builder.action().getCreds();
                builder.destroy();
            }
            catch (
                KrbException |
                IOException e ) {
                throw new KerberosException("AS req failed", e); //$NON-NLS-1$
            }
        }

        return Krb5Util.credsToTicket(creds);
    }


    /**
     * @param principal
     * @param password
     * @param ticketCacheName
     * @param renewTGT
     * @return a subjct with the kerberos initiate credentials (TGT)
     * @throws KerberosException
     */
    public static Subject getInitiatorSubject ( KerberosPrincipal principal, String password, String ticketCacheName, boolean renewTGT )
            throws KerberosException {
        KerberosTicket ticket = getKerberosTicket(principal, password, ticketCacheName, renewTGT);
        Set<Object> privCreds = new HashSet<>();
        privCreds.add(ticket);
        return new Subject(false, new HashSet<>(Arrays.asList((Principal) principal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param principal
     * @param password
     * @param ticketCacheName
     * @param renewTGT
     * @return
     * @throws KerberosException
     */
    private static KerberosTicket getKerberosTicket ( KerberosPrincipal principal, String password, String ticketCacheName, boolean renewTGT )
            throws KerberosException {
        PrincipalName principalName;
        try {
            principalName = new PrincipalName(principal.getName(), PrincipalName.KRB_NT_PRINCIPAL, principal.getRealm());
        }
        catch ( RealmException e ) {
            throw new KerberosException("Failed to parse principal name", e); //$NON-NLS-1$
        }

        Credentials creds = getCredentialsFromCache(ticketCacheName, renewTGT, principalName);
        if ( creds != null ) {
            principalName = creds.getClient();
        }

        if ( creds == null ) {
            try {
                KrbAsReqBuilder builder = new KrbAsReqBuilder(principalName, password != null ? password.toCharArray() : new char[0]);
                KrbAsReqBuilder done = builder.action();
                debugSalts(done, ETypesUtil.ALL_ETYPE_CODES);
                creds = done.getCreds();
                builder.destroy();
            }
            catch ( KrbException e ) {
                KRBError error = e.getError();
                if ( error != null ) {
                    handleError(principal, e, error);
                }
                throw new KerberosException("Failed to get TGT for " + principal, e); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                throw new KerberosException("AS req failed for " + principal, e); //$NON-NLS-1$
            }
        }

        if ( creds == null ) {
            throw new KerberosException("Failed to get credentials for " + principalName); //$NON-NLS-1$
        }

        return Krb5Util.credsToTicket(creds);
    }


    /**
     * @param done
     * @return
     */
    private static Map<Integer, String> debugSalts ( KrbAsReqBuilder done, int[] etypes ) {
        try {
            Map<Integer, String> salts = new HashMap<>();
            Field paList = done.getClass().getDeclaredField("paList"); //$NON-NLS-1$
            paList.setAccessible(true);
            PAData[] pas = (PAData[]) paList.get(done);

            for ( int etype : etypes ) {
                SaltAndParams salt = PAData.getSaltAndParams(etype, pas);
                if ( salt != null ) {
                    salts.put(etype, salt.salt);
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Salts are " + salts); //$NON-NLS-1$
            }
            return salts;
        }
        catch ( Exception e ) {
            log.warn("Failed to extract salts from AS-REP", e); //$NON-NLS-1$
            return Collections.EMPTY_MAP;
        }
    }


    /**
     * @param principal
     * @param e
     * @param error
     * @throws KerberosInvalidCredentialsException
     * @throws KerberosPrincipalNotFoundException
     * @throws KerberosException
     * @throws KerberosUnsupportedETypesException
     * @throws KerberosCredentialsExpiredException
     */
    static void handleError ( KerberosPrincipal principal, KrbException e, KRBError error ) throws KerberosException {

        switch ( error.getErrorCode() ) {
        case 0x6:
            throw new KerberosPrincipalNotFoundException(
                String.format("Principal does not exist: '%s'", principal), //$NON-NLS-1$
                principal,
                e);
        case 0xE:
            try {
                String[] et = ETypesUtil.mapETypes(EType.getDefaults("permitted_enctypes")); //$NON-NLS-1$
                throw new KerberosUnsupportedETypesException("KDC does not support any requested EType", et, e); //$NON-NLS-1$
            }
            catch ( KrbException e1 ) {
                log.debug("Failed to get ticket ETypes", e1); //$NON-NLS-1$
            }
        case 0x17:
            throw new KerberosCredentialsExpiredException(
                String.format("Kerberos preauthentication failed, wrong password for '%s'?", principal), //$NON-NLS-1$
                principal,
                e);
        case 0x1F:
            throw new KerberosInvalidCredentialsException(
                String.format("Kerberos integrity check failed, wrong password for '%s'?", principal), //$NON-NLS-1$
                principal,
                e);
        case 0x18:
            throw new KerberosInvalidCredentialsException(
                String.format("Kerberos preauthentication failed, wrong password for '%s'?", principal), //$NON-NLS-1$
                principal,
                e);
        case 0x25:
            throw new KerberosClockSkewException("Clock skew is too great", e); //$NON-NLS-1$
        }
    }


    /**
     * @param subject
     * @return whether we have a valid TGT (with more than 5 minutes lifetime)
     */
    public static boolean hasValidTGT ( Subject subject ) {
        for ( KerberosTicket kerberosTicket : subject.getPrivateCredentials(KerberosTicket.class) ) {
            KerberosPrincipal client = kerberosTicket.getClient();
            KerberosPrincipal server = kerberosTicket.getServer();
            if ( !StringUtils.equals(client.getRealm(), server.getRealm()) ) {
                continue;
            }
            if ( !server.getName().startsWith("krbtgt/" + client.getRealm()) ) { //$NON-NLS-1$
                continue;
            }
            if ( !kerberosTicket.isCurrent() ) {
                log.debug("Found expired TGT"); //$NON-NLS-1$
                if ( log.isTraceEnabled() ) {
                    log.trace("Expired ticket " + kerberosTicket); //$NON-NLS-1$
                }
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found valid TGT for %s (%s)", client, server)); //$NON-NLS-1$
            }
            if ( log.isTraceEnabled() ) {
                log.trace("Have valid TGT " + kerberosTicket); //$NON-NLS-1$
            }
            return true;
        }
        return false;
    }


    /**
     * @param userName
     * @param password
     * @param realm
     * @param ticketCacheName
     * @param renewTGT
     * @return a subjct with the kerberos initiate credentials (TGT)
     * @throws KerberosException
     */
    public static Subject getInitiatorSubject ( String userName, String password, String realm, String ticketCacheName, boolean renewTGT )
            throws KerberosException {
        String fullPrincipal = String.format("%s@%s", userName, realm); //$NON-NLS-1$
        KerberosPrincipal principal = new KerberosPrincipal(fullPrincipal, KerberosPrincipal.KRB_NT_PRINCIPAL);
        return getInitiatorSubject(principal, password, ticketCacheName, renewTGT);
    }


    /**
     * @param ticketCacheName
     * @param renewTGT
     * @param principalName
     * @return
     * @throws KerberosException
     */
    private static Credentials getCredentialsFromCache ( String ticketCacheName, boolean renewTGT, PrincipalName principalName )
            throws KerberosException {

        if ( StringUtils.isBlank(ticketCacheName) ) {
            return null;
        }
        Credentials creds;
        try {
            creds = Credentials.acquireTGTFromCache(principalName, ticketCacheName);
            if ( creds != null && !isCurrent(creds) ) {
                if ( renewTGT && creds.isRenewable() ) {
                    creds = renewCredentials(creds);
                }
                else {
                    log.debug("Credentials expired"); //$NON-NLS-1$
                    creds = null;
                }
            }
        }
        catch (
            KrbException |
            IOException e ) {
            throw new KerberosException("Failed to get credentials from cache", e); //$NON-NLS-1$
        }
        return creds;
    }


    private static boolean isCurrent ( Credentials creds ) {
        Date endTime = creds.getEndTime();
        if ( endTime != null ) {
            return ( System.currentTimeMillis() <= endTime.getTime() );
        }
        return true;
    }


    private static Credentials renewCredentials ( Credentials creds ) {
        Credentials lcreds;
        try {
            if ( !creds.isRenewable() )
                throw new RefreshFailedException("This ticket is not renewable"); //$NON-NLS-1$
            if ( System.currentTimeMillis() > creds.getRenewTill().getTime() )
                throw new RefreshFailedException("This ticket is past its last renewal time."); //$NON-NLS-1$
            lcreds = creds.renew();
        }
        catch ( Exception e ) {
            log.debug("Failed to renew credentails", e); //$NON-NLS-1$
            lcreds = null;
        }
        return lcreds;
    }


    /**
     * @param initiatorPrincipal
     * @param initiatorSubject
     * @param acceptorPrincipal
     * @param acceptorSubject
     * @param lifetime
     * @return the established gss context
     * @throws IOException
     * @throws GSSException
     */
    public static GSSContext validateServiceCredentials ( KerberosPrincipal initiatorPrincipal, Subject initiatorSubject,
            KerberosPrincipal acceptorPrincipal, Subject acceptorSubject, final int lifetime ) throws IOException, GSSException {

        final Oid krb5Mech = new Oid("1.2.840.113554.1.2.2"); //$NON-NLS-1$
        final Oid krb5PrincName = new Oid("1.2.840.113554.1.2.2.1"); //$NON-NLS-1$
        final GSSManager manager = GSSManager.getInstance();
        final GSSName initiatorName = manager.createName(initiatorPrincipal.getName(), krb5PrincName);
        final GSSName acceptorName = manager.createName(acceptorPrincipal.getName(), krb5PrincName);

        if ( log.isDebugEnabled() ) {
            log.debug("initiator " + initiatorName); //$NON-NLS-1$
            log.debug("acceptor " + acceptorName); //$NON-NLS-1$
        }

        GSSContext acceptorContext;
        GSSContext initiateContext;
        try {
            acceptorContext = createAcceptorContext(acceptorSubject, krb5Mech, manager, acceptorName);
            initiateContext = createInitiatorContext(initiatorSubject, lifetime, krb5Mech, manager, initiatorName, acceptorName);

            try {
                return doGSSAPIExchange(initiatorSubject, acceptorSubject, acceptorContext, initiateContext);
            }
            catch ( PrivilegedActionException e ) {
                acceptorContext.dispose();
                throw e;
            }
            finally {
                initiateContext.dispose();
            }

        }
        catch ( PrivilegedActionException e ) {
            log.debug("Caught", e); //$NON-NLS-1$
            if ( e.getCause() instanceof GSSException ) {
                throw (GSSException) e.getCause();
            }
            else if ( e.getCause() instanceof IOException ) {
                throw (IOException) e.getCause();
            }
            else if ( e.getCause() instanceof RuntimeException ) {
                throw (RuntimeException) e.getCause();
            }
            else {
                throw new RuntimeException(e.getCause());
            }
        }

    }


    /**
     * @param initiatorSubject
     * @param lifetime
     * @param krb5Mech
     * @param manager
     * @param initiatorName
     * @param acceptorName
     * @return
     * @throws PrivilegedActionException
     */
    protected static GSSContext createInitiatorContext ( Subject initiatorSubject, final int lifetime, final Oid krb5Mech, final GSSManager manager,
            final GSSName initiatorName, final GSSName acceptorName ) throws PrivilegedActionException {
        return Subject.doAs(initiatorSubject, new PrivilegedExceptionAction<GSSContext>() {

            @Override
            public GSSContext run () throws GSSException {
                return manager.createContext(
                    acceptorName,
                    krb5Mech,
                    manager.createCredential(initiatorName, lifetime, krb5Mech, GSSCredential.INITIATE_ONLY),
                    lifetime);
            }
        });
    }


    /**
     * @param acceptorSubject
     * @param krb5Mech
     * @param manager
     * @param acceptorName
     * @return
     * @throws PrivilegedActionException
     */
    protected static GSSContext createAcceptorContext ( Subject acceptorSubject, final Oid krb5Mech, final GSSManager manager,
            final GSSName acceptorName ) throws PrivilegedActionException {
        return Subject.doAs(acceptorSubject, new PrivilegedExceptionAction<GSSContext>() {

            @Override
            public GSSContext run () throws GSSException {
                return manager.createContext(
                    manager.createCredential(acceptorName, GSSCredential.INDEFINITE_LIFETIME, krb5Mech, GSSCredential.ACCEPT_ONLY));
            }

        });
    }


    /**
     * @param initiatorSubject
     * @param acceptorSubject
     * @param acceptorContext
     * @param initiateContext
     * @return
     * @throws PrivilegedActionException
     */
    protected static GSSContext doGSSAPIExchange ( Subject initiatorSubject, Subject acceptorSubject, GSSContext acceptorContext,
            GSSContext initiateContext ) throws PrivilegedActionException {
        byte[] itoa = doInit(initiatorSubject, initiateContext, new byte[0]);
        byte[] atoi = doAccept(acceptorSubject, acceptorContext, itoa);
        int limit = 5;

        while ( limit > 0 && ( !acceptorContext.isEstablished() || !initiateContext.isEstablished() ) ) {
            if ( atoi != null && atoi.length != 0 ) {
                itoa = doInit(initiatorSubject, initiateContext, atoi);
            }

            if ( itoa != null && itoa.length != 0 ) {
                atoi = doAccept(acceptorSubject, acceptorContext, itoa);
            }

            limit--;
        }

        if ( !initiateContext.isEstablished() || !acceptorContext.isEstablished() ) {
            log.warn("GSS Context not established"); //$NON-NLS-1$
            return null;
        }

        return acceptorContext;
    }


    /**
     * @param acceptorSubject
     * @param initiateContext
     * @return
     * @throws PrivilegedActionException
     */
    private static byte[] doInit ( Subject acceptorSubject, GSSContext initiateContext, byte[] buf ) throws PrivilegedActionException {
        return Subject.doAs(acceptorSubject, new PrivilegedExceptionAction<byte[]>() {

            @Override
            public byte[] run () throws GSSException {
                return initiateContext.initSecContext(buf, 0, buf != null ? buf.length : 0);

            }
        });
    }


    /**
     * @param subject
     * @param context
     * @param buf
     * @return
     * @throws PrivilegedActionException
     */
    private static byte[] doAccept ( Subject subject, GSSContext context, byte[] buf ) throws PrivilegedActionException {
        return Subject.doAs(subject, new PrivilegedExceptionAction<byte[]>() {

            @Override
            public byte[] run () throws GSSException {
                return context.acceptSecContext(buf, 0, buf != null ? buf.length : 0);
            }
        });
    }


    /**
     * @param e
     * @return the embedded error code, or -1 if not found
     */
    public static int getErrorCode ( KerberosException e ) {
        if ( e != null && e.getCause() instanceof KrbException ) {
            KrbException ex = (KrbException) e.getCause();
            if ( ex.getError() != null ) {
                return ex.getError().getErrorCode();
            }
        }

        return -1;
    }


    /**
     * @param e
     * @return the embedded error message, or null if not found
     */
    public static String getErrorMessage ( KerberosException e ) {
        if ( e != null && e.getCause() instanceof KrbException ) {
            KrbException ex = (KrbException) e.getCause();
            if ( ex.getError() != null ) {
                return ex.getError().getErrorString();
            }
        }

        return null;
    }

}
