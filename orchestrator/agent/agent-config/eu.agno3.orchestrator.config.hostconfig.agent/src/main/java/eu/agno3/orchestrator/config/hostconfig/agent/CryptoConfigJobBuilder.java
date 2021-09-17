/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.crypto.keystore.units.EnsureKeystore;
import eu.agno3.orchestrator.agent.crypto.keystore.units.ImportKey;
import eu.agno3.orchestrator.agent.crypto.keystore.units.RemoveKeystore;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.agent.crypto.truststore.units.EnsureTruststore;
import eu.agno3.orchestrator.agent.crypto.truststore.units.RemoveTruststore;
import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntry;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfig;
import eu.agno3.orchestrator.config.crypto.truststore.RevocationConfig;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfig;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfig;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.file.contents.PropertiesProvider;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 *
 */
@Component ( service = CryptoConfigJobBuilder.class )
public class CryptoConfigJobBuilder {

    private static final Logger log = Logger.getLogger(CryptoConfigJobBuilder.class);

    private static final int CHANGE_REMOVE = 1;
    private static final int CHANGE_ADD = 2;
    private static final int CHANGE_REVCONFIG = 4;

    private static final int CHANGE_VALIDATION = 16;
    private static final int CHANGE_IMPORTKEY = 32;

    private static final String REVOCATION_CONFIG_FILE = "revocation.properties"; //$NON-NLS-1$

    private TruststoresManager truststoresManager;
    private KeystoresManager keystoresManager;


    @Reference
    protected synchronized void setTruststoresManager ( TruststoresManager tsm ) {
        this.truststoresManager = tsm;
    }


    protected synchronized void unsetTruststoresManager ( TruststoresManager tsm ) {
        if ( this.truststoresManager == tsm ) {
            this.truststoresManager = null;
        }
    }


    @Reference
    protected synchronized void setKeystoresManager ( KeystoresManager ksm ) {
        this.keystoresManager = ksm;
    }


    protected synchronized void unsetKeystoresManager ( KeystoresManager ksm ) {
        if ( this.keystoresManager == ksm ) {
            this.keystoresManager = null;
        }
    }


    /**
     * @param b
     * @param ctx
     * @throws TruststoreManagerException
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws IOException
     * @throws CertificateEncodingException
     * @throws KeystoreManagerException
     * @throws InvalidParameterException
     */
    public void build ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws TruststoreManagerException, JobBuilderException, UnitInitializationFailedException, IOException, CertificateEncodingException,
            KeystoreManagerException, InvalidParameterException {

        configureKeyStores(b, ctx);
        configureTrustStores(b, ctx);
    }


    /**
     * @param b
     * @param ctx
     * @throws KeystoreManagerException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    protected void configureKeyStores ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws KeystoreManagerException, UnitInitializationFailedException, InvalidParameterException {
        @Nullable
        KeystoresConfig keystoreConfiguration = ctx.cfg().getKeystoreConfiguration();

        if ( keystoreConfiguration == null ) {
            throw new UnitInitializationFailedException();
        }

        Map<String, KeystoreConfig> cfgKeystores = makeKeystoreMap(keystoreConfiguration.getKeystores());
        Map<String, KeystoreConfig> oldKeystores;
        if ( ctx.cur().isPresent() ) {
            oldKeystores = makeKeystoreMap(ctx.cur().get().getKeystoreConfiguration().getKeystores());
        }
        else {
            oldKeystores = Collections.EMPTY_MAP;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Configuring key stores " + cfgKeystores.keySet()); //$NON-NLS-1$
            log.debug("Previous key stores " + oldKeystores.keySet()); //$NON-NLS-1$
        }

        Map<String, Integer> changes = getKeystoreChanges(ctx, oldKeystores, cfgKeystores, this.keystoresManager);

        for ( Entry<String, Integer> e : changes.entrySet() ) {
            String ksName = e.getKey();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found keystore %s change %d", ksName, e.getValue())); //$NON-NLS-1$
            }

            if ( ( e.getValue() & CHANGE_REMOVE ) != 0 ) {
                b.add(RemoveKeystore.class).keystore(ksName);
                continue;
            }

            KeystoreConfig keystoreConfig = cfgKeystores.get(ksName);

            // ensure keystore exists
            b.add(EnsureKeystore.class).keystore(ksName).validationTruststore(keystoreConfig.getValidationTrustStore());

            if ( ( e.getValue() & CHANGE_IMPORTKEY ) != 0 ) {
                for ( ImportKeyPairEntry entry : keystoreConfig.getImportKeyPairs() ) {
                    b.add(ImportKey.class).keystore(ksName).alias(entry.getAlias()).keyPair(entry.getKeyPair())
                            .certChain(entry.getCertificateChain());
                }
            }
        }
    }


    /**
     * @param keystores
     * @return
     */
    private static Map<String, KeystoreConfig> makeKeystoreMap ( Set<KeystoreConfig> keystores ) {
        Map<String, KeystoreConfig> res = new HashMap<>();
        for ( KeystoreConfig cfg : keystores ) {
            res.put(cfg.getAlias(), cfg);
        }
        return res;
    }


