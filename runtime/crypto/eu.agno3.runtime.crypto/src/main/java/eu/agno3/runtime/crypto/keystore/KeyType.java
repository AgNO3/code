/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.11.2016 by mbechler
 */
package eu.agno3.runtime.crypto.keystore;


import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Enumeration;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public enum KeyType {

    /**
     * 1024 bit RSA
     */
    RSA1024("RSA", new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4)),

    /**
     * 2048 bit RSA
     */
    RSA2048("RSA", new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4)),

    /**
     * 4096 bit RSA
     */
    RSA4096("RSA", new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4)),

    /**
     * NIST secp256r1 curve
     */
    EC_P256R1("EC", new ECGenParameterSpec("secp256r1")),

    /**
     * NIST secp384r1 curve
     */
    EC_P384R1("EC", new ECGenParameterSpec("secp384r1")),

    /**
     * NIST secp521r1 curve
     */
    // P521 is somehow broken
    // EC_P521R1("EC", new ECGenParameterSpec("secp521r1")),

    ;

    private String algo;
    private AlgorithmParameterSpec params;


    /**
     * 
     */
    private KeyType ( String algo, AlgorithmParameterSpec params ) {
        this.algo = algo;
        this.params = params;
    }


    /**
     * @return the algo
     */
    public String getAlgo () {
        return this.algo;
    }


    /**
     * @return the params
     */
    public AlgorithmParameterSpec getParams () {
        return this.params;
    }


    /**
     * @param publicKey
     * @return key type for a public key
     */
    public static KeyType getKeyType ( PublicKey publicKey ) {

        if ( publicKey instanceof RSAPublicKey ) {
            int size = ( (RSAPublicKey) publicKey ).getModulus().bitLength();
            switch ( size ) {
            case 1024:
                return RSA1024;
            case 2048:
                return RSA2048;
            case 4096:
                return RSA4096;
            }
        }
        else if ( publicKey instanceof ECPublicKey ) {
            ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
            JCEECPublicKey ecpub = new JCEECPublicKey(ecPublicKey);

            ECParameterSpec bcparams = ecpub.getParameters();
            X9ECParameters tgtparams = new X9ECParameters(bcparams.getCurve(), bcparams.getG(), bcparams.getN(), bcparams.getH(), bcparams.getSeed());

            Enumeration<?> names = ECNamedCurveTable.getNames();

            while ( names.hasMoreElements() ) {
                String name = (String) names.nextElement();
                X9ECParameters params = ECNamedCurveTable.getByName(name);
                if ( tgtparams.getCurve().equals(params.getCurve()) && tgtparams.getG().equals(params.getG()) ) {
                    switch ( name ) {
                    case "secp256r1":
                    case "prime256v1":
                        return EC_P256R1;
                    case "secp384r1":
                    case "prime384v1":
                        return EC_P384R1;
                    // P-521 seems broken
                    // case "secp521r1":
                    // case "prime521v1":
                    // return EC_P521R1;
                    default:
                        throw new IllegalArgumentException("Unsupported curve " + name);
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unsupported key type " + publicKey);
    }
}
