/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.system.backups;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class BackupInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3597050519973600757L;

    private DateTime timestamp;
    private UUID id;
    private Long totalSize;
    private List<ServiceBackupInfo> serviceBackups = new ArrayList<>();

    private String applianceVersion;
    private String applianceBuild;


    /**
     * @return the backup id
     */
    public UUID getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( UUID id ) {
        this.id = id;
    }


    /**
     * 
     * @return the backup timestamp
     */
    public DateTime getTimestamp () {
        return this.timestamp;
    }


    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp ( DateTime timestamp ) {
        this.timestamp = timestamp;
    }


    /**
     * @return the totalSize
     */
    public Long getTotalSize () {
        return this.totalSize;
    }


    /**
     * @param totalSize
     *            the totalSize to set
     */
    public void setTotalSize ( Long totalSize ) {
        this.totalSize = totalSize;
    }


    /**
     * @return the backupped services
     */
    public List<ServiceBackupInfo> getServices () {
        return this.serviceBackups;
    }


    /**
     * @param serviceBackups
     *            the serviceBackups to set
     */
    public void setServices ( List<ServiceBackupInfo> serviceBackups ) {
        this.serviceBackups = serviceBackups;
    }


    /**
     * @return the applianceVersion
     */
    public String getApplianceVersion () {
        return this.applianceVersion;
    }


    /**
     * @param appVe
     */
    public void setApplianceVersion ( String appVe ) {
        this.applianceVersion = appVe;
    }


    /**
     * @return the applianceBuild
     */
    public String getApplianceBuild () {
        return this.applianceBuild;
    }


    /**
     * @param applianceBuild
     *            the applianceBuild to set
     */
    public void setApplianceBuild ( String applianceBuild ) {
        this.applianceBuild = applianceBuild;
    }
}
