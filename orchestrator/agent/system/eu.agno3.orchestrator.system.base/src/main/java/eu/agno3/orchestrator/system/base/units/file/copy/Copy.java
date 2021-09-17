/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.copy;


import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;
import eu.agno3.orchestrator.system.base.units.file.TemporaryFileResult;
import eu.agno3.orchestrator.system.file.util.FileAttributeUtils;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.orchestrator.system.file.util.SerializablePath;


/**
 * @author mbechler
 * 
 */
public class Copy extends AbstractFileSourceDestExecutionUnit<Copy, CopyConfigurator> implements Named {

    /**
     * 
     */
    private static final long serialVersionUID = -6201284870182767781L;
    private static final int DEFAULT_BUF_SIZE = 4096;
    private boolean followSymlinks = false;
    private boolean copyAttributes = true;
    private SerializablePath targetTempFile;
    private int bufSize;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit#createConfigurator()
     */
    @Override
    public CopyConfigurator createConfigurator () {
        return new CopyConfigurator(this);
    }


    /**
     * @param followSymlinks
     *            the followSymlinks to set
     */
    void setFollowSymlinks ( boolean followSymlinks ) {
        this.followSymlinks = followSymlinks;
    }


    /**
     * @param copyAttributes
     *            the copyAttributes to set
     */
    void setCopyAttributes ( boolean copyAttributes ) {
        this.copyAttributes = copyAttributes;
    }


    void setBufSize ( int bufSize ) {
        this.bufSize = bufSize;
    }


    /**
     * 
     * @return the copy buffer size
     */
    protected int getBufSize () {
        if ( this.bufSize <= 0 ) {
            return DEFAULT_BUF_SIZE;
        }
        return this.bufSize;
    }


    /**
     * @return the followSymlinks
     */
    protected boolean isFollowSymlinks () {
        return this.followSymlinks;
    }


    /**
     * @return the copyAttributes
     */
    protected boolean isCopyAttributes () {
        return this.copyAttributes;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit#buildCopyOptions(java.util.Set)
     */
    @Override
    protected void buildCopyOptions ( Set<CopyOption> options ) {
        super.buildCopyOptions(options);

        if ( !isFollowSymlinks() ) {
            options.add(LinkOption.NOFOLLOW_LINKS);
        }

        if ( isCopyAttributes() ) {
            options.add(StandardCopyOption.COPY_ATTRIBUTES);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InvalidParameterException
     * 
     * @see eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws InvalidUnitConfigurationException {
        super.validate(context);

        try {
            if ( this.getFrom() != null ) {
                FileSecurityUtils.assertSecureLocation(this.getFrom());
            }
        }
        catch ( IOException e ) {
            throw new InvalidParameterException("Source location is insecure", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult prepare ( Context context ) throws ExecutionException {

        Path source = this.getEffectiveSource(context);

        if ( !this.isOverwriteTarget() && Files.exists(this.getEffectiveTarget(context), LinkOption.NOFOLLOW_LINKS) ) {
            throw new ExecutionException("Target file already exists"); //$NON-NLS-1$
        }

        try {
            this.targetTempFile = SerializablePath.wrap(FileTemporaryUtils.createRelatedTemporaryFile(this.getEffectiveTarget(context)));
            if ( context.getOutput().isDebugEnabled() ) {
                context.getOutput().debug(String.format("Copying file %s to temporary location %s", source, this.targetTempFile)); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to create temporary file:", e); //$NON-NLS-1$
        }

        FileUtil.copyFileContents(source, this.targetTempFile.unwrap(), new ProgressEventBridge(context), this.getBufSize());

        if ( this.isCopyAttributes() ) {
            try {
                FileAttributeUtils.copyAttributes(source, this.targetTempFile.unwrap());
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to copy attributes:", e); //$NON-NLS-1$
            }
        }

        return new TemporaryFileResult(this.targetTempFile.unwrap(), this.getEffectiveTarget(context));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult execute ( Context context ) throws ExecutionException {
        try {
            Path effectiveTarget = this.getEffectiveTarget(context);

            if ( this.targetTempFile == null ) {
                throw new ExecutionException("Target tempfile is not set"); //$NON-NLS-1$
            }

            if ( context.getOutput().isDebugEnabled() ) {
                context.getOutput().debug(String.format("Moving temporary file %s to target location %s", this.targetTempFile, effectiveTarget)); //$NON-NLS-1$
            }
            FileUtil.safeMove(this.targetTempFile.unwrap(), effectiveTarget, this.isOverwriteTarget());
            return new FileResult(effectiveTarget);
        }
        catch ( IOException e ) {
            throw new ExecutionException("Could not copy file:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#cleanup(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void cleanup ( Context context ) throws ExecutionException {

        if ( this.targetTempFile != null ) {
            try {
                Files.deleteIfExists(this.targetTempFile.unwrap());
            }
            catch ( IOException e ) {
                context.getOutput().error("Failed to remove temporary file " + this.targetTempFile, e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return String.format("Copy file %s to %s", this.getFrom(), this.getToDir() != null ? this.getToDir() : this.getTo()); //$NON-NLS-1$
    }
}
