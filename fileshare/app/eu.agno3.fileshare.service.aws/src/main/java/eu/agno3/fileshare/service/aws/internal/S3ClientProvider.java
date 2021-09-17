/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.service.aws.internal;


import java.io.IOException;
import java.security.Security;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.CryptoMode;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;

import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.secret.ConfigSecretKeyProvider;
import eu.agno3.runtime.crypto.secret.SecretKeyProvider;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = S3ClientProvider.class, configurationPid = "store.aws", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class S3ClientProvider {

    /**
     * 
     */
    private static final String AES = "AES"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(S3ClientProvider.class);

    private AmazonS3 client;
    private String bucket;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        log.debug("Activating S3 client"); //$NON-NLS-1$

        String b = ConfigUtil.parseString(ctx.getProperties(), "bucket", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(b) ) {
            log.error("No bucket specified"); //$NON-NLS-1$
            return;
        }
        this.bucket = b;

        try {
            AWSCredentialsProvider staticCreds = null;
            String accessKey = ConfigUtil.parseSecret(ctx.getProperties(), "accessKey", null); //$NON-NLS-1$
            String secretKey = ConfigUtil.parseSecret(ctx.getProperties(), "secretKey", null); //$NON-NLS-1$
            if ( !StringUtils.isBlank(accessKey) && !StringUtils.isBlank(secretKey) ) {
                staticCreds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
            }

            AWSCredentialsProvider defaultCreds = DefaultAWSCredentialsProviderChain.getInstance();
            AWSCredentialsProvider creds = staticCreds != null ? new AWSCredentialsProviderChain(defaultCreds, staticCreds) : defaultCreds;

            String ep = ConfigUtil.parseString(ctx.getProperties(), "endpointUrl", null); //$NON-NLS-1$
            String region = ConfigUtil.parseString(ctx.getProperties(), "region", null); //$NON-NLS-1$

            // if an URL is specified this usually is not "real" S3 and path style access ( = host/bucket/ )
            // is more common and easily achievable for
            boolean pathStyleAccess = ConfigUtil.parseBoolean(ctx.getProperties(), "pathStyleAccess", !StringUtils.isBlank(ep)); //$NON-NLS-1$

            if ( !ConfigUtil.parseBoolean(ctx.getProperties(), "encryption", false) ) { //$NON-NLS-1$
                log.debug("Not enabling encryption"); //$NON-NLS-1$
                AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withCredentials(creds);

                if ( !StringUtils.isBlank(ep) ) {
                    builder.withEndpointConfiguration(new EndpointConfiguration(ep, region));
                }
                else if ( !StringUtils.isBlank(region) ) {
                    builder.withRegion(region);
                }

                builder.withPathStyleAccessEnabled(pathStyleAccess);
                this.client = builder.build();
            }
            else {
                if ( Security.getProvider("BC") == null ) { //$NON-NLS-1$
                    Security.addProvider(new BouncyCastleProvider());
                }

                AmazonS3EncryptionClientBuilder builder = AmazonS3EncryptionClientBuilder.standard().withCredentials(creds);
                setupEncryption(ctx, builder);

                if ( !StringUtils.isBlank(ep) ) {
                    builder.withEndpointConfiguration(new EndpointConfiguration(ep, region));
                }
                else if ( !StringUtils.isBlank(region) ) {
                    builder.withRegion(region);
                }
                builder.withPathStyleAccessEnabled(pathStyleAccess);
                this.client = builder.build();
            }

            try {
                if ( !this.client.doesBucketExistV2(b) ) {
                    log.error("Bucket does not exist " + b); //$NON-NLS-1$
                }
            }
            catch ( SdkBaseException e ) {
                // emulator (minio) does not like it
                // also could be currently unavailable
                log.trace("Failed to check whether bucket exists", e); //$NON-NLS-1$
            }
        }
        catch (
            SdkBaseException |
            DecoderException |
            IOException |
            CryptoException e ) {
            log.error("Failed to initialize S3 client", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ctx
     * @param builder
     * @throws DecoderException
     * @throws IOException
     * @throws CryptoException
     */
    private static void setupEncryption ( ComponentContext ctx, AmazonS3EncryptionClientBuilder builder )
            throws DecoderException, IOException, CryptoException {
        /**
         * Java SDK CryptoMode Encrypt Decrypt Range Get Multipart Upload Max Size (bytes)
         * 1.7.8.1+ AuthenticatedEncryption AES‑GCM AES‑GCM AES-CBC Yes Yes ~64GB
         * 1.7.8.1+ StrictAuthenticatedEncryption AES‑GCM AES‑GCM No Yes ~64GB
         * 1.7.8.1+ EncryptionOnly AES‑CBC AES‑GCM AES‑CBC Yes Yes 5TB
         * pre-1.7.8 (Not Applicable) AES‑CBC AES‑CBC Yes Yes 5TB
         */
        CryptoMode encMode = CryptoMode.EncryptionOnly;
        String encModeSpec = ConfigUtil.parseString(ctx.getProperties(), "encryptionMode", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(encModeSpec) ) {
            encMode = CryptoMode.valueOf(encModeSpec);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Enabling encryption " + encMode); //$NON-NLS-1$
        }

        builder.setCryptoConfiguration(new CryptoConfiguration(encMode));

        SecretKeyProvider skp = ConfigSecretKeyProvider.create(ctx.getProperties(), "encryptionKey"); //$NON-NLS-1$
        String kmsMasterKeyId = ConfigUtil.parseString(ctx.getProperties(), "encryptionKMSMasterKeyId", null); //$NON-NLS-1$

        if ( !StringUtils.isBlank(kmsMasterKeyId) ) {
            builder.setEncryptionMaterials(new KMSEncryptionMaterialsProvider(kmsMasterKeyId));
        }
        else if ( skp != null ) {
            EncryptionMaterials materials = new EncryptionMaterials(skp.getSecret("default", 0, AES)); //$NON-NLS-1$
            builder.setEncryptionMaterials(new StaticEncryptionMaterialsProvider(materials));
        }

        String hexKeySpec = ConfigUtil.parseSecret(ctx.getProperties(), "encryptionHexKey", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(hexKeySpec) ) {
            EncryptionMaterials materials = new EncryptionMaterials(new SecretKeySpec(Hex.decodeHex(hexKeySpec.trim().toCharArray()), AES));
            builder.setEncryptionMaterials(new StaticEncryptionMaterialsProvider(materials));
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        AmazonS3 cl = this.client;
        this.client = null;
        if ( cl != null ) {
            cl.shutdown();
        }
    }


    /**
     * @return the bucket
     */
    public String getBucket () {
        return this.bucket;
    }


    /**
     * @return client
     * @throws StorageException
     */
    public AmazonS3 getClient () throws StorageException {
        AmazonS3 c = this.client;
        if ( c == null ) {
            throw new StorageException("Client not available"); //$NON-NLS-1$
        }
        return c;
    }
}
