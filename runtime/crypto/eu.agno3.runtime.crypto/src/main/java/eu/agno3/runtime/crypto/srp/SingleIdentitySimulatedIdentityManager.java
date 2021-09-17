/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.srp;


import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.agreement.srp.SRP6VerifierGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import org.bouncycastle.crypto.tls.HashAlgorithm;
import org.bouncycastle.crypto.tls.SimulatedTlsSRPIdentityManager;
import org.bouncycastle.crypto.tls.TlsSRPIdentityManager;
import org.bouncycastle.crypto.tls.TlsSRPLoginParameters;
import org.bouncycastle.crypto.tls.TlsUtils;


/**
 * @author mbechler
 *
 */
public class SingleIdentitySimulatedIdentityManager extends SimulatedTlsSRPIdentityManager implements TlsSRPIdentityManager {

    private byte[] identity;
    private byte[] secret;
    private byte[] salt;
    private SRP6GroupParameters groupParams;


    /**
     * @param groupParams
     * @param secret
     * @param identity
     * @param seedKey
     */
    public SingleIdentitySimulatedIdentityManager ( SRP6GroupParameters groupParams, byte[] identity, byte[] secret, byte[] seedKey ) {
        super(groupParams, makeGenerator(groupParams), makeMAC(seedKey));
        this.groupParams = groupParams;
        this.identity = identity;
        this.secret = secret;
        this.salt = seedKey;
    }


    /**
     * @param seedKey
     * @return
     */
    private static Mac makeMAC ( byte[] seedKey ) {
        HMac mac = new HMac(TlsUtils.createHash(HashAlgorithm.sha1));
        mac.init(new KeyParameter(seedKey));
        return mac;
    }


    /**
     * @param groupParams
     * @return
     */
    private static SRP6VerifierGenerator makeGenerator ( SRP6GroupParameters groupParams ) {
        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator();
        verifierGenerator.init(groupParams, TlsUtils.createHash(HashAlgorithm.sha1));
        return verifierGenerator;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.bouncycastle.crypto.tls.SimulatedTlsSRPIdentityManager#getLoginParameters(byte[])
     */
    @Override
    public TlsSRPLoginParameters getLoginParameters ( byte[] id ) {
        if ( MessageDigest.isEqual(id, this.identity) ) {
            BigInteger verifier = makeGenerator(this.groupParams).generateVerifier(this.salt, this.identity, this.secret);
            return new TlsSRPLoginParameters(this.group, verifier, this.salt);
        }
        return super.getLoginParameters(id);
    }

}
