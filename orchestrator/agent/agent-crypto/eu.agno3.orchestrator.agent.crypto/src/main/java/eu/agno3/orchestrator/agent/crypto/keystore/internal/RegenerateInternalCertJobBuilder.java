/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.InternalCAConfig;
import eu.agno3.orchestrator.agent.crypto.keystore.units.GenerateSignedCertificate;
import eu.agno3.orchestrator.crypto.jobs.RegenerateInternalCertJob;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.X509Util;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    JobRunnableFactory.class
}, property = "jobType=eu.agno3.orchestrator.crypto.jobs.RegenerateInternalCertJob" )
@JobType ( RegenerateInternalCertJob.class )
public class RegenerateInternalCertJobBuilder extends AbstractSystemJobRunnableFactory<RegenerateInternalCertJob> {

    private InternalCAConfig internalCAConfig;
    private X509Util x509util;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Reference
    @Override
    protected void setExecutionConfig ( ExecutionConfig cfg ) {
        super.setExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetExecutionConfig(eu.agno3.orchestrator.system.base.execution.ExecutionConfig)
     */
    @Override
    protected void unsetExecutionConfig ( ExecutionConfig cfg ) {
        super.unsetExecutionConfig(cfg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#setRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Reference
    @Override
    protected void setRunnerFactory ( RunnerFactory factory ) {
        super.setRunnerFactory(factory);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#unsetRunnerFactory(eu.agno3.orchestrator.system.base.execution.RunnerFactory)
     */
    @Override
    protected void unsetRunnerFactory ( RunnerFactory factory ) {
        super.unsetRunnerFactory(factory);
    }


    @Reference
    protected synchronized void setInternalCAConfig ( InternalCAConfig cfg ) {
        this.internalCAConfig = cfg;
    }


    protected synchronized void unsetInternalCAConfig ( InternalCAConfig cfg ) {
        if ( this.internalCAConfig == cfg ) {
            this.internalCAConfig = null;
        }
    }


    @Reference
    protected synchronized void setX509Util ( X509Util util ) {
        this.x509util = util;
    }


    protected synchronized void unsetX509Util ( X509Util util ) {
        if ( this.x509util == util ) {
            this.x509util = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#buildJob(eu.agno3.orchestrator.system.base.execution.JobBuilder,
     *      eu.agno3.orchestrator.jobs.agent.system.Job)
     */
    @Override
    protected void buildJob ( @NonNull JobBuilder b, @NonNull RegenerateInternalCertJob j ) throws JobBuilderException {

        try {
            DateTime validFrom = DateTime.now();
            DateTime validTo = makeValidTo(j, validFrom);
            int keyUsage = j.getKeyUsage() & this.internalCAConfig.getKeyUsageMask();
            KeyPurposeId[] eku = makeEkus(j.getExtendedKeyUsage());
            GeneralName[] sans = makeSans(j.getSanAdresses());
            Set<CertExtension> exts = new HashSet<>(this.x509util.getDefaultClientExtensions(keyUsage, eku, sans));

            List<X509Certificate> caExtraChain = new ArrayList<>();
            if ( j.getIncludeCA() ) {
                caExtraChain.add(this.internalCAConfig.getCaCertificate());
            }
            caExtraChain.addAll(Arrays.asList(this.internalCAConfig.getCaExtraChain()));

            b.add(GenerateSignedCertificate.class).keystore(j.getKeyStore()).alias(j.getKeyAlias())
                    .signingKeystore(this.internalCAConfig.getCaKeystoreName()).signingKeyAlias(this.internalCAConfig.getCaKeyAlias())
                    .extraChain(caExtraChain).subject(j.getSubject()).validFrom(validFrom).validTo(validTo).extensions(exts);

        }
        catch (
            UnitInitializationFailedException |
            CryptoException e ) {
            throw new JobBuilderException("Failed to build certificate regeneration job", e); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @param validFrom
     * @return
     */
    private DateTime makeValidTo ( RegenerateInternalCertJob j, DateTime validFrom ) {
        DateTime validTo;
        if ( j.getLifetime().isLongerThan(this.internalCAConfig.getMaximumLifetime()) ) {
            validTo = validFrom.plus(j.getLifetime());
        }
        else {
            validTo = validFrom.plus(this.internalCAConfig.getMaximumLifetime());
        }
        return validTo;
    }


    /**
     * @param extendedKeyUsage
     * @return
     */
    private KeyPurposeId[] makeEkus ( Set<ASN1ObjectIdentifier> extendedKeyUsage ) {
        if ( extendedKeyUsage == null ) {
            return new KeyPurposeId[] {};
        }
        List<KeyPurposeId> allowedKpus = new ArrayList<>();

        for ( ASN1ObjectIdentifier oid : extendedKeyUsage ) {
            KeyPurposeId kp = KeyPurposeId.getInstance(oid);
            if ( this.internalCAConfig.isExtendedKeyUsageAllowed(kp) ) {
                allowedKpus.add(kp);
            }
        }

        return allowedKpus.toArray(new KeyPurposeId[] {});
    }


    /**
     * @param sanAdresses
     * @return
     */
    private static GeneralName[] makeSans ( Set<HostOrAddress> sanAdresses ) {
        if ( sanAdresses == null ) {
            return new GeneralName[] {};
        }
        GeneralName[] sans = new GeneralName[sanAdresses.size()];
        int i = 0;
        for ( HostOrAddress addr : sanAdresses ) {
            sans[ i ] = makeSan(addr);
            i++;
        }
        return sans;
    }


    /**
     * @param addr
     * @return
     */
    private static GeneralName makeSan ( HostOrAddress addr ) {
        if ( addr.isHostName() ) {
            return new GeneralName(GeneralName.dNSName, addr.getHostName());
        }
        return new GeneralName(GeneralName.iPAddress, addr.getAddress().toString());
    }
}
