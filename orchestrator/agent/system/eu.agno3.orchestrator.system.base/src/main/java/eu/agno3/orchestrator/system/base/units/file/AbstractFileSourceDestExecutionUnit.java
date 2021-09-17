/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file;


import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;


/**
 * @author mbechler
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
@SuppressWarnings ( "serial" )
public abstract class AbstractFileSourceDestExecutionUnit <TExecutionUnit extends AbstractFileSourceDestExecutionUnit<TExecutionUnit, TConfigurator>, TConfigurator extends AbstractFileSourceDestConfigurator<TExecutionUnit, TConfigurator>>
        extends AbstractExecutionUnit<FileResult, TExecutionUnit, TConfigurator> {

    private Path from;
    private ResultReference<FileResult> fromResult;
    private Path to;
    private Path toDir;
    private boolean overwriteTarget = false;
    private boolean createTargetDir;
    private Set<PosixFilePermission> targetDirPerms = FileSecurityUtils.getOwnerOnlyDirPermissions();


    /**
     * @param from
     *            the from to set
     */
    void setFrom ( Path from ) {
        this.from = from;
    }


    void setFromResult ( ResultReference<FileResult> f ) {
        this.fromResult = f;
    }


    /**
     * @param to
     *            the to to set
     */
    void setTo ( Path to ) {
        this.to = to;
    }


    /**
     * @param toDir
     *            the toDir to set
     */
    void setToDir ( Path toDir ) {
        this.toDir = toDir;
    }


    /**
     * @param overwriteTarget
     *            the overwriteTarget to set
     */
    void setOverwriteTarget ( boolean overwriteTarget ) {
        this.overwriteTarget = overwriteTarget;
    }


    void setCreateTargetDir ( boolean createTargetDir ) {
        this.createTargetDir = createTargetDir;
    }


    void setTargetDirPerms ( Set<PosixFilePermission> perms ) {
        this.targetDirPerms = perms;
    }


    /**
     * @return the from
     */
    public Path getFrom () {
        return this.from;
    }


    protected ResultReference<FileResult> getFromResult () {
        return this.fromResult;
    }


    /**
     * @return the to
     */
    public Path getTo () {
        return this.to;
    }


    /**
     * @return the toDir
     */
    public Path getToDir () {
        return this.toDir;
    }


    /**
     * 
     * @param ctx
     * @return the effective source
     * @throws ResultReferenceException
     */
    public Path getEffectiveSource ( Context ctx ) throws ResultReferenceException {
        if ( this.getFromResult() != null ) {
            return ctx.fetchResult(this.getFromResult()).getPath();
        }
        return PrefixUtil.resolvePrefix(ctx, this.getFrom());
    }


    /**
     * 
     * @param ctx
     * @return the effective target
     */
    public Path getEffectiveTarget ( Context ctx ) {
        if ( this.getToDir() != null ) {
            return this.getToDir().resolve(this.getFrom().getFileName());
        }
        return PrefixUtil.resolvePrefix(ctx, this.getTo());
    }


    /**
     * @return the overwriteTarget
     */
    protected boolean isOverwriteTarget () {
        return this.overwriteTarget;
    }


    /**
     * @return
     */
    protected boolean isCreateTargetDir () {
        return this.createTargetDir;
    }


    /**
     * @return the targetDirPerms
     */
    public Set<PosixFilePermission> getTargetDirPerms () {
        return this.targetDirPerms;
    }


    protected final CopyOption[] getCopyOptions () {
        Set<CopyOption> options = new HashSet<>();
        buildCopyOptions(options);
        return options.toArray(new CopyOption[] {});
    }


    /**
     * @param options
     */
    protected void buildCopyOptions ( Set<CopyOption> options ) {
        if ( isOverwriteTarget() ) {
            options.add(StandardCopyOption.REPLACE_EXISTING);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public abstract TConfigurator createConfigurator ();


    /**
     * {@inheritDoc}
     * 
     * @throws InvalidUnitConfigurationException
     * 
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws InvalidUnitConfigurationException {

        validateArguments(context);

        if ( !doesSourceExist() ) {
            throw new InvalidParameterException("Source does not exist"); //$NON-NLS-1$
        }

        if ( !isOverwriteTarget() && doesTargetExist(context) ) {
            throw new InvalidParameterException("Target exists and overwriteTarget is not set"); //$NON-NLS-1$
        }

        if ( !context.getConfig().isAlwaysCreateTargets() && ! ( doesTargetDirExist(context) || this.isCreateTargetDir() ) ) {
            throw new InvalidParameterException("Target directory does not exist and is not automatically created"); //$NON-NLS-1$
        }
    }


    protected void validateArguments ( Context ctx ) throws RequiredParameterMissingException, InvalidParameterException {
        if ( isSourceMissing() ) {
            throw new RequiredParameterMissingException("Missing source file"); //$NON-NLS-1$
        }

        if ( isBothRefAndFileSet() ) {
            throw new InvalidParameterException("Both source file and reference set"); //$NON-NLS-1$
        }

        if ( isTargetMissing(ctx) ) {
            throw new RequiredParameterMissingException("Missing target declaration"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult execute ( Context context ) throws ExecutionException {
        doCreateTargetDir(context);
        return null;
    }


    protected void doCreateTargetDir ( Context context ) throws ExecutionException {
        if ( !doesTargetDirExist(context) && ( context.getConfig().isAlwaysCreateTargets() || this.isCreateTargetDir() ) ) {
            try {
                Files.createDirectories(this.getEffectiveTarget(context).getParent(), PosixFilePermissions.asFileAttribute(this.getTargetDirPerms()));
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to create target directory:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @return
     */
    private boolean isBothRefAndFileSet () {
        return this.getFrom() != null && this.getFromResult() != null;
    }


    /**
     * @return
     */
    protected boolean doesTargetExist ( Context ctx ) {
        return Files.exists(this.getEffectiveTarget(ctx), LinkOption.NOFOLLOW_LINKS);
    }


    /**
     * @param ctx
     * @return
     */
    private boolean doesTargetDirExist ( Context ctx ) {
        return Files.exists(this.getEffectiveTarget(ctx).getParent());
    }


    /**
     * @return whether the source file exists ( always true if this is a reference )
     */
    protected boolean doesSourceExist () {
        return this.getFrom() == null || this.getFrom() != null && Files.exists(this.getFrom());
    }


    /**
     * @return
     */
    private boolean isTargetMissing ( Context ctx ) {
        return this.getEffectiveTarget(ctx) == null;
    }


    /**
     * @return
     */
    private boolean isSourceMissing () {
        return this.getFrom() == null && this.getFromResult() == null;
    }

}
