/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.remove;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.NoSuchServiceException;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;
import eu.agno3.orchestrator.system.base.units.file.TemporaryFileResult;
import eu.agno3.orchestrator.system.file.hashtracking.FileHashTracker;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.orchestrator.system.file.util.SerializablePath;


/**
 * @author mbechler
 * 
 */
public class Remove extends AbstractFileExecutionUnit<Remove, RemoveConfigurator> implements Named {

    /**
     * 
     */
    private static final long serialVersionUID = -2640622976317001481L;
    private SerializablePath backupFile;

    private boolean removeHash;


    /**
     * @return the removeHash
     */
    public boolean isRemoveHash () {
        return this.removeHash;
    }


    /**
     * @param removeHash
     *            the removeHash to set
     */
    public void setRemoveHash ( boolean removeHash ) {
        this.removeHash = removeHash;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( isFileMissing() ) {
            throw new RequiredParameterMissingException("Missing file"); //$NON-NLS-1$
        }

        if ( isBothRefAndFileSet() ) {
            throw new InvalidParameterException("Both file and reference set"); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    private boolean isBothRefAndFileSet () {
        return this.getFile() != null && this.getFileRef() != null;
    }


    /**
     * @return
     */
    private boolean isFileMissing () {
        return this.getFile() == null && this.getFileRef() == null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult prepare ( Context context ) throws ExecutionException {

        if ( !this.isFollowSymlinks() && Files.isSymbolicLink(this.getEffectiveFile(context)) ) {
            throw new ExecutionException("File is a symbolic link and not following links"); //$NON-NLS-1$
        }

        if ( this.getFileRef() != null ) {
            return prepareReference(context);
        }

        return new FileResult(this.getFile());
    }


    /**
     * @param context
     * @return
     * @throws ResultReferenceException
     * @throws ExecutionException
     */
    private FileResult prepareReference ( Context context ) throws ExecutionException {
        FileResult res = context.fetchResult(this.getFileRef());
        if ( res instanceof TemporaryFileResult ) {
            return new TemporaryFileResult(res.getPath(), ( (TemporaryFileResult) res ).getActualTarget());
        }
        return new FileResult(res.getPath());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult execute ( Context context ) throws ExecutionException {
        Path effectiveFile = this.getEffectiveFile(context);

        context.getOutput().info("Remove file " + this.getFile()); //$NON-NLS-1$

        if ( !Files.exists(effectiveFile) ) {
            return new FileResult(effectiveFile);
        }

        try {
            this.backupFile = SerializablePath.wrap(FileTemporaryUtils.createRelatedTemporaryFile(effectiveFile));
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to create backup", e); //$NON-NLS-1$
        }

        try {
            FileUtil.safeMove(effectiveFile, this.backupFile.unwrap(), true);
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to move file to backup file", e); //$NON-NLS-1$
        }
        return new FileResult(effectiveFile);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#rollback(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void rollback ( Context context ) throws ExecutionException {
        try {
            if ( this.backupFile != null ) {
                FileUtil.safeMove(this.backupFile.unwrap(), this.getEffectiveFile(context), false);
            }
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to restore file to original location", e); //$NON-NLS-1$
        }

        super.rollback(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#cleanup(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void cleanup ( Context context ) throws ExecutionException {

        try {
            if ( this.backupFile != null ) {
                Files.deleteIfExists(this.backupFile.unwrap());
                Path effectiveFile = this.getEffectiveFile(context);
                try {
                    context.getConfig().getService(FileHashTracker.class).removeHash(effectiveFile);
                }
                catch ( NoSuchServiceException e ) {
                    context.getOutput().error("Failed to file hash for " + effectiveFile, e); //$NON-NLS-1$
                }
            }
        }
        catch ( IOException e ) {
            context.getOutput().error("Failed to remove backup file " + this.backupFile, e); //$NON-NLS-1$
        }
        super.cleanup(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public RemoveConfigurator createConfigurator () {
        return new RemoveConfigurator(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return String.format(
            "Remove file %s", //$NON-NLS-1$
            this.getFile() != null ? this.getFile() : "result"); //$NON-NLS-1$
    }
}
