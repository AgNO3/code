/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.hostconfig.agent.api.SMTPConfigurator;
import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfiguration;
import eu.agno3.orchestrator.config.web.SMTPConfiguration;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.units.exec.Exec;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.base.units.file.contents.Contents;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.security.password.PasswordGenerationException;
import eu.agno3.runtime.security.password.PasswordGenerator;
import eu.agno3.runtime.security.password.PasswordType;


/**
 * @author mbechler
 *
 */
@Component ( service = SMTPConfigurator.class )
public class SMTPConfiguratorImpl implements SMTPConfigurator {

    /**
     * 
     */
    private static final Charset PW_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private ExecutionConfigProperties executionConfig;
    private PasswordGenerator passwordGenerator;


    @Reference
    protected synchronized void setExecutionConfig ( ExecutionConfigProperties ec ) {
        this.executionConfig = ec;
    }


    protected synchronized void unsetExecutionConfig ( ExecutionConfigProperties ec ) {
        if ( this.executionConfig == ec ) {
            this.executionConfig = null;
        }
    }


    @Reference
    protected synchronized void setPasswordGenerator ( PasswordGenerator pg ) {
        this.passwordGenerator = pg;
    }


    protected synchronized void unsetPasswordGenerator ( PasswordGenerator pg ) {
        if ( this.passwordGenerator == pg ) {
            this.passwordGenerator = null;
        }
    }


    /**
     * @param b
     * @param ctx
     * @param mc
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     * @throws ServiceManagementException
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void setupSMTPClient ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, MailingConfiguration mc )
            throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        SMTPConfiguration sc = mc.getSmtpConfiguration();

        UserPrincipal servicePrincipal = ctx.getServiceManager().getServicePrincipal();

        String username = servicePrincipal != null ? servicePrincipal.getName() : "default";
        String password = null;

        Path pwFile = Paths.get("/etc/sasldb2.d/localsmtp/", username);
        Path realPwFile = PrefixUtil.resolvePrefix(this.executionConfig, pwFile);

        if ( !Files.exists(realPwFile, LinkOption.NOFOLLOW_LINKS) ) {
            try {
                password = this.passwordGenerator.generate(PasswordType.RANDOM, 64, Locale.ROOT);
            }
            catch ( PasswordGenerationException e ) {
                throw new UnitInitializationFailedException("Failed to generate password", e);
            }

            b.add(Contents.class).file(pwFile).content(password.getBytes(PW_CHARSET)).perms(FileSecurityUtils.getOwnerOnlyFilePermissions())
                    .owner(servicePrincipal)
                    .createTargetDir(FileSecurityUtils.getGroupReadDirPermissions(), null, ctx.getServiceManager().getGroupPrincipal());

            b.add(Exec.class).cmd("/usr/sbin/update-sasldb");
        }
        else if ( Files.exists(pwFile, LinkOption.NOFOLLOW_LINKS) ) {
            try {
                byte[] userPwBytes = Files.readAllBytes(pwFile);
                password = new String(userPwBytes, PW_CHARSET);
            }
            catch ( IOException e ) {
                throw new UnitInitializationFailedException("Failed to read password", e);
            }
        }
        else {
            try {
                byte[] userPwBytes = Files.readAllBytes(realPwFile);
                password = new String(userPwBytes, PW_CHARSET);
            }
            catch ( IOException e ) {
                throw new UnitInitializationFailedException("Failed to read password", e);
            }
        }

        ctx.factory(
            "smtp",
            "local",
            PropertyConfigBuilder.get().p("readTimeout", sc.getSocketTimeout()).p("writeTimeout", sc.getSocketTimeout())
                    .p("connTimeout", sc.getSocketTimeout()).p("smtpHost", "localhost").p("smtpPort", 587)
                    .p("overrideHostname", sc.getOverrideEhloHostName()).p("defaultFromAddress", sc.getOverrideDefaultFromAddress())
                    .p("defaultFromName", sc.getOverrideDefaultFromName()).p("enableSSL", false).p("enableStartTLS", false).p("enableAuth", true)
                    .p("smtpUser", username).p("smtpPassword", password).p("authMechs", Arrays.asList("PLAIN")));

    }
}
