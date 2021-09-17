/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.touch;


import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.system.base.units.file.AbstractFileConfigurator;


/**
 * @author mbechler
 * 
 */
public class TouchConfigurator extends AbstractFileConfigurator<Touch, TouchConfigurator> {

    /**
     * @param unit
     */
    protected TouchConfigurator ( Touch unit ) {
        super(unit);
    }


    /**
     * 
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setCreateTime ( FileTime date ) {
        this.getExecutionUnit().setCreateTime(new DateTime(date.toMillis()));
        return this.self();
    }


    /**
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setCreateTime ( Date date ) {
        return this.setCreateTime(FileTime.fromMillis(dateToMillis(date)));
    }


    /**
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setCreateTime ( DateTime date ) {
        return this.setCreateTime(FileTime.fromMillis(dateTimeToMillis(date)));
    }


    /**
     * Set the create time to the current time
     * 
     * @return this configurator
     */
    public TouchConfigurator setCreateTime () {
        return this.setCreateTime(DateTime.now());
    }


    /**
     * 
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setModifyTime ( FileTime date ) {
        this.getExecutionUnit().setModifyTime(new DateTime(date.toMillis()));
        return this.self();
    }


    /**
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setModifyTime ( Date date ) {
        return this.setModifyTime(FileTime.fromMillis(dateToMillis(date)));
    }


    /**
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setModifyTime ( DateTime date ) {
        return this.setModifyTime(FileTime.fromMillis(dateTimeToMillis(date)));
    }


    /**
     * Set the modify time to the current time
     * 
     * @return this configurator
     */
    public TouchConfigurator setModifyTime () {
        return this.setModifyTime(DateTime.now());
    }


    /**
     * 
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setAccessTime ( FileTime date ) {
        this.getExecutionUnit().setAccessTime(new DateTime(date.toMillis()));
        return this.self();
    }


    /**
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setAccessTime ( Date date ) {
        return this.setAccessTime(FileTime.fromMillis(dateToMillis(date)));
    }


    /**
     * @param date
     * @return this configurator
     */
    public TouchConfigurator setAccessTime ( DateTime date ) {
        return this.setAccessTime(FileTime.fromMillis(dateTimeToMillis(date)));
    }


    /**
     * Set the access time to the current time
     * 
     * @return this configurator
     */
    public TouchConfigurator setAccessTime () {
        return this.setAccessTime(DateTime.now());
    }


    /**
     * @param date
     * @return the number of ms since the unix epoch
     */
    private static long dateTimeToMillis ( DateTime date ) {
        return date.getMillis();
    }


    /**
     * @param date
     * @return the number of ms since the unix epoch
     */
    private static long dateToMillis ( Date date ) {
        return date.getTime();
    }

}