    /**
     * @param b
     * @param ctx
     * @throws TruststoreManagerException
     * @throws JobBuilderException
     * @throws UnitInitializationFailedException
     * @throws IOException
     * @throws CertificateEncodingException
     */
    private void configureTrustStores ( JobBuilder b, ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
            throws TruststoreManagerException, JobBuilderException, UnitInitializationFailedException, IOException, CertificateEncodingException {

        @Nullable
        TruststoresConfig trustConfiguration = ctx.cfg().getTrustConfiguration();

        if ( trustConfiguration == null ) {
            throw new UnitInitializationFailedException();
        }

        Map<String, TruststoreConfig> cfgTruststores = trustConfiguration.getTruststores() != null
                ? makeTruststoreMap(trustConfiguration.getTruststores()) : Collections.EMPTY_MAP;
        Map<String, TruststoreConfig> oldTruststores;
        if ( !ctx.cur().isPresent() ) {
            oldTruststores = Collections.EMPTY_MAP;
        }
        else {
            oldTruststores = makeTruststoreMap(trustConfiguration.getTruststores());
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Configuring trust stores " + cfgTruststores.keySet()); //$NON-NLS-1$
            log.debug("Previous trust stores " + oldTruststores.keySet()); //$NON-NLS-1$
        }

        Map<String, Integer> changes = getTruststoreChanges(ctx, oldTruststores, cfgTruststores, this.truststoresManager);
        for ( Entry<String, Integer> e : changes.entrySet() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found truststore %s change %d", e.getKey(), e.getValue())); //$NON-NLS-1$
            }

            if ( ( e.getValue() & CHANGE_REMOVE ) != 0 ) {
                // remove truststore
                b.add(RemoveTruststore.class).truststore(e.getKey());
                continue;
            }

            // ensure truststore exists
            b.add(EnsureTruststore.class).truststore(e.getKey());

            TruststoreConfig truststoreConfig = cfgTruststores.get(e.getKey());
            b.add(Contents.class)
                    .content(new PropertiesProvider(RevocationConfigUtil.makeRevocationProperties(truststoreConfig.getRevocationConfiguration())))
                    .noPrefix().file(new File(this.truststoresManager.getTrustStorePath(e.getKey()), REVOCATION_CONFIG_FILE))
                    .perms(FileSecurityUtils.getWorldReadableFilePermissions())
                    .runIf( ( e.getValue() & ( CHANGE_REVCONFIG | CHANGE_ADD ) ) != 0 || ctx.job().getApplyInfo().isForce());
        }

    }


