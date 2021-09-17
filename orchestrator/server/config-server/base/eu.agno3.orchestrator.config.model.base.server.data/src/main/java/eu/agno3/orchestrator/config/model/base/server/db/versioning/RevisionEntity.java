/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.db.versioning;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import eu.agno3.orchestrator.config.model.base.versioning.RevisionType;


/**
 * @author mbechler
 * 
 */
@Entity
@org.hibernate.envers.RevisionEntity ( RevisionEntityListener.class )
@PersistenceUnit ( unitName = "config" )
@Table ( name = "revisions" )
public class RevisionEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2333133977734500323L;
    private long revision;
    private Date revisionTimestamp;
    private String user;
    private RevisionType overrideRevisionType;


    /**
     * @return the revision
     */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    @RevisionNumber
    public long getRevision () {
        return this.revision;
    }


    /**
     * @param revision
     *            the revision to set
     */
    public void setRevision ( long revision ) {
        this.revision = revision;
    }


    /**
     * @param revisionTimestamp
     *            the revisionTimestamp to set
     */
    public void setRevisionTimestamp ( Date revisionTimestamp ) {
        this.revisionTimestamp = revisionTimestamp;
    }


    /**
     * @return the revisionTimestamp
     */
    @RevisionTimestamp
    @Temporal ( TemporalType.TIMESTAMP )
    public Date getRevisionTimestamp () {
        return this.revisionTimestamp;
    }


    /**
     * @return the user
     */
    @Basic
    @Column ( name = "username" )
    public String getUser () {
        return this.user;
    }


    /**
     * @param user
     *            the user to set
     */
    public void setUser ( String user ) {
        this.user = user;
    }


    /**
     * @return the overrideRevisionType
     */
    @Basic
    @Column ( nullable = true )
    public RevisionType getOverrideRevisionType () {
        return this.overrideRevisionType;
    }


    /**
     * @param overrideRevisionType
     *            the overrideRevisionType to set
     */
    public void setOverrideRevisionType ( RevisionType overrideRevisionType ) {
        this.overrideRevisionType = overrideRevisionType;
    }

}
