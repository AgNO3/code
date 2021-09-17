/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;


/**
 * @author mbechler
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
public abstract class AbstractFileSourceDestConfigurator <TExecutionUnit extends AbstractFileSourceDestExecutionUnit<TExecutionUnit, TConfigurator>, TConfigurator extends AbstractFileSourceDestConfigurator<TExecutionUnit, TConfigurator>>
        extends AbstractConfigurator<FileResult, TExecutionUnit, TConfigurator> {

    /**
     * @param unit
     */
    protected AbstractFileSourceDestConfigurator ( TExecutionUnit unit ) {
        super(unit);
    }


    /**
     * @param f
     *            a previous file result
     * @return this configurator
     */
    public TConfigurator from ( ResultReference<FileResult> f ) {
        this.getExecutionUnit().setFromResult(f);
        return this.self();
    }


    /**
     * @param f
     *            source file
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public TConfigurator from ( Path f ) throws InvalidUnitConfigurationException {

        if ( !Files.isRegularFile(f) ) {
            throw new InvalidUnitConfigurationException("Source file is not a regular file"); //$NON-NLS-1$
        }

        if ( !Files.isReadable(f) ) {
            throw new InvalidUnitConfigurationException("Source file is not readable"); //$NON-NLS-1$
        }

        this.getExecutionUnit().setFrom(f.normalize());
        return this.self();
    }


    /**
     * @param f
     *            source file
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public TConfigurator from ( File f ) throws InvalidUnitConfigurationException {
        this.from(f.toPath());
        return this.self();
    }


    /**
     * @param f
     *            target file
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public TConfigurator to ( Path f ) throws InvalidUnitConfigurationException {
        if ( Files.isDirectory(f) ) {
            throw new InvalidUnitConfigurationException("Target file is a directory"); //$NON-NLS-1$
        }

        if ( !Files.isDirectory(f.getParent()) || !Files.isWritable(f.getParent()) ) {
            throw new InvalidUnitConfigurationException("Target parent directory is not writable"); //$NON-NLS-1$
        }

        this.getExecutionUnit().setTo(f.normalize());
        return this.self();
    }


    /**
     * @param f
     *            target file
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public TConfigurator to ( File f ) throws InvalidUnitConfigurationException {
        this.to(f.toPath());
        return this.self();
    }


    /**
     * @param dir
     *            target directory
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public TConfigurator toDir ( Path dir ) throws InvalidUnitConfigurationException {
        if ( !Files.isDirectory(dir) ) {
            throw new InvalidUnitConfigurationException("Target directory is not a directory"); //$NON-NLS-1$
        }

        if ( !Files.isWritable(dir) ) {
            throw new InvalidUnitConfigurationException("Target directory is not writable"); //$NON-NLS-1$
        }

        this.getExecutionUnit().setToDir(dir);
        return this.self();
    }


    /**
     * @param dir
     *            target directory
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public TConfigurator toDir ( File dir ) throws InvalidUnitConfigurationException {
        this.toDir(dir.toPath());
        return this.self();
    }


    /**
     * Enable overwriting of target file
     * 
     * @return this configurator
     */
    public TConfigurator replaceExisting () {
        this.getExecutionUnit().setOverwriteTarget(true);
        return this.self();
    }

}