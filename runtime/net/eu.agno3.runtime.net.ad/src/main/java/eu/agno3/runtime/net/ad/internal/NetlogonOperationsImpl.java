/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADUserInfo;
import eu.agno3.runtime.net.ad.ADUserInfoImpl;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.ad.NetlogonOperations;
import eu.agno3.runtime.net.ad.msgs.KerbVerifyPACRequest;
import eu.agno3.runtime.net.ad.msgs.NetlogonGenericInformation;
import eu.agno3.runtime.net.ad.msgs.NetlogonIdentityInfo;
import eu.agno3.runtime.net.ad.msgs.NetlogonNetworkInfo;
import eu.agno3.runtime.net.ad.msgs.NetlogonValidationGenericInfo2;
import eu.agno3.runtime.net.ad.msgs.NetlogonValidationSamInfo;
import eu.agno3.runtime.net.ad.msgs.NetrLogonSamLogonWithFlags;

import jcifs.dcerpc.DcerpcException;
import jcifs.ntlmssp.Type3Message;
import jcifs.pac.Pac;


/**
 * @author mbechler
 *
 */
public class NetlogonOperationsImpl implements NetlogonOperations {

    /**
     * 
     */
    private static final int STATUS_LOGON_FAILURE = 0xC000006D;

    /**
     * 
     */
    private static final int STATUS_ACCESS_DENIED = 0xc0000022;

    private static final Logger log = Logger.getLogger(NetlogonOperationsImpl.class);

    private NetlogonConnection conn;


    /**
     * @param conn
     * 
     */
    public NetlogonOperationsImpl ( NetlogonConnection conn ) {
        this.conn = conn;
    }


    @Override
    public ADUserInfo pacValidate ( Pac pac ) throws ADException {
        return pacValidate(pac, false);
    }


    /**
     * @param pac
     * @param fallback
     * @return
     * @throws ADException
     */
    private ADUserInfo pacValidate ( Pac pac, boolean isRetry ) throws ADException {
        try {
            NetrLogonSamLogonWithFlags logon = makeSAMRLogonPAC(pac);

            boolean failed = true;
            try {
                this.conn.getDcerpcHandle().sendrecv(logon);
                failed = false;
            }
            finally {
                // don't validate on access denied error, this indicates that our authenticator was wrong
                if ( !failed || logon.getResult().getErrorCode() != STATUS_ACCESS_DENIED ) {
                    this.conn.validate(logon.getReturnAuthenticator(), failed);
                }
            }

            if ( logon.getAuthorative() != 1 ) {
                throw new ADException("Result is not authorative " + logon.getAuthorative()); //$NON-NLS-1$
            }

            return ADUserInfoImpl.fromPAC(pac.getLogonInfo());
        }
        catch ( DcerpcException e ) {
            return handlePACValidateFailure(pac, isRetry, e);
        }
        catch ( IOException e ) {
            throw new ADException("Communication failure", e); //$NON-NLS-1$
        }
        catch ( ADException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new ADException(e);
        }

    }


    /**
     * @param pac
     * @param isRetry
     * @param e
     * @return
     * @throws ADException
     */
    protected ADUserInfo handlePACValidateFailure ( Pac pac, boolean isRetry, DcerpcException e ) throws ADException {
        if ( !isRetry && e.getErrorCode() == STATUS_ACCESS_DENIED ) {
            log.debug("Got STATUS_ACCESS_DENIED, reauthenticating"); //$NON-NLS-1$
            try {
                this.conn.init();
            }
            catch ( IOException e1 ) {
                log.debug("Initialization failed", e1); //$NON-NLS-1$
                this.conn.fail();
            }
            return pacValidate(pac, true);
        }
        else if ( e.getErrorCode() == STATUS_ACCESS_DENIED ) {
            this.conn.fail();
        }

        if ( e.getErrorCode() == STATUS_LOGON_FAILURE ) {
            throw new ADException("Server rejected PAC", e); //$NON-NLS-1$
        }

        throw new ADException("pacValidate call failed", e); //$NON-NLS-1$
    }


