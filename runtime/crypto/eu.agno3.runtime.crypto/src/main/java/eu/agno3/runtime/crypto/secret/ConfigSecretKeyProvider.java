/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 8, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.secret;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.CryptoKeyNotFoundException;
import eu.agno3.runtime.crypto.scrypt.SCryptParams;
import eu.agno3.runtime.crypto.scrypt.SCryptUtil;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
public class ConfigSecretKeyProvider implements SecretKeyProvider {

    private static final Logger log = Logger.getLogger(ConfigSecretKeyProvider.class);
    private static final SCryptParams PARAMS = new SCryptParams(1 << 14 - 1, 8, 1);

    private final Map<Integer, byte[]> keys = new TreeMap<>();
    private final int latest;


    /**
     * 
     */
    private ConfigSecretKeyProvider ( Map<Integer, byte[]> keys, int latest ) {
        this.latest = latest;
        this.keys.putAll(keys);
    }


    /**
     * @param cfg
     * @param id
     * @return provider, if configured
     * @throws IOException
     */
    public static SecretKeyProvider create ( Dictionary<String, Object> cfg, String id ) throws IOException {

        int kvno = ConfigUtil.parseInt(cfg, id + ".knvo", -1); //$NON-NLS-1$
        if ( kvno > 0 ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Loading keys using KVNO " + id); //$NON-NLS-1$
            }
            Map<Integer, byte[]> keys = new TreeMap<>();

            for ( int i = 0; i < kvno; i++ ) {
                String raw = ConfigUtil.parseSecret(cfg, id + '.' + String.valueOf(i) + ".raw", null); //$NON-NLS-1$
                if ( raw != null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("RAW key " + i); //$NON-NLS-1$
                    }
                    keys.put(i, Base64.getDecoder().decode(raw));
                    continue;
                }

                String sec = ConfigUtil.parseSecret(cfg, id + '.' + String.valueOf(i), null);
                if ( sec != null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("DERIVE key " + i); //$NON-NLS-1$
                    }
                    keys.put(i, derive(id, sec));
                }
            }
            if ( !keys.isEmpty() ) {
                return new ConfigSecretKeyProvider(keys, kvno);
            }
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Loading single key " + id); //$NON-NLS-1$
            }
            String raw = ConfigUtil.parseSecret(cfg, id + ".raw", null); //$NON-NLS-1$
            if ( raw != null ) {
                log.debug("Found RAW key"); //$NON-NLS-1$
                return new ConfigSecretKeyProvider(Collections.singletonMap(0, Base64.getDecoder().decode(raw)), 0);
            }

            String sec = ConfigUtil.parseSecret(cfg, id, null);
            if ( sec != null ) {
                log.debug("Found DERIVE key"); //$NON-NLS-1$
                return new ConfigSecretKeyProvider(Collections.singletonMap(0, derive(id, sec)), 0);
            }
        }
        log.debug("No key configured"); //$NON-NLS-1$
        return null;
    }


    /**
     * @param sec
     * @return
     */
    private static byte[] derive ( String id, String sec ) {
        return SCryptUtil.generate(sec.getBytes(StandardCharsets.UTF_8), id.getBytes(StandardCharsets.UTF_8), PARAMS).getKey();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getExportableSecret(java.lang.String, int)
     */
    @Override
    public SecretKeyWithVersion getExportableSecret ( String keyId, int kvno ) throws CryptoException {
        int kv = kvno < 0 ? this.latest : kvno;
        byte[] key = this.keys.get(kv);
        if ( key == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Key not found " + kv); //$NON-NLS-1$
            }
            throw new CryptoKeyNotFoundException();
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Found key version " + kv); //$NON-NLS-1$
        }
        return new ExportableSecretWithVersion(key, kv);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getSecret(java.lang.String, int, java.lang.String)
     */
    @Override
    public SecretKeyWithVersion getSecret ( String keyId, int kvno, String algo ) throws CryptoException {
        return getExportableSecret(keyId, kvno);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getOrCreateExportableSecret(java.lang.String, int, int)
     */
    @Override
    public SecretKeyWithVersion getOrCreateExportableSecret ( String keyId, int kvno, int keySize ) throws CryptoException {
        return getExportableSecret(keyId, kvno);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.secret.SecretKeyProvider#getOrCreateSecret(java.lang.String, int, java.lang.String,
     *      int)
     */
    @Override
    public SecretKeyWithVersion getOrCreateSecret ( String keyId, int kvno, String algo, int keySize ) throws CryptoException {
        return getExportableSecret(keyId, kvno);
    }

}
