/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.UnitFlags;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;
import eu.agno3.orchestrator.system.base.units.file.TemporaryFileResult;
import eu.agno3.orchestrator.system.file.hashtracking.FileHashTracker;
import eu.agno3.orchestrator.system.file.util.FileHashUtil;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.orchestrator.system.file.util.SerializablePath;


/**
 * @author mbechler
 * 
 */
public class Contents extends AbstractFileExecutionUnit<Contents, ContentsConfigurator> implements Named {

    /**
     * 
     */
    private static final String EXISTING_HASH_DOES_NOT_MATCH = "Existing hash for generated does not match the stored one for "; //$NON-NLS-1$
    /**
     * 
     */
    private static final long serialVersionUID = -7600746403314619138L;
    private boolean replaceFile = true;
    private SerializablePath tempFile;
    private SerializablePath backupFile;
    private ContentProvider contentProvider;
    private boolean noHashTracking;
    private byte[] newFileHash;
    private byte[] origFileHash;


    /**
     * @param replaceFile
     *            the replaceFile to set
     */
    void setReplaceFile ( boolean replaceFile ) {
        this.replaceFile = replaceFile;
    }


    /**
     * @return the replaceFile
     */
    protected boolean isReplaceFile () {
        return this.replaceFile;
    }


    /**
     * @param noHashTracking
     *            the noHashTracking to set
     */
    void setNoHashTracking ( boolean noHashTracking ) {
        this.noHashTracking = noHashTracking;
    }


    /**
     * @return the noHashTracking
     */
    public boolean isNoHashTracking () {
        return this.noHashTracking;
    }


    /**
     * @param contentProvider
     *            the contentProvider to set
     */
    void setContentProvider ( ContentProvider contentProvider ) {
        this.contentProvider = contentProvider;
    }


