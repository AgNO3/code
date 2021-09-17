/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.11.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class StructuralObjectReferenceImpl implements StructuralObjectReference {

    /**
     * 
     */
    private static final long serialVersionUID = -523454382119140157L;

    private UUID id;
    private StructuralObjectType type;
    private String localType;


    /**
     * 
     */
    public StructuralObjectReferenceImpl () {}


    /**
     * 
     * @param id
     * @param type
     * @param localType
     */
    public StructuralObjectReferenceImpl ( UUID id, StructuralObjectType type, String localType ) {
        super();
        this.id = id;
        this.type = type;
        this.localType = localType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectReference#getId()
     */
    @Override
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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectReference#getType()
     */
    @Override
    public StructuralObjectType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( StructuralObjectType type ) {
        this.type = type;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObjectReference#getLocalType()
     */
    @Override
    public String getLocalType () {
        return this.localType;
    }


    /**
     * @param localType
     *            the localType to set
     */
    public void setLocalType ( String localType ) {
        this.localType = localType;
    }


    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        return result;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        StructuralObjectReferenceImpl other = (StructuralObjectReferenceImpl) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("[%s] %s %s", this.type, this.localType, this.id); //$NON-NLS-1$
    }


    /**
     * @param obj
     * @return a reference object
     */
    public static StructuralObjectReference fromObject ( StructuralObject obj ) {
        return new StructuralObjectReferenceImpl(obj.getId(), obj.getType(), obj.getLocalType());
    }
}
