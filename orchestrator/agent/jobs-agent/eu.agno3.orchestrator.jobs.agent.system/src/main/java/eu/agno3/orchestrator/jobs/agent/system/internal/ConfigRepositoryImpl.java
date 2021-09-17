/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.agent.system.ServiceDirFilter;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ConfigRepository.class, SystemService.class
}, configurationPid = ConfigRepositoryImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
@SystemServiceType ( ConfigRepository.class )
public class ConfigRepositoryImpl implements ConfigRepository {

    private static final Logger log = Logger.getLogger(ConfigRepositoryImpl.class);

    /**
     * 
     */
    public static final String PID = "config.repository"; //$NON-NLS-1$

    private static final String CONFIG_REPOSITORY_BASE = "base"; //$NON-NLS-1$

    private static final String DEFAULT_REPOSITORY_BASE = "/var/lib/orchagent/config/"; //$NON-NLS-1$

    private static final String TYPE = ".type"; //$NON-NLS-1$
    private static final String STATE = ".state"; //$NON-NLS-1$
    private static final String APPLIED_REVISION = ".appliedRevision"; //$NON-NLS-1$

    private static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private File configRepositoryBase;

    private XmlMarshallingService xmlMarshaller;

    private ServiceTypeRegistry serviceTypeRegistry;

    private FileLock lock;


    @Reference
    protected synchronized void setXmlMarshaller ( XmlMarshallingService ms ) {
        this.xmlMarshaller = ms;
    }


    protected synchronized void unsetXmlMarshaller ( XmlMarshallingService ms ) {
        if ( this.xmlMarshaller == ms ) {
            this.xmlMarshaller = null;
        }
    }


