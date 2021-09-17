/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.symlink;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.orchestrator.system.file.util.SerializablePath;


/**
 * @author mbechler
 * 
 */
public class Symlink extends AbstractFileExecutionUnit<Symlink, SymlinkConfigurator> implements Named {

    /**
     * 
     */
    private static final long serialVersionUID = -2640622976317001481L;

    private SerializablePath source;


    /**
     * @return the source
     */
    public Path getSource () {
        if ( this.source == null ) {
            return null;
        }
        return this.source.unwrap();
    }


    /**
     * @param source
     *            the source to set
     */
    void setSource ( Path source ) {
        this.source = SerializablePath.wrap(source);
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

        if ( this.getSource() == null ) {
            throw new RequiredParameterMissingException("Missing source"); //$NON-NLS-1$
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

        return new FileResult(this.getFile());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult execute ( Context context ) throws ExecutionException {
        Path p = this.getEffectiveFile(context);
        try {
            Path sourcePath = PrefixUtil.resolvePrefix(context, getSource());
            if ( !Files.exists(sourcePath) ) {
                throw new ExecutionException("Source does not exist " + sourcePath); //$NON-NLS-1$
            }
            Files.createSymbolicLink(sourcePath, getEffectiveFile(context));
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to create symlink", e); //$NON-NLS-1$
        }

        return new FileResult(p);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public SymlinkConfigurator createConfigurator () {
        return new SymlinkConfigurator(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return String.format(
            "Symlink file %s to %s", //$NON-NLS-1$
            this.getSource() != null ? this.getSource() : null,
            this.getFile() != null ? this.getFile() : "result"); //$NON-NLS-1$
    }
}