    /**
     * @return the contentProvider
     */
    protected ContentProvider getContentProvider () {
        return this.contentProvider;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);
        validateParams();
        validateEnvironment(context);
    }


    /**
     * @param context
     * @throws InvalidParameterException
     * @throws ExecutionException
     * @throws ResultReferenceException
     * @throws InvalidUnitConfigurationException
     */
    private void validateEnvironment ( Context context )
            throws InvalidParameterException, ExecutionException, ResultReferenceException, InvalidUnitConfigurationException {
        if ( this.isFollowSymlinks() ) {
            throw new InvalidParameterException("followSymlinks unsupported"); //$NON-NLS-1$
        }

        if ( this.contentProvider == null ) {
            throw new InvalidParameterException("No content provider set"); //$NON-NLS-1$
        }

        this.contentProvider.validate(context);

        if ( !this.noHashTracking ) {
            validateExistingHash(context);
        }
    }


    /**
     * @throws InvalidParameterException
     * @throws RequiredParameterMissingException
     */
    private void validateParams () throws InvalidParameterException, RequiredParameterMissingException {
        if ( this.getFileRef() != null ) {
            throw new InvalidParameterException("File reference set, this is not supported"); //$NON-NLS-1$
        }

        if ( this.getFile() == null ) {
            throw new RequiredParameterMissingException("Missing file specification"); //$NON-NLS-1$
        }

        if ( !this.isReplaceFile() && Files.exists(this.getFile(), this.getLinkOption()) ) {
            throw new InvalidParameterException("Target file exists and replaceFile is disabled"); //$NON-NLS-1$
        }
    }


    /**
     * @param context
     * @throws ResultReferenceException
     * @throws ExecutionException
     * @throws InvalidUnitConfigurationException
     */
    private void validateExistingHash ( Context context ) throws ResultReferenceException, ExecutionException, InvalidUnitConfigurationException {
        try {
            Path effectiveFile = this.getEffectiveFile(context);
            if ( Files.exists(effectiveFile) && !context.getFlag(UnitFlags.FORCE)
                    && !context.getConfig().getService(FileHashTracker.class).checkHash(effectiveFile, FileHashUtil.sha512(effectiveFile)) ) {
                throw new ExecutionException(EXISTING_HASH_DOES_NOT_MATCH + this.getFile());
            }
        }
        catch (
            NoSuchServiceException |
            NoSuchAlgorithmException |
            IOException e ) {
            throw new InvalidUnitConfigurationException("Could not check file hash", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult prepare ( Context context ) throws ExecutionException {

        Path effectiveFile = this.getEffectiveFile(context);
        if ( this.replaceFile ) {
            makeBackup(context, effectiveFile);
        }

        try {
            this.tempFile = SerializablePath.wrap(FileTemporaryUtils.createRelatedTemporaryFile(effectiveFile));
            context.getOutput().info("Generating file contents for " + this.getFile()); //$NON-NLS-1$
            doProduce(context);
            doFileMode(context);
            doHashFile();
            return new TemporaryFileResult(this.tempFile.unwrap(), this.getFile());
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to create temporary file", e); //$NON-NLS-1$
        }

    }


    /**
     * @param context
     * @param effectiveFile
     * @throws ExecutionException
     */
    private void makeBackup ( Context context, Path effectiveFile ) throws ExecutionException {
        if ( !Files.exists(effectiveFile) ) {
            return;
        }
        context.getOutput().info("Generating file backup for " + this.getFile()); //$NON-NLS-1$
        try {
            Path tempBackup = FileTemporaryUtils.createRelatedTemporaryFile(effectiveFile);
            Files.copy(effectiveFile, tempBackup, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

            if ( !this.noHashTracking ) {
                this.origFileHash = checkHash(context, effectiveFile, tempBackup);
            }

            this.backupFile = SerializablePath.wrap(tempBackup);
        }
        catch (
            IOException |
            NoSuchAlgorithmException |
            NoSuchServiceException e ) {
            throw new ExecutionException("Failed to create backup copy", e); //$NON-NLS-1$
        }
    }


    /**
     * @param context
     * @param effectiveFile
     * @param tempBackup
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws NoSuchServiceException
     * @throws ExecutionException
     */
    private byte[] checkHash ( Context context, Path effectiveFile, Path tempBackup )
            throws NoSuchAlgorithmException, IOException, NoSuchServiceException, ExecutionException {
        byte[] origHash = FileHashUtil.sha512(tempBackup);
        if ( !context.getConfig().getService(FileHashTracker.class).checkHash(effectiveFile, origHash) ) {
            if ( context.getFlag(UnitFlags.FORCE) ) {
                context.getOutput().info("Overwriting files with wrong hash: " + this.getFile()); //$NON-NLS-1$
            }
            else {
                throw new ExecutionException(EXISTING_HASH_DOES_NOT_MATCH + this.getFile());
            }
        }
        return origHash;
    }


    /**
     * @throws IOException
     * 
     */
    private void doHashFile () throws IOException {
        if ( this.noHashTracking ) {
            return;
        }
        try {
            this.newFileHash = FileHashUtil.sha512(this.tempFile.unwrap());
        }
        catch ( NoSuchAlgorithmException e ) {
            throw new IOException("Cannot generate file hash", e); //$NON-NLS-1$
        }
    }


    private void doFileMode ( Context ctx ) throws ExecutionException {
        try {
            this.ensureFileMode(ctx, this.tempFile.unwrap());
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to set file permissions", e); //$NON-NLS-1$
        }
    }


    private void doProduce ( Context ctx ) throws ExecutionException {
        try ( FileChannel c = FileChannel.open(this.tempFile.unwrap(), LinkOption.NOFOLLOW_LINKS, StandardOpenOption.WRITE); ) {
            c.lock();
            this.contentProvider.transferTo(ctx, c);
            if ( c.isOpen() ) {
                c.force(true);
            }
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to generate file contents", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult execute ( Context context ) throws ExecutionException {
        super.execute(context);
        try {
            this.ensureFileModeLazy(context, this.tempFile.unwrap());
            context.getOutput().info("Moving file to it's destination " + this.getFile()); //$NON-NLS-1$
            FileUtil.safeMove(this.tempFile.unwrap(), this.getEffectiveFile(context), this.replaceFile);

            if ( !this.noHashTracking ) {
                updateHashInternal(context);
            }
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to move temporary file to it's destination", e); //$NON-NLS-1$
        }

        return new FileResult(this.getFile());
    }


    /**
     * @param context
     * @throws ResultReferenceException
     * @throws ExecutionException
     */
    private void updateHashInternal ( Context context ) throws ResultReferenceException, ExecutionException {
        try {
            context.getConfig().getService(FileHashTracker.class).updateHash(this.getEffectiveFile(context), this.newFileHash);
        }
        catch (
            NoSuchServiceException |
            IOException e ) {
            throw new ExecutionException("Failed to update file hash", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#rollback(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void rollback ( Context context ) throws ExecutionException {

        if ( this.replaceFile && this.backupFile != null ) {
            try {
                Path unwrap = this.backupFile.unwrap();
                FileUtil.safeMove(unwrap, this.getEffectiveFile(context), true);
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to restore backup copy", e); //$NON-NLS-1$
            }

        }
        else {
            try {
                Files.deleteIfExists(this.getEffectiveFile(context));
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to remove created file", e); //$NON-NLS-1$
            }
        }

        if ( !this.noHashTracking ) {
            try {
                context.getConfig().getService(FileHashTracker.class).updateHash(this.getEffectiveFile(context), this.origFileHash);
            }
            catch (
                NoSuchServiceException |
                IOException e ) {
                throw new ExecutionException("Failed to restore file hash", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#cleanup(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void cleanup ( Context context ) throws ExecutionException {
        Exception error = null;
        if ( this.tempFile != null ) {
            try {
                Files.deleteIfExists(this.tempFile.unwrap());
            }
            catch ( IOException e ) {
                context.getOutput().debug("Failed to remove temporary file", e); //$NON-NLS-1$
                error = e;
            }
        }

        if ( this.backupFile != null ) {
            try {
                Files.deleteIfExists(this.backupFile.unwrap());
            }
            catch ( IOException e ) {
                context.getOutput().debug("Failed to remove backup file", e); //$NON-NLS-1$
                error = e;
            }
        }

        super.cleanup(context);

        if ( error != null ) {
            throw new ExecutionException("Cleanup failed", error); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ContentsConfigurator createConfigurator () {
        return new ContentsConfigurator(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return String.format("Generate file %s", this.getFile()); //$NON-NLS-1$
    }
}
