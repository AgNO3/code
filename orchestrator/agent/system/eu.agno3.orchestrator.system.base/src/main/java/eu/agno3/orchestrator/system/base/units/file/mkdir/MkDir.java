/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.mkdir;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 * 
 */
public class MkDir extends AbstractFileExecutionUnit<MkDir, MkDirConfigurator> implements Named {

    /**
     * 
     */
    private static final long serialVersionUID = -2640622976317001481L;


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
        if ( !Files.exists(p) ) {
            try {
                super.execute(context);
                Set<PosixFilePermission> perms = getPerms();
                if ( perms == null ) {
                    perms = FileSecurityUtils.getOwnerOnlyDirPermissions();
                }
                p = Files.createDirectory(p, PosixFilePermissions.asFileAttribute(perms));
                ensureFileMode(context, p);
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to create directory " + p, e); //$NON-NLS-1$
            }
        }

        try {
            PosixFileAttributeView attrs = ensureFileModeLazy(context, p);
            if ( this.getPerms() != null ) {
                attrs.setPermissions(this.getPerms());
            }
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to set permissions on " + p, e); //$NON-NLS-1$
        }

        return new FileResult(p);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public MkDirConfigurator createConfigurator () {
        return new MkDirConfigurator(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return String.format(
            "Mkdir %s", //$NON-NLS-1$
            this.getFile() != null ? this.getFile() : "result"); //$NON-NLS-1$
    }
}
