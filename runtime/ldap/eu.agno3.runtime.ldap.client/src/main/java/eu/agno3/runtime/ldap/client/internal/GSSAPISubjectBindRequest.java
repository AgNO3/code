/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SASLBindRequest;
import com.unboundid.ldap.sdk.SASLQualityOfProtection;

import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public class GSSAPISubjectBindRequest extends SASLBindRequest implements CallbackHandler {

    private static final Logger log = Logger.getLogger(GSSAPISubjectBindRequest.class);

    /**
     * 
     */
    public static final String GSSAPI_MECHANISM_NAME = "GSSAPI"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -5629730208690723919L;

    private GSSAPISubjectFactory subjectFactory;

    private Map<String, Object> saslProperties;


    /**
     * @param controls
     */
    protected GSSAPISubjectBindRequest ( GSSAPISubjectFactory subjectFactory, Map<String, Object> saslProperties, Control[] controls ) {
        super(controls);
        this.subjectFactory = subjectFactory;
        this.saslProperties = new HashMap<>(2);
        this.saslProperties.put(Sasl.QOP, "auth"); //$NON-NLS-1$
        this.saslProperties.put(Sasl.SERVER_AUTH, "true"); //$NON-NLS-1$
        this.saslProperties.putAll(saslProperties);
    }


    /**
     * @param controls
     */
    protected GSSAPISubjectBindRequest ( GSSAPISubjectFactory subjectFactory, Map<String, Object> saslProperties ) {
        this(subjectFactory, saslProperties, new Control[] {});
    }


    /**
     * @param controls
     */
    protected GSSAPISubjectBindRequest ( GSSAPISubjectFactory subjectFactory, Control[] controls ) {
        this(subjectFactory, Collections.EMPTY_MAP, controls);
    }


    /**
     * @param subjectFactory
     */
    public GSSAPISubjectBindRequest ( GSSAPISubjectFactory subjectFactory ) {
        this(subjectFactory, new Control[] {});
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.SASLBindRequest#getSASLMechanismName()
     */
    @Override
    public String getSASLMechanismName () {
        return GSSAPI_MECHANISM_NAME;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.BindRequest#duplicate()
     */
    @Override
    public BindRequest duplicate () {
        return new GSSAPISubjectBindRequest(this.subjectFactory, this.saslProperties, getControls());
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.BindRequest#duplicate(com.unboundid.ldap.sdk.Control[])
     */
    @Override
    public BindRequest duplicate ( Control[] controls ) {
        return new GSSAPISubjectBindRequest(this.subjectFactory, this.saslProperties, controls);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.BindRequest#process(com.unboundid.ldap.sdk.LDAPConnection, int)
     */
    @Override
    protected BindResult process ( final LDAPConnection conn, int depth ) throws LDAPException {
        if ( this.subjectFactory != null ) {
            try {
                Subject subject = createSubject();

                return Subject.doAs(subject, new PrivilegedExceptionAction<BindResult>() {

                    @Override
                    public BindResult run () throws Exception {
                        return doAuth(conn);
                    }
                });
            }
            catch ( PrivilegedActionException e ) {
                log.debug("Failed to perform authentication", e); //$NON-NLS-1$
                if ( e.getCause() instanceof LDAPException ) {
                    throw (LDAPException) e.getCause();
                }
                throw new LDAPException(ResultCode.AUTH_UNKNOWN, e.getCause());
            }
        }

        return doAuth(conn);
    }


    /**
     * @return
     * @throws LDAPException
     */
    protected Subject createSubject () throws LDAPException {
        Subject subject;
        try {
            subject = this.subjectFactory.getSubject();
            if ( subject == null ) {
                throw new LDAPException(ResultCode.AUTH_METHOD_NOT_SUPPORTED, "Failed to get authentication subject"); //$NON-NLS-1$
            }
        }
        catch ( KerberosException e ) {
            throw new LDAPException(ResultCode.INVALID_CREDENTIALS, e.getMessage(), e); // $NON-NLS-1$
        }
        return subject;
    }


    /**
     * Shamelessly copied from UnboundId
     * 
     * @param conn
     * @return
     * @throws LDAPException
     */
    protected BindResult doAuth ( LDAPConnection conn ) throws LDAPException {
        final String[] mechanisms = {
            GSSAPI_MECHANISM_NAME
        };

        final SaslClient saslClient;
        try {
            saslClient = Sasl.createSaslClient(mechanisms, null, "ldap", conn.getConnectedAddress(), this.saslProperties, this); //$NON-NLS-1$
            return processSASLBind(this, saslClient, conn);
        }
        catch ( Exception e ) {
            throw new LDAPException(ResultCode.LOCAL_ERROR, "Failed to create SASL client", e); //$NON-NLS-1$
        }
    }


    /**
     * Performs a SASL bind against an LDAP directory server.
     * 
     * Shamelessly copied from UnboundId
     *
     * @return The result of the bind operation processing.
     *
     * @throws LDAPException
     *             If a problem occurs while processing the bind.
     */
    BindResult processSASLBind ( SASLBindRequest bindRequest, SaslClient saslClient, LDAPConnection conn ) throws LDAPException {
        try {
            // Get the SASL credentials for the initial request.
            byte[] credBytes = makeInitialCredentials(saslClient);
            ASN1OctetString saslCredentials = toASN1(credBytes);

            BindResult bindResult = this.sendBindRequest(conn, StringUtils.EMPTY, saslCredentials, getControls(), getResponseTimeoutMillis(conn));

            if ( !bindResult.getResultCode().equals(ResultCode.SASL_BIND_IN_PROGRESS) ) {
                return bindResult;
            }

            byte[] serverCredBytes = bindResult.getServerSASLCredentials().getValue();

            while ( true ) {
                try {
                    credBytes = saslClient.evaluateChallenge(serverCredBytes);
                }
                catch ( Exception e ) {
                    throw new LDAPException(ResultCode.LOCAL_ERROR, "SASL exchange failed", e); //$NON-NLS-1$
                }

                saslCredentials = toASN1(credBytes);
                bindResult = this.sendBindRequest(conn, StringUtils.EMPTY, saslCredentials, getControls(), getResponseTimeoutMillis(conn));
                if ( !bindResult.getResultCode().equals(ResultCode.SASL_BIND_IN_PROGRESS) ) {
                    return bindResult;
                }

                serverCredBytes = bindResult.getServerSASLCredentials().getValue();
            }
        }
        finally {
            boolean hasNegotiatedSecurity = checkSASLQoP(saslClient);
            if ( hasNegotiatedSecurity ) {
                setupSASLQoP(saslClient, conn);
            }

            if ( !hasNegotiatedSecurity ) {
                try {
                    saslClient.dispose();
                }
                catch ( Exception e ) {
                    log.warn("Failed to dispose SASL client", e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * @param saslClient
     * @return
     * @throws LDAPException
     */
    protected byte[] makeInitialCredentials ( SaslClient saslClient ) throws LDAPException {
        byte[] credBytes = null;
        try {
            if ( saslClient.hasInitialResponse() ) {
                credBytes = saslClient.evaluateChallenge(new byte[0]);
            }
        }
        catch ( Exception e ) {
            throw new LDAPException(ResultCode.LOCAL_ERROR, "SASL initial request failed", e); //$NON-NLS-1$
        }
        return credBytes;
    }


    /**
     * @param credBytes
     * @return
     */
    protected ASN1OctetString toASN1 ( byte[] credBytes ) {
        ASN1OctetString saslCredentials;
        if ( ( credBytes == null ) || ( credBytes.length == 0 ) ) {
            saslCredentials = null;
        }
        else {
            saslCredentials = new ASN1OctetString(credBytes);
        }
        return saslCredentials;
    }


    /**
     * @param saslClient
     * @return
     */
    private static boolean checkSASLQoP ( SaslClient saslClient ) {
        boolean hasNegotiatedSecurity = false;
        if ( saslClient.isComplete() ) {
            final Object qopObject = saslClient.getNegotiatedProperty(Sasl.QOP);
            if ( qopObject != null ) {
                final String qopString = String.valueOf(qopObject).toLowerCase(Locale.ROOT);
                if ( qopString.contains(SASLQualityOfProtection.AUTH_INT.toString())
                        || qopString.contains(SASLQualityOfProtection.AUTH_CONF.toString()) ) {
                    hasNegotiatedSecurity = true;
                }
            }
        }
        return hasNegotiatedSecurity;
    }


    /**
     * @param saslClient
     * @param conn
     * @throws LDAPException
     */
    private static void setupSASLQoP ( SaslClient saslClient, LDAPConnection conn ) throws LDAPException {
        log.debug("Integrity or confidentialitiy enabled"); //$NON-NLS-1$

        try {
            Method method = conn.getClass().getDeclaredMethod("applySASLQoP", SaslClient.class); //$NON-NLS-1$
            method.setAccessible(true);
            method.invoke(conn, saslClient);
        }
        catch (
            NoSuchMethodException |
            SecurityException |
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            try {
                saslClient.dispose();
            }
            catch ( Exception ex ) {
                log.warn("Failed to dispose SASL client", ex); //$NON-NLS-1$
            }
            throw new LDAPException(ResultCode.AUTH_METHOD_NOT_SUPPORTED, "Failed to setup SASL conf/integ", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.sdk.LDAPRequest#toString(java.lang.StringBuilder)
     */
    @Override
    public void toString ( StringBuilder sb ) {
        sb.append("GSSAPIBindRequest(subject="); //$NON-NLS-1$
        sb.append(this.subjectFactory);
        sb.append(')');
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    @Override
    public void handle ( Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
        for ( Callback cb : callbacks ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Recieved callback " + cb); //$NON-NLS-1$
            }
            throw new UnsupportedCallbackException(cb);
        }

    }
}
