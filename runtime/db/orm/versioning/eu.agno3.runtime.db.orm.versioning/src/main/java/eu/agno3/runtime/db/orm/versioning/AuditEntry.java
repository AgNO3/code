/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.versioning;


import org.hibernate.envers.RevisionType;


/**
 * @author mbechler
 * @param <TEntity>
 *            type of entity
 * @param <TRevisionEntity>
 *            type of revision entity
 * 
 */
public class AuditEntry <TEntity, TRevisionEntity> {

    private TEntity entity;
    private TRevisionEntity revisionEntity;
    private RevisionType type;


    /**
     * @param entity
     * @param revisionEntity
     * @param type
     */
    public AuditEntry ( TEntity entity, TRevisionEntity revisionEntity, RevisionType type ) {
        super();
        this.entity = entity;
        this.revisionEntity = revisionEntity;
        this.type = type;
    }


    /**
     * @return the entity state associate of this entry
     */
    public TEntity getEntity () {
        return this.entity;
    }


    /**
     * @return the revision entity associated with this entry's change
     */
    public TRevisionEntity getRevisionEntity () {
        return this.revisionEntity;
    }


    /**
     * 
     * @return the type of this entry's change.
     */
    public RevisionType getRevisionType () {
        return this.type;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof AuditEntry ) {
            AuditEntry<?, ?> other = (AuditEntry<?, ?>) obj;

            return this.entity.equals(other.entity) && this.revisionEntity.equals(other.revisionEntity) && this.type.equals(other.type);
        }

        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.entity.hashCode() + this.revisionEntity.hashCode() + this.type.hashCode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        StringBuilder out = new StringBuilder();

        out.append("AuditEntry: "); //$NON-NLS-1$
        out.append(this.type);
        out.append(" "); //$NON-NLS-1$
        out.append(this.entity);
        out.append(" "); //$NON-NLS-1$
        out.append(this.revisionEntity);

        return out.toString();
    }
}