    /**
     * @param ctx
     * @param changes
     * @param cfgTruststores
     * @param tsManager
     * @return
     * @throws TruststoreManagerException
     * @throws JobBuilderException
     */
    private static Map<String, Integer> getTruststoreChanges ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            Map<String, TruststoreConfig> oldTruststores, Map<String, TruststoreConfig> cfgTruststores, TruststoresManager tsManager )
                    throws TruststoreManagerException, JobBuilderException {
        Map<String, Integer> changes = new HashMap<>();
        List<String> knownTruststores = tsManager.getTrustStores();
        if ( log.isDebugEnabled() ) {
            log.debug("Known truststores on system " + knownTruststores); //$NON-NLS-1$
        }
        addAddedTruststores(cfgTruststores, changes, oldTruststores, knownTruststores);
        addRemovedTruststores(cfgTruststores, changes, oldTruststores, knownTruststores);
        addModifiedTruststores(ctx, oldTruststores, cfgTruststores, tsManager, changes, knownTruststores);
        return changes;
    }


    /**
     * @param ctx
     * @param oldKeystores
     * @param cfgKeystores
     * @param keystoresManager2
     * @return
     * @throws KeystoreManagerException
     */
    private static Map<String, Integer> getKeystoreChanges ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            Map<String, KeystoreConfig> oldKeystores, Map<String, KeystoreConfig> cfgKeystores, KeystoresManager ksManager )
                    throws KeystoreManagerException {
        Map<String, Integer> changes = new HashMap<>();
        List<String> knownKeystores = ksManager.getKeyStores();
        if ( log.isDebugEnabled() ) {
            log.debug("Known keystores on system " + knownKeystores); //$NON-NLS-1$
        }
        addAddedKeystores(cfgKeystores, changes, oldKeystores, knownKeystores);
        addRemovedKeystores(cfgKeystores, changes, oldKeystores, knownKeystores);
        addModifiedKeystores(ctx, oldKeystores, cfgKeystores, ksManager, changes, knownKeystores);
        return changes;
    }


    /**
     * @param ctx
     * @param cfgTruststores
     * @param oldTruststores
     * @param tsManager
     * @param changes
     * @param knownTruststores
     * @throws TruststoreManagerException
     * @throws JobBuilderException
     */
    private static void addModifiedTruststores ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            Map<String, TruststoreConfig> cfgTruststores, Map<String, TruststoreConfig> oldTruststores, TruststoresManager tsManager,
            Map<String, Integer> changes, List<String> knownTruststores ) throws TruststoreManagerException, JobBuilderException {
        Set<String> commonTruststores = new HashSet<>(cfgTruststores.keySet());
        commonTruststores.retainAll(oldTruststores.keySet());
        for ( String alias : commonTruststores ) {
            int tmChange = getTrustStoreChange(
                ctx,
                alias,
                oldTruststores.get(alias),
                cfgTruststores.get(alias),
                tsManager.getTrustStoreManager(alias));

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Changes for %s: %d", alias, tmChange)); //$NON-NLS-1$
            }
            if ( tmChange != 0 || ctx.job().getApplyInfo().isForce() ) {
                changes.put(alias, tmChange);
            }
        }
    }


    /**
     * @param ctx
     * @param oldKeystores
     * @param cfgKeystores
     * @param ksManager
     * @param changes
     * @param knownKeystores
     * @throws KeystoreManagerException
     */
    private static void addModifiedKeystores ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx,
            Map<String, KeystoreConfig> oldKeystores, Map<String, KeystoreConfig> cfgKeystores, KeystoresManager ksManager,
            Map<String, Integer> changes, List<String> knownKeystores ) throws KeystoreManagerException {
        Set<String> commonKeystores = new HashSet<>(cfgKeystores.keySet());
        commonKeystores.retainAll(knownKeystores);
        for ( String alias : commonKeystores ) {
            int tmChange = getKeystoreChange(ctx, alias, oldKeystores.get(alias), cfgKeystores.get(alias), ksManager.getKeyStoreManager(alias));

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Changes for %s: %d", alias, tmChange)); //$NON-NLS-1$
            }
            if ( tmChange != 0 || ctx.job().getApplyInfo().isForce() ) {
                changes.put(alias, tmChange);
            }
        }
    }


    /**
     * @param ctx
     * @param alias
     * @param keystoreConfig
     * @param keystoreConfig2
     * @param keyStoreManager
     * @return
     */
    private static int getKeystoreChange ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, String alias,
            KeystoreConfig oldKsConfig, KeystoreConfig newKsConfig, KeystoreManager keyStoreManager ) {
        if ( oldKsConfig == null ) {
            return CHANGE_ADD;
        }

        int res = 0;
        if ( newKsConfig.getValidationTrustStore() != null && !newKsConfig.getValidationTrustStore().equals(oldKsConfig.getValidationTrustStore()) ) {
            res |= CHANGE_VALIDATION;
        }
        else if ( newKsConfig.getValidationTrustStore() == null && oldKsConfig.getValidationTrustStore() != null ) {
            res |= CHANGE_VALIDATION;
        }

        Map<String, ImportKeyPairEntry> oldImports = makeKeyPairMap(oldKsConfig.getImportKeyPairs());
        Map<String, ImportKeyPairEntry> newImports = makeKeyPairMap(newKsConfig.getImportKeyPairs());

        Set<String> additions = new HashSet<>(newImports.keySet());
        additions.removeAll(oldImports.keySet());
        Set<String> removals = new HashSet<>(oldImports.keySet());
        removals.removeAll(newImports.keySet());
        Set<String> common = new HashSet<>(newImports.keySet());
        common.retainAll(oldImports.keySet());

        if ( !additions.isEmpty() || !removals.isEmpty() ) {
            res |= CHANGE_IMPORTKEY;
        }

        for ( String keyAlias : common ) {
            ImportKeyPairEntry olde = oldImports.get(keyAlias);
            ImportKeyPairEntry newe = newImports.get(keyAlias);

            if ( !Objects.equals(olde.getCertificateChain(), newe.getCertificateChain()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Certificate chain changed " + keyAlias); //$NON-NLS-1$
                }
                res |= CHANGE_IMPORTKEY;
            }

            if ( !Objects.equals(olde.getKeyPair(), newe.getKeyPair()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Key changed " + keyAlias); //$NON-NLS-1$
                }
                res |= CHANGE_IMPORTKEY;
            }
        }

        return res;
    }


    /**
     * @param importKeyPairs
     * @return
     */
    private static Map<String, ImportKeyPairEntry> makeKeyPairMap ( Set<ImportKeyPairEntry> importKeyPairs ) {
        Map<String, ImportKeyPairEntry> entries = new HashMap<>();
        for ( ImportKeyPairEntry kpe : importKeyPairs ) {
            entries.put(kpe.getAlias(), kpe);
        }
        return entries;
    }


    /**
     * @param cfgTruststores
     * @param changes
     * @param oldTruststores
     * @param knownTruststores
     */
    private static void addRemovedTruststores ( Map<String, TruststoreConfig> cfgTruststores, Map<String, Integer> changes,
            Map<String, TruststoreConfig> oldTruststores, List<String> knownTruststores ) {
        Set<String> removedTruststores = new HashSet<>(oldTruststores.keySet());
        removedTruststores.removeAll(cfgTruststores.keySet());
        removedTruststores.remove("client"); //$NON-NLS-1$
        removedTruststores.remove("internal"); //$NON-NLS-1$
        for ( String alias : removedTruststores ) {
            changes.put(alias, CHANGE_REMOVE);
        }
    }


    /**
     * @param cfgKeystores
     * @param changes
     * @param oldKeystores
     * @param knownKeystores
     */
    private static void addRemovedKeystores ( Map<String, KeystoreConfig> cfgKeystores, Map<String, Integer> changes,
            Map<String, KeystoreConfig> oldKeystores, List<String> knownKeystores ) {
        Set<String> removedKeystores = new HashSet<>(oldKeystores.keySet());
        removedKeystores.removeAll(cfgKeystores.keySet());
        removedKeystores.remove("internalCA"); //$NON-NLS-1$
        removedKeystores.remove("orchagent"); //$NON-NLS-1$
        removedKeystores.remove("orchserver"); //$NON-NLS-1$
        removedKeystores.remove("web"); //$NON-NLS-1$
        for ( String alias : removedKeystores ) {
            changes.put(alias, CHANGE_REMOVE);
        }
    }


    /**
     * @param cfgTruststores
     * @param changes
     * @param oldTruststores
     * @param knownTruststores
     */
    private static void addAddedTruststores ( Map<String, TruststoreConfig> cfgTruststores, Map<String, Integer> changes,
            Map<String, TruststoreConfig> oldTruststores, List<String> knownTruststores ) {

        Set<String> missingTruststores = new HashSet<>(oldTruststores.keySet());
        missingTruststores.removeAll(knownTruststores);

        for ( String alias : missingTruststores ) {
            log.warn("Truststore is present in configuration but not available on system " + alias); //$NON-NLS-1$
        }

        Set<String> addedTruststores = new HashSet<>(cfgTruststores.keySet());
        addedTruststores.removeAll(knownTruststores);
        for ( String alias : addedTruststores ) {
            changes.put(alias, CHANGE_ADD);
        }
    }


    /**
     * @param cfgKeystores
     * @param changes
     * @param oldKeystores
     * @param knownKeystores
     */
    private static void addAddedKeystores ( Map<String, KeystoreConfig> cfgKeystores, Map<String, Integer> changes,
            Map<String, KeystoreConfig> oldKeystores, List<String> knownKeystores ) {
        Set<String> missingKeystores = new HashSet<>(oldKeystores.keySet());
        missingKeystores.removeAll(knownKeystores);

        for ( String alias : missingKeystores ) {
            log.warn("Keystore is present in configuration but not available on system " + alias); //$NON-NLS-1$
        }

        Set<String> addedKeystores = new HashSet<>(cfgKeystores.keySet());
        addedKeystores.removeAll(oldKeystores.keySet());
        for ( String alias : addedKeystores ) {
            changes.put(alias, CHANGE_ADD);
        }
    }


    /**
     * @param ctx
     * @param alias
     * @param truststoreConfig
     * @param trustStoreManager
     * @return
     * @throws TruststoreManagerException
     * @throws JobBuilderException
     */
    private static int getTrustStoreChange ( ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx, String alias,
            TruststoreConfig oldTsConf, TruststoreConfig newTsConf, TruststoreManager trustStoreManager )
                    throws TruststoreManagerException, JobBuilderException {

        int changed = 0;
        if ( oldTsConf == null || trustStoreManager.getRevocationConfig() == null
                || checkRevocationConfigChanged(oldTsConf.getRevocationConfiguration(), newTsConf.getRevocationConfiguration()) ) {
            changed |= CHANGE_REVCONFIG;
        }
        return changed;
    }


    /**
     * @param revocationConfiguration
     * @param revocationConfig
     * @return
     * @throws JobBuilderException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static boolean checkRevocationConfigChanged ( RevocationConfig oldRevConf, RevocationConfig newRevConf ) throws JobBuilderException {
        try {
            return !CompareUtil.compareProperties(oldRevConf, newRevConf);
        }
        catch (
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException |
            IntrospectionException e ) {
            throw new JobBuilderException("Failed to compare revocation config", e); //$NON-NLS-1$
        }
    }


    /**
     * @param truststores
     * @return
     */
    private static Map<String, TruststoreConfig> makeTruststoreMap ( Set<TruststoreConfig> truststores ) {
        Map<String, TruststoreConfig> res = new HashMap<>();
        for ( TruststoreConfig ts : truststores ) {
            res.put(ts.getAlias(), ts);
        }
        return res;
    }
}
