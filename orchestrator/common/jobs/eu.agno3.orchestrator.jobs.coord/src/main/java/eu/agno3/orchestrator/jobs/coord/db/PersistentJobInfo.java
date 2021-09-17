/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.db;


import java.util.Arrays;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.JobInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "jobs" )
@PersistenceUnit ( unitName = "jobs" )
public class PersistentJobInfo extends JobInfoImpl {

    /**
     * 
     */
    private static final long serialVersionUID = 894587779394314948L;

    private String jobGroup;
    private byte[] serializedJob;
    private int progress;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getJobId()
     */
    @Override
    @Id
    @Column ( length = 16 )
    public UUID getJobId () {
        return super.getJobId();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getType()
     */
    @Override
    @Basic
    public String getType () {
        return super.getType();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#setType(java.lang.String)
     */
    @Override
    public void setType ( String type ) {
        super.setType(type);
    }


    /**
     * @return the jobGroup
     */
    @Basic
    public String getJobGroup () {
        return this.jobGroup;
    }


    /**
     * @param group
     */
    public void setJobGroup ( String group ) {
        this.jobGroup = group;
    }


    /**
     * @return the serializedJob
     */
    @Lob
    @Basic ( fetch = FetchType.LAZY )
    public byte[] getSerializedJob () {
        if ( this.serializedJob != null ) {
            return Arrays.copyOf(this.serializedJob, this.serializedJob.length);
        }
        return new byte[] {};
    }


    /**
     * @param serializedJob
     *            the serializedJob to set
     */
    public void setSerializedJob ( byte[] serializedJob ) {
        if ( serializedJob != null ) {
            this.serializedJob = Arrays.copyOf(serializedJob, serializedJob.length);
        }
        else {
            this.serializedJob = null;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getOwner()
     */
    @Override
    @Basic
    public UserPrincipal getOwner () {
        return super.getOwner();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getState()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public JobState getState () {
        return super.getState();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getFinishedTime()
     */
    @Override
    @Basic
    public DateTime getFinishedTime () {
        return super.getFinishedTime();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getLastKeepAliveTime()
     */
    @Override
    @Basic
    public DateTime getLastKeepAliveTime () {
        return super.getLastKeepAliveTime();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getQueuedTime()
     */
    @Override
    @Basic
    public DateTime getQueuedTime () {
        return super.getQueuedTime();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobInfoImpl#getStartedTime()
     */
    @Override
    @Basic
    public DateTime getStartedTime () {
        return super.getStartedTime();
    }


    /**
     * @return the progressInternal
     */
    @Basic
    public int getProgress () {
        return this.progress;
    }


    /**
     * @param progressInternal
     *            the progressInternal to set
     */
    public void setProgress ( int progressInternal ) {
        this.progress = progressInternal;
    }

}
