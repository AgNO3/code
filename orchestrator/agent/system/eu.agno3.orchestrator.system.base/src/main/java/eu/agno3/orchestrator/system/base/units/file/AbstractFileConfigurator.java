/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;


/**
 * @author mbechler
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
public abstract class AbstractFileConfigurator <TExecutionUnit extends AbstractFileExecutionUnit<TExecutionUnit, TConfigurator>, TConfigurator extends AbstractFileConfigurator<TExecutionUnit, TConfigurator>>
        extends AbstractConfigurator<FileResult, TExecutionUnit, TConfigurator> {

    /**
     * @param unit
     */
    protected AbstractFileConfigurator ( TExecutionUnit unit ) {
        super(unit);
    }


    /**
     * @param f
     * @return this configurator
     */
    public TConfigurator file ( Path f ) {
        this.getExecutionUnit().setFile(f);
        return this.self();
    }


    /**
     * @param f
     * @return this configurator
     */
    public TConfigurator file ( File f ) {
        return this.file(f.toPath());
    }


    /**
     * @param path
     * @return this configurator
     */
    public TConfigurator file ( String path ) {
        return this.file(Paths.get(path));
    }


    /**
     * 
     * @param ref
     * @return this configurator
     */
    public TConfigurator file ( ResultReference<FileResult> ref ) {
        this.getExecutionUnit().setFileRef(ref);
        return this.self();
    }


    /**
     * @param perms
     * @return this configurator
     */
    public TConfigurator perms ( Set<PosixFilePermission> perms ) {
        this.getExecutionUnit().setPerms(perms);
        return this.self();
    }


    /**
     * @param permSpec
     *            permission string
     * @return this configurator
     * @see PosixFilePermissions#fromString(java.lang.String)
     * @throws InvalidParameterException
     */
    public TConfigurator perms ( String permSpec ) throws InvalidParameterException {
        try {
            return this.perms(PosixFilePermissions.fromString(permSpec));
        }
        catch ( IllegalArgumentException e ) {
            throw new InvalidParameterException("Illegal permission specification:", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param owner
     * @return this configurator
     */
    public TConfigurator owner ( UserPrincipal owner ) {
        this.getExecutionUnit().setOwner(owner);
        return this.self();
    }


    /**
     * @param owner
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    @SuppressWarnings ( "resource" )
    public TConfigurator owner ( String owner ) throws InvalidUnitConfigurationException {
        if ( this.getExecutionUnit().getFile() == null ) {
            throw new InvalidUnitConfigurationException("File needs to set before calling owner(java.lang.String)"); //$NON-NLS-1$
        }

        try {
            FileSystem fs = this.getExecutionUnit().getFile().getFileSystem();
            UserPrincipalLookupService lookup = fs.getUserPrincipalLookupService();
            return this.owner(lookup.lookupPrincipalByName(owner));
        }
        catch ( IOException e ) {
            throw new InvalidParameterException("User does not exist " + owner, e); //$NON-NLS-1$
        }
    }


    /**
     * @param group
     * @return this configurator
     */
    public TConfigurator group ( GroupPrincipal group ) {
        this.getExecutionUnit().setGroup(group);
        return this.self();
    }


    /**
     * @param group
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    @SuppressWarnings ( "resource" )
    public TConfigurator group ( String group ) throws InvalidUnitConfigurationException {
        if ( this.getExecutionUnit().getFile() == null ) {
            throw new InvalidUnitConfigurationException("File needs to set before calling group(java.lang.String)"); //$NON-NLS-1$
        }

        try {
            FileSystem fs = this.getExecutionUnit().getFile().getFileSystem();
            UserPrincipalLookupService lookup = fs.getUserPrincipalLookupService();
            return this.group(lookup.lookupPrincipalByGroupName(group));
        }
        catch ( IOException e ) {
            throw new InvalidParameterException("Group does not exist " + group, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param user
     * @return this configurator
     */
    public TConfigurator ownerLazy ( String user ) {
        this.getExecutionUnit().setOwnerLazy(user);
        return this.self();
    }


    /**
     * 
     * @param group
     * @return this configurator
     */
    public TConfigurator groupLazy ( String group ) {
        this.getExecutionUnit().setGroupLazy(group);
        return this.self();
    }


    /**
     * @return this configurator
     */
    public TConfigurator followSymlinks () {
        this.getExecutionUnit().setFollowSymlinks(true);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public TConfigurator createTargetDir () {
        this.getExecutionUnit().setCreateTargetDir(true);
        return this.self();
    }


    /**
     * 
     * @param dirPerms
     * @param owner
     * @param group
     * @return this configurator
     */
    public TConfigurator createTargetDir ( Set<PosixFilePermission> dirPerms, String owner, String group ) {
        this.getExecutionUnit().setCreateTargetDir(true);
        this.getExecutionUnit().setTargetDirPerms(dirPerms);
        this.getExecutionUnit().setTargetDirOwnerLazy(owner);
        this.getExecutionUnit().setTargetDirGroupLazy(group);
        return this.self();
    }


    /**
     * 
     * @param dirPerms
     * @param owner
     * @param group
     * @return this configurator
     */
    public TConfigurator createTargetDir ( Set<PosixFilePermission> dirPerms, UserPrincipal owner, GroupPrincipal group ) {
        this.getExecutionUnit().setCreateTargetDir(true);
        this.getExecutionUnit().setTargetDirPerms(dirPerms);
        this.getExecutionUnit().setTargetDirOwner(owner);
        this.getExecutionUnit().setTargetDirGroup(group);
        return this.self();
    }


    /**
     * @return this configurator
     */
    public TConfigurator noPrefix () {
        this.getExecutionUnit().setDisablePrefix(true);
        return this.self();
    }
}