    @Reference
    protected synchronized void setServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        this.serviceTypeRegistry = reg;
    }


    protected synchronized void unsetServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        if ( this.serviceTypeRegistry == reg ) {
            this.serviceTypeRegistry = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {
        String configRepositoryBaseSpec = (String) ctx.getProperties().get(CONFIG_REPOSITORY_BASE);

        if ( StringUtils.isBlank(configRepositoryBaseSpec) ) {
            configRepositoryBaseSpec = DEFAULT_REPOSITORY_BASE;
        }

        File repoBase = new File(configRepositoryBaseSpec.trim());

        if ( !repoBase.exists() ) {
            try {
                Files.createDirectories(repoBase.toPath(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));
            }
            catch ( IOException e ) {
                throw new IOException("Failed to set repository config permissions", e); //$NON-NLS-1$
            }
        }

        if ( !repoBase.isDirectory() || !repoBase.canWrite() ) {
            throw new IOException("Config repository is not a writable directory"); //$NON-NLS-1$
        }

        this.configRepositoryBase = repoBase.getCanonicalFile();

        this.lock = new FileLock( ( new File(this.configRepositoryBase, ".lock") ).toPath()); //$NON-NLS-1$
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) throws IOException {
        if ( this.lock != null ) {
            this.lock.close();
            this.lock = null;
        }
    }


    /**
     * @param service
     * @return
     * @throws ConfigRepositoryException
     */
    protected Path getServicePath ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        if ( service == null ) {
            throw new ConfigRepositoryException("No service given"); //$NON-NLS-1$
        }
        return this.configRepositoryBase.toPath().resolve(service.getId().toString());
    }


    /**
     * @param service
     * @return
     * @throws ConfigRepositoryException
     */
    private Path getServicePath ( StructuralObjectReference service ) throws ConfigRepositoryException {
        if ( service == null ) {
            throw new ConfigRepositoryException("No service given"); //$NON-NLS-1$
        }
        return this.configRepositoryBase.toPath().resolve(service.getId().toString());
    }


    @Override
    public Collection<ServiceStructuralObject> getServices () throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainReadLock() ) {
            Collection<ServiceStructuralObject> services = new ArrayList<>();
            for ( File serviceDir : this.configRepositoryBase.listFiles(new ServiceDirFilter()) ) {
                services.add(getService(serviceDir.toPath()));
            }
            return services;
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get services", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigRepository#getServicesByType(java.lang.String)
     */
    @Override
    public Collection<ServiceStructuralObject> getServicesByType ( String serviceType ) throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainReadLock() ) {
            Collection<ServiceStructuralObject> services = new ArrayList<>();
            for ( File serviceDir : this.configRepositoryBase.listFiles(new ServiceDirFilter()) ) {
                ServiceStructuralObjectImpl service = getService(serviceDir.toPath());
                if ( serviceType.equals(service.getServiceType()) ) {
                    services.add(service);
                }
            }
            return services;
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get services", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigRepository#getServiceReferences()
     */
    @Override
    public Collection<StructuralObjectReference> getServiceReferences () throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainReadLock() ) {
            Collection<StructuralObjectReference> services = new ArrayList<>();
            for ( File serviceDir : this.configRepositoryBase.listFiles(new ServiceDirFilter()) ) {
                services.add(getServiceReference(serviceDir.toPath()));
            }
            return services;
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get services", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigRepository#getSingletonServiceByType(java.lang.String)
     */
    @Override
    public ServiceStructuralObject getSingletonServiceByType ( String serviceType ) throws ConfigRepositoryException {

        ServiceStructuralObject found = null;
        for ( File serviceDir : this.configRepositoryBase.listFiles(new ServiceDirFilter()) ) {
            ServiceStructuralObject service = getService(serviceDir.toPath());
            if ( service.getServiceType().equals(serviceType) && found == null ) {
                found = service;
            }
            else if ( service.getServiceType().equals(serviceType) ) {
                throw new ConfigRepositoryException("Multiple matches found for singleton service " + serviceType); //$NON-NLS-1$
            }
        }
        return found;
    }


    /**
     * @param serviceDir
     * @return
     * @throws ConfigRepositoryException
     */
    protected ServiceStructuralObjectImpl getService ( Path servicePath ) throws ConfigRepositoryException {
        ServiceStructuralObjectImpl service = new ServiceStructuralObjectImpl();
        UUID uuid = UUID.fromString(servicePath.toFile().getName());
        if ( uuid == null ) {
            throw new ConfigRepositoryException();
        }
        service.setId(uuid);
        service.setServiceType(getServiceType(servicePath));
        service.setState(getServiceState(servicePath));
        service.setAppliedRevision(getAppliedRevision(servicePath));
        return service;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ConfigRepositoryException
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.ConfigRepository#getService(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference)
     */
    @Override
    public ServiceStructuralObject getService ( StructuralObjectReference service ) throws ConfigRepositoryException {
        return getService(getServicePath(service));
    }


    /**
     * @param serviceDir
     * @return
     * @throws ConfigRepositoryException
     */
    protected StructuralObjectReference getServiceReference ( Path servicePath ) throws ConfigRepositoryException {
        UUID uuid = UUID.fromString(servicePath.toFile().getName());
        if ( uuid == null ) {
            throw new ConfigRepositoryException();
        }
        return new StructuralObjectReferenceImpl(uuid, StructuralObjectType.SERVICE, getServiceType(servicePath));
    }


    @Override
    public void registerService ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainWriteLock() ) {
            registerServiceInternal(service);
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to register service", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @throws ConfigRepositoryException
     * @throws IOException
     */
    private void registerServiceInternal ( ServiceStructuralObject service ) throws ConfigRepositoryException, IOException {
        Path serviceDir = getServicePath(service);

        if ( Files.exists(serviceDir) ) {
            throw new ConfigRepositoryException("Service already registered"); //$NON-NLS-1$
        }

        Files.createDirectory(serviceDir, PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyDirPermissions()));

        try {
            setServiceState(serviceDir, ConfigurationState.UNCONFIGURED);
            setServiceType(serviceDir, service.getServiceType());
        }
        catch ( ConfigRepositoryException e ) {
            Files.deleteIfExists(serviceDir.resolve(TYPE));
            Files.deleteIfExists(serviceDir.resolve(APPLIED_REVISION));
            Files.deleteIfExists(serviceDir.resolve(STATE));
            Files.delete(serviceDir);
            throw e;
        }
    }


    @Override
    public ServiceStructuralObject ensureServiceRegistered ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainWriteLock() ) {
            Path servicePath = getServicePath(service);

            if ( !Files.exists(servicePath) ) {
                this.registerServiceInternal(service);
                return service;
            }

            ServiceStructuralObjectImpl stored = getService(servicePath);
            checkServiceMatch(service, stored);
            return stored;
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to ensure service is registered", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @param stored
     * @throws ConfigRepositoryException
     */
    protected void checkServiceMatch ( ServiceStructuralObject service, ServiceStructuralObjectImpl stored ) throws ConfigRepositoryException {
        if ( !stored.getId().equals(service.getId()) ) {
            throw new ConfigRepositoryException("Config store inconsistent: id mismatch"); //$NON-NLS-1$
        }

        if ( !stored.getServiceType().equals(service.getServiceType()) ) {
            throw new ConfigRepositoryException("Config store inconsistent: service type mismatch"); //$NON-NLS-1$
        }

        if ( doAppliedRevisionsMismatch(service, stored) ) {
            log.warn(String.format(
                "Server and agent do not agree over the currently applied revision (server: %d, agent: %d)", //$NON-NLS-1$
                service.getAppliedRevision(),
                stored.getAppliedRevision()));
        }
    }


    private static boolean doAppliedRevisionsMismatch ( ServiceStructuralObject service, ServiceStructuralObjectImpl stored ) {
        return ( stored.getAppliedRevision() != null && stored.getAppliedRevision() > 0
                && !stored.getAppliedRevision().equals(service.getAppliedRevision()) )
                || ( stored.getAppliedRevision() == null && service.getAppliedRevision() != null );
    }


    @Override
    public void removeService ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainWriteLock() ) {
            Path serviceDir = getServicePath(service);
            if ( !Files.exists(serviceDir) ) {
                throw new ConfigRepositoryException("Service not registered"); //$NON-NLS-1$
            }
            FileUtils.deleteDirectory(serviceDir.toFile());
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get remove service", e); //$NON-NLS-1$
        }
    }


    /**
     * @param path
     * @return
     * @throws ConfigRepositoryException
     */
    private static Long getAppliedRevision ( Path path ) throws ConfigRepositoryException {
        try {
            Path revPath = path.resolve(APPLIED_REVISION);
            if ( !Files.exists(revPath) ) {
                return null;
            }

            List<String> lines = Files.readAllLines(revPath, CHARSET);
            if ( lines.size() != 1 ) {
                throw new ConfigRepositoryException("Illegal service applied revision file format"); //$NON-NLS-1$
            }
            return Long.valueOf(lines.get(0).trim());
        }
        catch (
            IOException |
            IllegalArgumentException e ) {
            throw new ConfigRepositoryException("Failed to get service applied revision", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @return
     * @throws ConfigRepositoryException
     */
    private Long getAppliedRevision ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        return getAppliedRevision(getServicePath(service));
    }


    /**
     * @param service
     * @param oldActiveRev
     * @throws ConfigRepositoryException
     */
    private void setAppliedRevision ( ServiceStructuralObject service, long rev ) throws ConfigRepositoryException {
        setAppliedRevision(getServicePath(service), rev);
    }


    /**
     * @param servicePath
     * @param rev
     * @throws ConfigRepositoryException
     */
    private static void setAppliedRevision ( Path servicePath, long rev ) throws ConfigRepositoryException {
        try {
            Files.write(servicePath.resolve(APPLIED_REVISION), Arrays.asList((CharSequence) Long.toString(rev)), CHARSET);
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to set service applied revision", e); //$NON-NLS-1$
        }
    }


    /**
     * @param path
     * @return
     * @throws ConfigRepositoryException
     */
    @SuppressWarnings ( "all" )
    private static @NonNull ConfigurationState getServiceState ( Path path ) throws ConfigRepositoryException {
        try {
            List<String> lines = Files.readAllLines(path.resolve(STATE), CHARSET);
            if ( lines.size() != 1 ) {
                throw new ConfigRepositoryException("Illegal service state file format"); //$NON-NLS-1$
            }
            return ConfigurationState.valueOf(lines.get(0).trim());
        }
        catch (
            IOException |
            IllegalArgumentException e ) {
            throw new ConfigRepositoryException("Failed to get service state", e); //$NON-NLS-1$
        }
    }


    /**
     * @param serviceDir
     * @param unconfigured
     * @throws ConfigRepositoryException
     */
    private static void setServiceState ( Path serviceDir, ConfigurationState unconfigured ) throws ConfigRepositoryException {
        try {
            Files.write(serviceDir.resolve(STATE), Arrays.asList((CharSequence) unconfigured.name()), CHARSET);
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to set service state", e); //$NON-NLS-1$
        }
    }


    /**
     * @param serviceDir
     * @return
     * @throws ConfigRepositoryException
     */
    private static String getServiceType ( Path path ) throws ConfigRepositoryException {
        try {
            List<String> lines = Files.readAllLines(path.resolve(TYPE), CHARSET);
            if ( lines.size() != 1 ) {
                throw new ConfigRepositoryException("Illegal service type file format"); //$NON-NLS-1$
            }
            return lines.get(0).trim();
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get service type", e); //$NON-NLS-1$
        }
    }


    /**
     * @param serviceDir
     * @param serviceType
     * @throws ConfigRepositoryException
     */
    private static void setServiceType ( Path serviceDir, String serviceType ) throws ConfigRepositoryException {
        try {
            Files.write(serviceDir.resolve(TYPE), Arrays.asList((CharSequence) serviceType), CHARSET);
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to set service type", e); //$NON-NLS-1$
        }
    }


    protected ServiceTypeDescriptor<@NonNull ?, @NonNull ?> getServiceTypeDescriptor ( Path path ) throws ConfigRepositoryException {
        try {
            return this.serviceTypeRegistry.getDescriptor(getServiceType(path));
        }
        catch ( ModelServiceException e ) {
            throw new ConfigRepositoryException("Failed to get service type descriptor", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @return the service type descriptor
     * @throws ConfigRepositoryException
     */
    @Override
    public ServiceTypeDescriptor<@NonNull ?, @NonNull ?> getServiceTypeDescriptor ( ServiceStructuralObject service )
            throws ConfigRepositoryException {
        return this.getServiceTypeDescriptor(getServicePath(service));
    }


    /**
     * @param service
     * @return the path to the active service configuration
     * @throws ConfigRepositoryException
     */
    protected Path getActiveConfigurationFile ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        return getServicePath(service).resolve("activeConfig.xml"); //$NON-NLS-1$
    }


    /**
     * @param service
     * @return the path to the failsafe service configuration
     * @throws ConfigRepositoryException
     */
    protected Path getFailsafeConfigurationFile ( ServiceStructuralObject service ) throws ConfigRepositoryException {
        return getServicePath(service).resolve("failsafeConfig.xml"); //$NON-NLS-1$
    }


    @Override
    public <T extends ConfigurationInstance> void setActiveConfiguration ( ServiceStructuralObject service, T config )
            throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainWriteLock() ) {
            Path activeConfig = this.getActiveConfigurationFile(service);
            Path failsafeConfig = this.getFailsafeConfigurationFile(service);
            Path newConfig = FileTemporaryUtils.createRelatedTemporaryFile(activeConfig);
            Path bakFailsafe = null;

            if ( config.getRevision() == null ) {
                throw new ConfigRepositoryException("No revision set on config"); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Switching active configuration to [%s]", config)); //$NON-NLS-1$
            }

            try {
                marshallConfiguration(config, newConfig);
                bakFailsafe = makeFailsafeBackup(failsafeConfig);
                switchConfiguration(service, config, activeConfig, failsafeConfig, newConfig, bakFailsafe);
            }
            finally {
                Files.deleteIfExists(newConfig);
                if ( bakFailsafe != null ) {
                    Files.deleteIfExists(bakFailsafe);
                }
            }

        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to set active configuration for service " + service, e); //$NON-NLS-1$
        }
    }


    /**
     * @param failsafeConfig
     * @param bakFailsafe
     * @return
     * @throws IOException
     */
    private static Path makeFailsafeBackup ( Path failsafeConfig ) throws IOException {
        // backup failsafe config
        Path bakFailsafe = null;
        if ( Files.exists(failsafeConfig) ) {
            bakFailsafe = FileTemporaryUtils.createRelatedTemporaryFile(failsafeConfig);
            Files.copy(failsafeConfig, bakFailsafe, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        }
        return bakFailsafe;
    }


    /**
     * @param activeConfig
     * @param failsafeConfig
     * @param newConfig
     * @param bakFailsafe
     * @throws IOException
     * @throws ConfigRepositoryException
     */
    protected void switchConfiguration ( ServiceStructuralObject service, ConfigurationInstance config, Path activeConfig, Path failsafeConfig,
            Path newConfig, Path bakFailsafe ) throws ConfigRepositoryException {
        Long oldActiveRev = getAppliedRevision(service);
        try {
            // write last active to failsafe
            if ( Files.exists(activeConfig) ) {
                FileUtil.safeMove(activeConfig, failsafeConfig, true);
            }

            // write new active
            FileUtil.safeMove(newConfig, activeConfig, false);

            setAppliedRevision(service, config.getRevision());
            setServiceState(getServicePath(service), ConfigurationState.APPLIED);
        }
        catch ( IOException e ) {
            log.warn("Failed to switch configuration", e); //$NON-NLS-1$
            if ( bakFailsafe != null ) {
                revertConfiguration(service, activeConfig, failsafeConfig, bakFailsafe, oldActiveRev);
            }
            throw new ConfigRepositoryException("Failed to switch to new configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @param activeConfig
     * @param failsafeConfig
     * @param bakFailsafe
     * @param oldActiveRev
     * @throws IOException
     * @throws ConfigRepositoryException
     */
    private void revertConfiguration ( ServiceStructuralObject service, Path activeConfig, Path failsafeConfig, Path bakFailsafe,
            Long oldActiveRev ) {
        try {
            if ( Files.exists(failsafeConfig) ) {
                FileUtil.safeMove(failsafeConfig, activeConfig, true);
            }
            FileUtil.safeMove(bakFailsafe, failsafeConfig, true);

            if ( oldActiveRev != null ) {
                setAppliedRevision(service, oldActiveRev);
            }
            else {
                Files.deleteIfExists(getServicePath(service).resolve(APPLIED_REVISION));
            }
        }
        catch (
            IOException |
            ConfigRepositoryException e ) {
            log.error("Failed to revert configuration after save failure", e); //$NON-NLS-1$
        }
    }


    /**
     * @param config
     * @param newConfig
     * @throws ConfigRepositoryException
     * @throws IOException
     */
    protected <T extends ConfigurationInstance> void marshallConfiguration ( T config, Path newConfig ) throws ConfigRepositoryException {
        try ( FileChannel ch = FileChannel.open(
            newConfig,
            EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))); //$NON-NLS-1$
              OutputStream fos = Channels.newOutputStream(ch) ) {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fos);
            this.xmlMarshaller.marshall(config, writer);
        }
        catch (
            IOException |
            XMLBindingException |
            XMLStreamException |
            FactoryConfigurationError e ) {
            throw new ConfigRepositoryException("Failed to marshall configuration", e); //$NON-NLS-1$
        }
    }


    @Override
    public <@NonNull T extends ConfigurationInstance> Optional<T> getActiveConfiguration ( ServiceStructuralObject service )
            throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainReadLock() ) {
            Path activeConfig = this.getActiveConfigurationFile(service);

            if ( !Files.exists(activeConfig) ) {
                return Optional.empty();
            }

            return Optional.of(unmarshallConfig(service, activeConfig));
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get active configuration for service " + service, e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @return
     * @throws ConfigRepositoryException
     * @throws IOException
     */
    @SuppressWarnings ( "unchecked" )
    protected <T extends ConfigurationInstance> T unmarshallConfig ( ServiceStructuralObject service, Path config ) throws ConfigRepositoryException {
        ServiceTypeDescriptor<?, ?> serviceDesc = getServiceTypeDescriptor(service);
        try ( FileInputStream fis = new FileInputStream(config.toFile()) ) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fis);

            if ( reader == null ) {
                throw new XMLStreamException();
            }

            return (T) this.xmlMarshaller.unmarshall(serviceDesc.getConfigurationType(), reader);
        }
        catch (
            XMLStreamException |
            FactoryConfigurationError |
            XMLBindingException |
            IOException e ) {
            throw new ConfigRepositoryException("Failed to unmarshall configuration", e); //$NON-NLS-1$
        }
    }


    @Override
    public <T extends ConfigurationInstance> Optional<T> getFailsafeConfiguration ( ServiceStructuralObject service )
            throws ConfigRepositoryException {
        try ( LockContext l = this.lock.obtainReadLock() ) {
            Path failsafeConfig = this.getFailsafeConfigurationFile(service);

            if ( !Files.exists(failsafeConfig) ) {
                return Optional.empty();
            }

            return Optional.ofNullable(unmarshallConfig(service, failsafeConfig));
        }
        catch ( IOException e ) {
            throw new ConfigRepositoryException("Failed to get failsafe configuration for service " + service, e); //$NON-NLS-1$
        }
    }

}
