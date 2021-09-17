/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2015 by mbechler
 */
package eu.agno3.runtime.security.krb.internal;


import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import eu.agno3.runtime.net.krb5.KerberosException;

import jcifs.pac.PACDecodingException;
import jcifs.pac.kerberos.KerberosAuthData;
import jcifs.pac.kerberos.KerberosRelevantAuthData;


/**
 * @author mbechler
 *
 */
public class SPNEGOAcceptor {

    private static final Logger log = Logger.getLogger(SPNEGOAcceptor.class);

    private GSSManager manager;
    private GSSName acceptorName;
    private Subject acceptorSubject;

    private final Oid spnegoMechOid;
    private final Oid krbMechOid;

    private KerberosKey[] keys;

    private KerberosPrincipal servicePrincipal;


    /**
     * @param subj
     * @param servicePrincipal
     * @throws KerberosException
     * 
     */
    public SPNEGOAcceptor ( Subject subj, KerberosPrincipal servicePrincipal ) throws KerberosException {
        this.manager = GSSManager.getInstance();
        this.acceptorSubject = subj;
        this.servicePrincipal = servicePrincipal;

        this.keys = this.acceptorSubject.getPrivateCredentials(KerberosKey.class).toArray(new KerberosKey[] {});

        Oid krb5PrincipalNameType;
        try {
            this.spnegoMechOid = new Oid("1.3.6.1.5.5.2"); //$NON-NLS-1$
            this.krbMechOid = new Oid("1.2.840.113554.1.2.2"); //$NON-NLS-1$
            krb5PrincipalNameType = new Oid("1.2.840.113554.1.2.2.1"); //$NON-NLS-1$

        }
        catch ( GSSException e ) {
            throw new KerberosException("Failed to create OIDs", e); //$NON-NLS-1$
        }

        try {
            this.acceptorName = this.manager.createName(servicePrincipal.getName(), krb5PrincipalNameType);
        }
        catch ( GSSException e ) {
            throw new KerberosException("Failed to parse principal name", e); //$NON-NLS-1$
        }

    }


    /**
     * @param updatedSubject
     */
    public void updateSubject ( Subject updatedSubject ) {
        this.acceptorSubject = updatedSubject;
        this.keys = this.acceptorSubject.getPrivateCredentials(KerberosKey.class).toArray(new KerberosKey[] {});
    }


    /**
     * @return
     */
    Oid[] getAcceptedMechanisms () {
        return new Oid[] {
            this.spnegoMechOid, this.krbMechOid
        };
    }


    /**
     * @return the servicePrincipal
     */
    public KerberosPrincipal getServicePrincipal () {
        return this.servicePrincipal;
    }


    /**
     * @return
     */
    GSSName getAcceptorName () {
        return this.acceptorName;
    }


    /**
     * @return the acceptorSubject
     */
    public Subject getAcceptorSubject () {
        return this.acceptorSubject;
    }


    /**
     * @return the manager
     */
    GSSManager getManager () {
        return this.manager;
    }


    /**
     * @return a GSS Context
     * @throws GSSException
     */
    public GSSContext createContext () throws GSSException {
        if ( this.acceptorSubject == null ) {
            throw new GSSException(GSSException.FAILURE);
        }
        log.debug("Creating context"); //$NON-NLS-1$
        try {
            return doCreateContext();
        }
        catch ( PrivilegedActionException e ) {
            if ( e.getException() instanceof RuntimeException ) {
                throw (RuntimeException) e.getException();
            }
            else if ( e.getException() instanceof GSSException ) {
                throw (GSSException) e.getException();
            }
            else {
                throw new RuntimeException(e);
            }

        }
    }


    /**
     * @return
     * @throws PrivilegedActionException
     */
    protected GSSContext doCreateContext () throws PrivilegedActionException {
        return Subject.doAs(this.acceptorSubject, new PrivilegedExceptionAction<GSSContext>() {

            @Override
            public GSSContext run () throws GSSException {
                GSSCredential credential = getManager()
                        .createCredential(getAcceptorName(), GSSCredential.INDEFINITE_LIFETIME, getAcceptedMechanisms(), GSSCredential.ACCEPT_ONLY);
                GSSContext ctx = getManager().createContext(credential);
                return ctx;
            }
        });
    }


    /**
     * @param ctx
     * @param token
     * @return an output token if required
     * @throws GSSException
     */
    public byte[] doAccept ( final GSSContext ctx, byte[] token ) throws GSSException {

        if ( this.acceptorSubject == null ) {
            throw new GSSException(GSSException.FAILURE);
        }

        log.debug("accept()"); //$NON-NLS-1$

        try {
            return Subject.doAs(this.acceptorSubject, new PrivilegedExceptionAction<byte[]>() {

                @Override
                public byte[] run () throws GSSException {
                    return ctx.acceptSecContext(token, 0, token.length);
                }

            });
        }
        catch ( PrivilegedActionException e ) {
            if ( e.getException() instanceof RuntimeException ) {
                throw (RuntimeException) e.getException();
            }
            else if ( e.getException() instanceof GSSException ) {
                throw (GSSException) e.getException();
            }
            else {
                throw new RuntimeException(e);
            }

        }

    }


    /**
     * 
     * @param ctx
     * @param token
     * @return base64 encoded output token
     * @throws GSSException
     */
    public String doAccept ( GSSContext ctx, String token ) throws GSSException {

        byte[] res = doAccept(ctx, Base64.decodeBase64(token));

        if ( res == null || res.length == 0 ) {
            if ( !ctx.isEstablished() ) {
                throw new GSSException(GSSException.DEFECTIVE_TOKEN);
            }
            return null;
        }

        return Base64.encodeBase64String(res);
    }


    /**
     * @return the service keys
     */
    public KerberosKey[] getServiceKeys () {
        if ( this.keys == null ) {
            return null;
        }
        return Arrays.copyOf(this.keys, this.keys.length);
    }


    /**
     * @param data
     * @param kvno
     * @return the parsed authentication data
     * @throws PACDecodingException
     * @throws KerberosException
     */
    public List<KerberosAuthData> parseAuthData ( byte[] data, int kvno ) throws PACDecodingException, KerberosException {
        Map<Integer, KerberosKey> serviceKeys = selectKeys(getAcceptorSubject(), kvno);
        return ( new KerberosRelevantAuthData(data, serviceKeys) ).getAuthorizations();
    }


    /**
     * @param algo
     * @return
     * @throws KerberosException
     */
    private static Map<Integer, KerberosKey> selectKeys ( Subject acceptorSubject, int kvno ) throws KerberosException {
        Map<Integer, KerberosKey> keys = new HashMap<>();
        for ( KerberosKey k : acceptorSubject.getPrivateCredentials(KerberosKey.class) ) {
            if ( k.getVersionNumber() == kvno ) {
                keys.put(k.getKeyType(), k);
            }
        }

        if ( keys.isEmpty() ) {
            throw new KerberosException("Failed to find key for KVNO " + kvno); //$NON-NLS-1$
        }

        return keys;
    }

}
