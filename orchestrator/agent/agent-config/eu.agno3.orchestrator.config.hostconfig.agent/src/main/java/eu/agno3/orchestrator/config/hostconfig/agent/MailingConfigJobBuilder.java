/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.jobs.HostConfigurationJob;
import eu.agno3.orchestrator.config.web.SMTPConfiguration;
import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.config.web.agent.SSLConfigUtil;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.base.units.service.DisableService;
import eu.agno3.orchestrator.system.base.units.service.EnableService;
import eu.agno3.orchestrator.system.base.units.service.ReloadService;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.crypto.HashUtil;


/**
 * @author mbechler
 *
 */
public final class MailingConfigJobBuilder {

    private static final String POSTFIX = "postfix"; //$NON-NLS-1$
    private static final String SASLAUTHD = "saslauthd"; //$NON-NLS-1$
    private static final String POSTFIX_MAIN_CF = "/etc/postfix/main.cf"; //$NON-NLS-1$
    private static final String POSTFIX_MASTER_CF = "/etc/postfix/master.cf.agno3"; //$NON-NLS-1$
    private static final String POSTFIX_SASL_CONF = "/etc/postfix/sasl/smtpd.conf"; //$NON-NLS-1$
    private static final String POSTFIX_SASL_PASSWORDS = "/etc/postfix/sasl_passwords"; //$NON-NLS-1$
    private static final String SASLAUTHD_DEFAULTS = "/etc/default/saslauthd.agno3"; //$NON-NLS-1$
    private static final String POSTMAP = "/usr/sbin/postmap"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(MailingConfigJobBuilder.class);


    /**
     * @param b
     * @param ctx
     * @throws IOException
     * @throws UnitInitializationFailedException
     */
    public static void configureMailing ( @NonNull JobBuilder b,
            @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx )
                    throws UnitInitializationFailedException, IOException {

        if ( ctx.cfg().getMailingConfiguration().getMailingEnabled() ) {

            b.add(Contents.class).file(POSTFIX_MAIN_CF).content(ctx.tpl(POSTFIX_MAIN_CF, makeExtraContext(ctx)))
                    .perms(FileSecurityUtils.getWorldReadableFilePermissions());
            b.add(Contents.class).file(POSTFIX_MASTER_CF).content(ctx.tpl(POSTFIX_MASTER_CF))
                    .perms(FileSecurityUtils.getWorldReadableFilePermissions());
            b.add(Contents.class).file(POSTFIX_SASL_CONF).content(ctx.tpl(POSTFIX_SASL_CONF))
                    .perms(FileSecurityUtils.getWorldReadableFilePermissions());
            b.add(Contents.class).file(SASLAUTHD_DEFAULTS).content(ctx.tpl(SASLAUTHD_DEFAULTS))
                    .perms(FileSecurityUtils.getOwnerOnlyFilePermissions());
            b.add(Contents.class).file(POSTFIX_SASL_PASSWORDS).content(ctx.tpl(POSTFIX_SASL_PASSWORDS))
                    .perms(FileSecurityUtils.getOwnerOnlyFilePermissions());

            b.add(Exec.class).cmd(POSTMAP).args(POSTFIX_SASL_PASSWORDS);

            b.add(EnableService.class).service(SASLAUTHD);
            b.add(EnableService.class).service(POSTFIX);
            b.add(ReloadService.class).service(POSTFIX);
        }
        else {
            b.add(DisableService.class).service(SASLAUTHD);
            b.add(DisableService.class).service(POSTFIX);
        }
    }


    /**
     * @param ctx
     * @return
     */
    private static Map<String, Serializable> makeExtraContext (
            @NonNull ConfigurationJobContext<@NonNull HostConfiguration, HostConfigurationJob> ctx ) {
        Map<String, Serializable> extra = new HashMap<>();
        SMTPConfiguration smtpc = ctx.cfg().getMailingConfiguration().getSmtpConfiguration();
        SSLClientConfiguration sslc = smtpc.getSslClientConfiguration();
        if ( smtpc.getSslClientMode() != SSLClientMode.DISABLE ) {
            extra.put("postfix_tls_protocols", makePostfixTLSProtocols(sslc)); //$NON-NLS-1$
            extra.put("postfix_tls_ciphers", makePostfixTLSCiphers(sslc)); //$NON-NLS-1$
            extra.put(
                "postfix_truststore_path", //$NON-NLS-1$
                SSLConfigUtil.getOpenSSLTruststorePath(ctx.cfg().getTrustConfiguration(), sslc.getTruststoreAlias()));
        }

        if ( sslc.getPinnedPublicKeys() != null && !sslc.getPinnedPublicKeys().isEmpty() ) {
            extra.put("use_tls_fingerprint", true); //$NON-NLS-1$
            List<String> fingerprints = new ArrayList<>();
            for ( PublicKeyEntry e : sslc.getPinnedPublicKeys() ) {

                byte[] encoded = e.getPublicKey().getEncoded();

                try {
                    MessageDigest instance = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
                    byte[] digest = instance.digest(encoded);
                    fingerprints.add(HashUtil.hexToDotted(Hex.encodeHexString(digest), false));
                }
                catch ( NoSuchAlgorithmException ex ) {
                    log.error("Failed to generate public key fingerprint", ex); //$NON-NLS-1$
                }
            }
            extra.put("tls_fingerprints", (Serializable) fingerprints); //$NON-NLS-1$
        }
        else {
            extra.put("use_tls_fingerprint", false); //$NON-NLS-1$
        }
        return extra;
    }


    /**
     * @param sslClientConfiguration
     * @return
     */
    private static String makePostfixTLSCiphers ( SSLClientConfiguration sslClientConfiguration ) {
        return StringUtils.join(SSLConfigUtil.toOpenSSLCiphers(sslClientConfiguration.getSecurityMode()), ","); //$NON-NLS-1$
    }


    /**
     * @param sslClientConfiguration
     * @return
     */
    private static String makePostfixTLSProtocols ( SSLClientConfiguration sslClientConfiguration ) {
        return StringUtils.join(SSLConfigUtil.toOpenSSLProtocols(sslClientConfiguration.getSecurityMode()), ","); //$NON-NLS-1$
    }
}
