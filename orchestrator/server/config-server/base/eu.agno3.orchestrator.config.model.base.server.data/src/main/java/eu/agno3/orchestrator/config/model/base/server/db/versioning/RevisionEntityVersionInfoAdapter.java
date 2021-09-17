/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.db.versioning;


import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import eu.agno3.orchestrator.config.model.base.versioning.RevisionType;
import eu.agno3.orchestrator.config.model.base.versioning.VersionInfo;


/**
 * @author mbechler
 * 
 */
public class RevisionEntityVersionInfoAdapter implements VersionInfo {

    private static final long serialVersionUID = -3505499047639782277L;
    private RevisionEntity revisionEntity;
    private org.hibernate.envers.RevisionType revType;


    /**
     * @param revisionEntity
     * @param revType
     */
    public RevisionEntityVersionInfoAdapter ( RevisionEntity revisionEntity, org.hibernate.envers.RevisionType revType ) {
        this.revisionEntity = revisionEntity;
        this.revType = revType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.VersionInfo#getRevisionNumber()
     */
    @Override
    public long getRevisionNumber () {
        return this.revisionEntity.getRevision();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.VersionInfo#getRevisionTime()
     */
    @Override
    public Date getRevisionTime () {
        return this.revisionEntity.getRevisionTimestamp();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.VersionInfo#getRevisionType()
     */
    @Override
    public RevisionType getRevisionType () {
        if ( this.revisionEntity.getOverrideRevisionType() != null ) {
            return this.revisionEntity.getOverrideRevisionType();
        }
        return fromEnvers(this.revType);
    }

    private static final Map<org.hibernate.envers.RevisionType, RevisionType> ENVERS_TYPES = new EnumMap<>(org.hibernate.envers.RevisionType.class);

    static {
        ENVERS_TYPES.put(org.hibernate.envers.RevisionType.ADD, RevisionType.ADD);
        ENVERS_TYPES.put(org.hibernate.envers.RevisionType.MOD, RevisionType.MODIFY);
        ENVERS_TYPES.put(org.hibernate.envers.RevisionType.DEL, RevisionType.DELETE);
    }


    /**
     * @param revisionType
     * @return the revision type entry for a envers revision type
     */
    public static RevisionType fromEnvers ( org.hibernate.envers.RevisionType revisionType ) {
        RevisionType t = ENVERS_TYPES.get(revisionType);

        if ( t == null ) {
            return RevisionType.UNKNOWN;
        }

        return t;
    }

}