    /**
     * @param pac
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws ADException
     */
    protected NetrLogonSamLogonWithFlags makeSAMRLogonPAC ( Pac pac ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ShortBufferException, ADException {
        KerbVerifyPACRequest pacVfyReq = new KerbVerifyPACRequest(pac);

        NetlogonIdentityInfo logonIdentity = new NetlogonIdentityInfo(
            this.conn.getRealm().getDomainName(),
            0,
            0,
            0,
            StringUtils.EMPTY,
            StringUtils.EMPTY);
        byte[] encoded = pacVfyReq.getEncoded();
        NetlogonGenericInformation genInfo = new NetlogonGenericInformation(logonIdentity, "Kerberos", this.conn.encryptSession(encoded)); //$NON-NLS-1$
        NetlogonValidationGenericInfo2 valInfo = new NetlogonValidationGenericInfo2();

        NetrLogonSamLogonWithFlags logon = new NetrLogonSamLogonWithFlags(
            this.conn.getDcerpcHandle().getServer(),
            this.conn.getRealm().getLocalNetbiosHostname(),
            this.conn.authenticate(),
            genInfo,
            valInfo,
            0);
        return logon;
    }


    @Override
    public ADUserInfo ntlmValidate ( byte[] serverChallenge, Type3Message auth ) throws ADException {
        return ntlmValidate(serverChallenge, auth, false);
    }


    private ADUserInfo ntlmValidate ( byte[] serverChallenge, Type3Message auth, boolean retry ) throws ADException {
        try {
            NetrLogonSamLogonWithFlags logon = makeSamrLogonNTLM(serverChallenge, auth);
            boolean failed = true;
            try {
                this.conn.getDcerpcHandle().sendrecv(logon);
                failed = false;
            }
            finally {
                // don't validate on access denied error, this indicates that our authenticator was wrong
                if ( !failed || logon.getResult().getErrorCode() != STATUS_ACCESS_DENIED ) {
                    this.conn.validate(logon.getReturnAuthenticator(), failed);
                }
            }
            if ( logon.getAuthorative() != 1 ) {
                throw new ADException("Result is not authorative"); //$NON-NLS-1$
            }
            NetlogonValidationSamInfo netlogonValidationSamInfo = (NetlogonValidationSamInfo) logon.getValidationInformation();
            if ( netlogonValidationSamInfo == null ) {
                throw new ADException("Validation did not return info"); //$NON-NLS-1$
            }
            return ADUserInfoImpl.fromValidationSAMInfo(netlogonValidationSamInfo);
        }
        catch ( DcerpcException e ) {
            if ( !retry && e.getErrorCode() == 0xc0000022 ) {
                return handleAccessDenied(serverChallenge, auth);
            }
            else if ( e.getErrorCode() == 0xC0000064 ) {
                throw new ADException("User not found", e); //$NON-NLS-1$
            }
            else if ( e.getErrorCode() == 0xC000006A ) {
                throw new ADException("Authentication failure: " + e.getMessage(), e); //$NON-NLS-1$
            }
            throw new ADException("ntlmValidate call failed: " + e.getMessage(), e); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            throw new ADException("Communication failure", e); //$NON-NLS-1$
        }
        catch ( ADException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new ADException(e);
        }

    }


    /**
     * @param serverChallenge
     * @param auth
     * @return
     * @throws ADException
     */
    protected ADUserInfo handleAccessDenied ( byte[] serverChallenge, Type3Message auth ) throws ADException {
        log.debug("Got STATUS_ACCESS_DENIED, reauthenticating"); //$NON-NLS-1$
        try {
            this.conn.init();
        }
        catch ( IOException e1 ) {
            log.debug("Initialization failed", e1); //$NON-NLS-1$
            this.conn.fail();
        }
        return ntlmValidate(serverChallenge, auth, true);
    }


    /**
     * @param serverChallenge
     * @param auth
     * @return
     * @throws ADException
     */
    protected NetrLogonSamLogonWithFlags makeSamrLogonNTLM ( byte[] serverChallenge, Type3Message auth ) throws ADException {
        NetlogonIdentityInfo netlogonIdentityInfo = new NetlogonIdentityInfo(
            auth.getDomain(),
            0x00000820,
            0,
            0,
            auth.getUser(),
            auth.getWorkstation());

        NetlogonNetworkInfo netlogonNetworkInfo = new NetlogonNetworkInfo(
            netlogonIdentityInfo,
            serverChallenge,
            auth.getNTResponse(),
            auth.getLMResponse());

        NetrLogonSamLogonWithFlags netrLogonSamLogon = new NetrLogonSamLogonWithFlags(
            this.conn.getDcerpcHandle().getServer(),
            this.conn.getRealm().getLocalNetbiosHostname(),
            this.conn.authenticate(),
            netlogonNetworkInfo,
            new NetlogonValidationSamInfo(),
            0);
        return netrLogonSamLogon;
    }
}
