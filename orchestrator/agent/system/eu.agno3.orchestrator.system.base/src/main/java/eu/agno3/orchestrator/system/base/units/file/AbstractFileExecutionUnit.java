/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.SerializablePath;


/**
 * @author mbechler
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
@SuppressWarnings ( "serial" )
public abstract class AbstractFileExecutionUnit <TExecutionUnit extends AbstractFileExecutionUnit<TExecutionUnit, TConfigurator>, TConfigurator extends AbstractFileConfigurator<TExecutionUnit, TConfigurator>>
        extends AbstractExecutionUnit<FileResult, TExecutionUnit, TConfigurator> {

    private static final Logger log = Logger.getLogger(AbstractFileExecutionUnit.class);

    private SerializablePath file;
    private ResultReference<FileResult> fileRef;
    private Set<PosixFilePermission> perms;
    private transient UserPrincipal owner;
    private String ownerLazy;

    private transient GroupPrincipal group;
    private String groupLazy;

    private boolean followSymlinks = false;
    private boolean createTargetDir;
    private Set<PosixFilePermission> targetDirPerms = FileSecurityUtils.getOwnerOnlyDirPermissions();
    private transient UserPrincipal targetDirOwner;
    private String targetDirOwnerLazy;
    private transient GroupPrincipal targetDirGroup;
    private String targetDirGroupLazy;

    private boolean disablePrefix;


    /**
     * @param file
     *            the file to set
     */
    void setFile ( Path file ) {
        this.file = SerializablePath.wrap(file);
    }


    /**
     * @param fileRef
     *            the fileRef to set
     */
    void setFileRef ( ResultReference<FileResult> fileRef ) {
        this.fileRef = fileRef;
    }


    /**
     * @param perms
     *            the perms to set
     */
    void setPerms ( Set<PosixFilePermission> perms ) {
        this.perms = perms;
    }


    /**
     * @param owner
     *            the owner to set
     */
    void setOwner ( UserPrincipal owner ) {
        this.owner = owner;
    }


    /**
     * @param ownerLazy
     *            the ownerLazy to set
     */
    void setOwnerLazy ( String ownerLazy ) {
        this.ownerLazy = ownerLazy;
    }


    /**
     * @param group
     *            the group to set
     */
    void setGroup ( GroupPrincipal group ) {
        this.group = group;
    }


    /**
     * @param groupLazy
     *            the groupLazy to set
     */
    void setGroupLazy ( String groupLazy ) {
        this.groupLazy = groupLazy;
    }


    /**
     * 
     * @param follow
     */
    void setFollowSymlinks ( boolean follow ) {
        this.followSymlinks = follow;
    }


    /**
     * 
     * @param createTargetDir
     */
    void setCreateTargetDir ( boolean createTargetDir ) {
        this.createTargetDir = createTargetDir;
    }


    void setTargetDirPerms ( Set<PosixFilePermission> perms ) {
        this.targetDirPerms = perms;
    }


    /**
     * @param targetDirGroup
     *            the targetDirGroup to set
     */
    void setTargetDirGroup ( GroupPrincipal targetDirGroup ) {
        this.targetDirGroup = targetDirGroup;
    }


    /**
     * @param targetDirOwner
     *            the targetDirOwner to set
     */
    void setTargetDirOwner ( UserPrincipal targetDirOwner ) {
        this.targetDirOwner = targetDirOwner;
    }


    /**
     * @param targetDirGroup
     *            the targetDirGroup to set
     */
    void setTargetDirGroupLazy ( String targetDirGroup ) {
        this.targetDirGroupLazy = targetDirGroup;
    }


    /**
     * @param targetDirOwner
     *            the targetDirOwner to set
     */
    void setTargetDirOwnerLazy ( String targetDirOwner ) {
        this.targetDirOwnerLazy = targetDirOwner;
    }


    /**
     * @return the file
     */
    public Path getFile () {
        if ( this.file == null ) {
            return null;
        }
        return this.file.unwrap();
    }


    /**
     * @return the fileRef
     */
    public ResultReference<FileResult> getFileRef () {
        return this.fileRef;
    }


    /**
     * @return the perms
     */
    public Set<PosixFilePermission> getPerms () {
        return this.perms;
    }


    /**
     * @return the owner
     */
    public UserPrincipal getOwner () {
        return this.owner;
    }


    /**
     * @return the ownerLazy
     */
    public String getOwnerLazy () {
        return this.ownerLazy;
    }


    /**
     * @return the group
     */
    public GroupPrincipal getGroup () {
        return this.group;
    }


    /**
     * @return the groupLazy
     */
    public String getGroupLazy () {
        return this.groupLazy;
    }


    /**
     * 
     * @return whether symlinks are followed
     */
    public boolean isFollowSymlinks () {
        return this.followSymlinks;
    }


    /**
     * 
     * @return whether to create the target dir
     */
    public boolean isCreateTargetDir () {
        return this.createTargetDir;
    }


    /**
     * @return the disablePrefix
     */
    public boolean isDisablePrefix () {
        return this.disablePrefix;
    }


    /**
     * @param disablePrefix
     *            the disablePrefix to set
     */
    void setDisablePrefix ( boolean disablePrefix ) {
        this.disablePrefix = disablePrefix;
    }


    /**
     * @return the targetDirPerms
     */
    public Set<PosixFilePermission> getTargetDirPerms () {
        return this.targetDirPerms;
    }


    /**
     * @return the targetDirGroup
     * @throws IOException
     */
    public GroupPrincipal getTargetDirGroup () throws IOException {
        if ( this.targetDirGroup == null && this.targetDirGroupLazy != null ) {
            if ( FileSecurityUtils.isRunningAsRoot() ) {
                return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(this.targetDirGroupLazy);
            }
        }
        return this.targetDirGroup;
    }


    /**
     * @return the targetDirGroup
     */
    public String getTargetDirGroupLazy () {
        return this.targetDirGroupLazy;
    }


    /**
     * @return the targetDirOwner
     * @throws IOException
     */
    public UserPrincipal getTargetDirOwner () throws IOException {
        if ( this.targetDirOwner == null && this.targetDirOwnerLazy != null ) {
            if ( FileSecurityUtils.isRunningAsRoot() ) {
                return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(this.targetDirOwnerLazy);
            }
        }
        return this.targetDirOwner;
    }


    /**
     * @return the targetDirOwner
     */
    public String getTargetDirOwnerLazy () {
        return this.targetDirOwnerLazy;
    }


    /**
     * Set the configured file ownership and permissions
     * 
     * @param effectivePath
     * @return the file attribute view
     * @throws IOException
     */
    protected PosixFileAttributeView ensureFileMode ( Context ctx, Path effectivePath ) throws IOException {
        PosixFileAttributeView posixAttrs = Files.getFileAttributeView(effectivePath, PosixFileAttributeView.class, this.getLinkOption());

        if ( posixAttrs == null ) {
            throw new IOException("Not a POSIX filesystem"); //$NON-NLS-1$
        }

        if ( !ctx.getConfig().isDryRun() && this.getOwner() != null ) {
            posixAttrs.setOwner(this.getOwner());
        }

        if ( !ctx.getConfig().isDryRun() && this.getGroup() != null ) {
            posixAttrs.setGroup(this.getGroup());
        }

        if ( !ctx.getConfig().isDryRun() && this.getPerms() != null ) {
            posixAttrs.setPermissions(this.getPerms());
        }

        return posixAttrs;
    }


    protected PosixFileAttributeView ensureFileModeLazy ( Context ctx, Path effectivePath ) throws IOException {
        PosixFileAttributeView posixAttrs = Files.getFileAttributeView(effectivePath, PosixFileAttributeView.class, this.getLinkOption());

        if ( posixAttrs == null ) {
            throw new IOException("Not a POSIX filesystem"); //$NON-NLS-1$
        }

        String lazyOwner = this.getOwnerLazy();
        if ( !ctx.getConfig().isDryRun() && lazyOwner != null ) {
            try {
                posixAttrs.setOwner(FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(lazyOwner));
            }
            catch ( UserPrincipalNotFoundException e ) {
                ctx.getOutput().error("Failed to find user " + lazyOwner, e); //$NON-NLS-1$
                throw e;
            }
        }

        String lazyGroup = this.getGroupLazy();
        if ( !ctx.getConfig().isDryRun() && lazyGroup != null ) {
            try {
                posixAttrs.setGroup(FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(lazyGroup));
            }
            catch ( UserPrincipalNotFoundException e ) {
                ctx.getOutput().error("Failed to find group " + lazyGroup, e); //$NON-NLS-1$
                throw e;
            }
        }

        return posixAttrs;
    }


    /**
     * @return
     */
    protected LinkOption[] getLinkOption () {
        if ( !this.isFollowSymlinks() ) {
            return new LinkOption[] {
                LinkOption.NOFOLLOW_LINKS
            };
        }
        return new LinkOption[] {};
    }


    /**
     * @param ctx
     * @throws ResultReferenceException
     */
    protected Path getEffectiveFile ( Context ctx ) throws ResultReferenceException {
        if ( this.getFileRef() != null ) {
            return ctx.fetchResult(this.getFileRef()).getPath();
        }

        if ( this.isDisablePrefix() ) {
            return this.getFile();
        }
        return PrefixUtil.resolvePrefix(ctx, this.getFile());
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#suspend(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void suspend ( Context context ) throws ExecutionException {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#resume(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void resume ( Context context ) throws ExecutionException {
        // ignore
    }


    protected void doCreateTargetDir ( Context context ) throws ExecutionException {
        boolean createTarget = context.getConfig().isAlwaysCreateTargets() || this.isCreateTargetDir();
        if ( createTarget && !doesTargetDirExist(context) ) {
            try {
                log.debug(String.format(
                    "Target %s with %s (own: %s group: %s)", //$NON-NLS-1$
                    this.getEffectiveFile(context),
                    this.getTargetDirPerms(),
                    this.getTargetDirOwner(),
                    this.getTargetDirGroup()));
                context.getOutput().info("Creating target directory"); //$NON-NLS-1$
                FileSecurityUtils.createDirectories(
                    this.getEffectiveFile(context).getParent(),
                    this.getTargetDirOwner(),
                    this.getTargetDirGroup(),
                    this.getTargetDirPerms());
            }
            catch ( IOException e ) {
                throw new ExecutionException("Failed to create target directory:", e); //$NON-NLS-1$
            }
        }
    }


    private boolean doesTargetDirExist ( Context context ) throws ResultReferenceException {
        return Files.exists(this.getEffectiveFile(context).getParent());
    }


    private void writeObject ( ObjectOutputStream os ) throws IOException {
        if ( this.owner != null ) {
            this.ownerLazy = this.owner.getName();
        }
        if ( this.group != null ) {
            this.groupLazy = this.group.getName();
        }
        if ( this.targetDirOwner != null ) {
            this.targetDirOwnerLazy = this.targetDirOwner.getName();
        }
        if ( this.targetDirGroup != null ) {
            this.targetDirGroupLazy = this.targetDirGroup.getName();
        }
        os.defaultWriteObject();
    }

}
