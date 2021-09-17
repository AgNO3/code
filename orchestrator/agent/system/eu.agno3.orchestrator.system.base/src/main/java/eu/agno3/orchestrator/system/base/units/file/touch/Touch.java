/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.touch;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Named;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;
import eu.agno3.orchestrator.system.base.units.file.TemporaryFileResult;


/**
 * @author mbechler
 * 
 */
public class Touch extends AbstractFileExecutionUnit<Touch, TouchConfigurator> implements Named {

    /**
     * 
     */
    private static final long serialVersionUID = -2640622976317001481L;
    private DateTime createTime = null;
    private DateTime modifyTime = null;
    private DateTime accessTime = null;

    private boolean touchedTemporary = false;
    private boolean created;


    /**
     * @param createTime
     *            the createTime to set
     */
    void setCreateTime ( DateTime createTime ) {
        this.createTime = createTime;
    }


    /**
     * @param modifyTime
     *            the modifyTime to set
     */
    void setModifyTime ( DateTime modifyTime ) {
        this.modifyTime = modifyTime;
    }


    /**
     * @param accessTime
     *            the accessTime to set
     */
    void setAccessTime ( DateTime accessTime ) {
        this.accessTime = accessTime;
    }


    /**
     * @return the createTime
     */
    public FileTime getCreateTime () {
        if ( this.createTime == null ) {
            return null;
        }
        return FileTime.fromMillis(this.createTime.getMillis());
    }


    /**
     * @return the modifyTime
     */
    public FileTime getModifyTime () {
        if ( this.modifyTime == null ) {
            return null;
        }
        return FileTime.fromMillis(this.modifyTime.getMillis());
    }


    /**
     * @return the accessTime
     */
    public FileTime getAccessTime () {
        if ( this.accessTime == null ) {
            return null;
        }
        return FileTime.fromMillis(this.accessTime.getMillis());
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
            this.touchedTemporary = true;
            this.doTouch(context, res.getPath());
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
        Path p = this.getEffectiveFile(context);

        if ( !this.touchedTemporary ) {
            doTouch(context, p);
        }
        return new FileResult(p);
    }


    /**
     * @param p
     * @throws ExecutionException
     */
    private void doTouch ( Context ctx, Path p ) throws ExecutionException {
        ctx.getOutput().info(String.format(
            "Touching file %s (mode=%s)", //$NON-NLS-1$
            p,
            this.getPerms() != null ? PosixFilePermissions.toString(this.getPerms()) : null));

        if ( !Files.exists(this.getEffectiveFile(ctx), LinkOption.NOFOLLOW_LINKS) ) {
            try {
                doCreateTargetDir(ctx);
                Files.write(p, new byte[] {}, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to create file", e); //$NON-NLS-1$
            }
            this.created = true;
        }

        PosixFileAttributeView attrs;
        try {
            attrs = this.ensureFileMode(ctx, p);
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to set file mode:", e); //$NON-NLS-1$
        }

        try {
            attrs.setTimes(this.getModifyTime(), this.getAccessTime(), this.getCreateTime());
        }
        catch ( IOException e ) {
            throw new ExecutionException("Failed to set file times:", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the created
     */
    public boolean isCreated () {
        return this.created;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public TouchConfigurator createConfigurator () {
        return new TouchConfigurator(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return String.format(
            "Touch file %s", //$NON-NLS-1$
            this.getFile() != null ? this.getFile() : "result"); //$NON-NLS-1$
    }
}
