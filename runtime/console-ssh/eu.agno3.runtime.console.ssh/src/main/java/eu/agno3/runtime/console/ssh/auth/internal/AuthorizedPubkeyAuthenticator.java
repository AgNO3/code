/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.auth.internal;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;


/**
 * @author mbechler
 *
 */
@Component (
    service = PublickeyAuthenticator.class,
    configurationPid = AuthorizedPubkeyAuthenticator.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class AuthorizedPubkeyAuthenticator implements PublickeyAuthenticator {

    /**
     * 
     */
    public static final String PID = "console.ssh.auth.pubkey"; //$NON-NLS-1$
    private static final String SSH_RSA = "ssh-rsa"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(AuthorizedPubkeyAuthenticator.class);

    private static final String LOCAL_USER = System.getProperty("user.name"); //$NON-NLS-1$
    private static final String LOCAL_USER_HOME = System.getProperty("user.home"); //$NON-NLS-1$
    private static final Charset PUBKEY_CHARSET = Charset.forName("US-ASCII"); //$NON-NLS-1$

    private MultiValuedMap<String, File> pubkeyFiles = new ArrayListValuedHashMap<>();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.pubkeyFiles.clear();
        String filesSpec = (String) ctx.getProperties().get("files"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(filesSpec) ) {
            for ( String entrySpec : StringUtils.split(filesSpec, ',') ) {
                configureFromEntry(entrySpec);
            }
        }
        else {
            File f = new File(LOCAL_USER_HOME, ".ssh/id_rsa.pub"); //$NON-NLS-1$
            if ( !f.exists() || !f.canRead() ) {
                log.debug("No public key available in ~/.ssh/id_rsa.pub"); //$NON-NLS-1$
            }
            else {
                this.pubkeyFiles.put(LOCAL_USER, f);
            }
        }

    }


    /**
     * @param entrySpec
     */
    protected void configureFromEntry ( String entrySpec ) {
        String[] parts = StringUtils.split(entrySpec, ':');

        if ( parts.length != 2 ) {
            log.warn("Invalid entry " + entrySpec); //$NON-NLS-1$
            return;
        }

        String userSpec = parts[ 0 ].trim();
        String fileSpec = parts[ 1 ].trim();

        File f = new File(fileSpec);
        if ( !f.exists() || !f.canRead() ) {
            log.warn("No public key available in " + fileSpec); //$NON-NLS-1$
            return;
        }
        this.pubkeyFiles.put(userSpec, f);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator#authenticate(java.lang.String,
     *      java.security.PublicKey, org.apache.sshd.server.session.ServerSession)
     */
    @Override
    public boolean authenticate ( String username, PublicKey pkey, ServerSession sess ) {

        Collection<File> files = this.pubkeyFiles.get(username);

        if ( files.isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("User %s does not have any keys configured", username)); //$NON-NLS-1$
            }
            return false;
        }

        for ( File f : files ) {
            try ( FileInputStream fis = new FileInputStream(f);
                  InputStreamReader fr = new InputStreamReader(fis, PUBKEY_CHARSET);
                  BufferedReader br = new BufferedReader(fr) ) {

                String line;
                while ( ( line = br.readLine() ) != null ) {
                    String[] parts = StringUtils.split(line, ' ');

                    if ( parts == null || parts.length < 2 ) {
                        log.warn("Failed to parse public key file"); //$NON-NLS-1$
                        continue;
                    }

                    String type = parts[ 0 ];
                    String dataEncoded = parts[ 1 ];
                    String comment = parts.length > 2 ? parts[ 2 ] : null;

                    if ( !SSH_RSA.equals(type) ) {
                        log.warn("Pubkey file does not contain a RSA public key"); //$NON-NLS-1$
                        continue;
                    }

                    PublicKey pk = parseSSHPublicKey(dataEncoded);
                    if ( pk.equals(pkey) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Matched key with comment " + comment); //$NON-NLS-1$
                        }
                        return true;
                    }
                }
            }
            catch (
                IllegalArgumentException |
                IOException |
                InvalidKeySpecException |
                NoSuchAlgorithmException e ) {
                log.warn("Could not read public key", e); //$NON-NLS-1$
            }
        }

        return false;
    }


    /**
     * @param dataEncoded
     * @return
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    protected PublicKey parseSSHPublicKey ( String dataEncoded ) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] data = Base64.decodeBase64(dataEncoded);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        String type = new String(readByteArray(dis), PUBKEY_CHARSET);

        if ( !type.equals(SSH_RSA) ) {
            throw new InvalidKeySpecException("Not a ssh RSA key"); //$NON-NLS-1$
        }

        BigInteger e = readBigInt(dis);
        BigInteger modulus = readBigInt(dis);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, e);
        return KeyFactory.getInstance("RSA").generatePublic(spec); //$NON-NLS-1$
    }


    /**
     * @param dis
     * @return
     * @throws IOException
     */
    private BigInteger readBigInt ( DataInputStream dis ) throws IOException {
        return new BigInteger(readByteArray(dis));
    }


    /**
     * @param dis
     * @return
     * @throws IOException
     */
    protected byte[] readByteArray ( DataInputStream dis ) throws IOException {
        int size = dis.readInt();
        if ( size < 0 ) {
            throw new IOException("Cannot handle sizes bigger than signed int32"); //$NON-NLS-1$
        }

        byte bytes[] = new byte[size];
        dis.readFully(bytes, 0, size);
        return bytes;
    }

}
