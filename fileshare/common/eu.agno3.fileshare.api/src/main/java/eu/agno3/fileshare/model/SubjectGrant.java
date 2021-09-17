/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "subject_grants" )
public class SubjectGrant extends Grant {

    /**
     * 
     */
    private static final long serialVersionUID = 2944128000590061821L;
    private Subject target;


    /**
     * 
     */
    public SubjectGrant () {}


    /**
     * 
     * @param g
     * @param refs
     * @param basic
     */
    public SubjectGrant ( SubjectGrant g, boolean refs, boolean basic ) {
        super(g, refs, basic);
        if ( !basic ) {
            this.target = g.target.cloneShallow(false);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Grant#cloneShallow(boolean, boolean)
     */
    @Override
    public SubjectGrant cloneShallow ( boolean refs, boolean basic ) {
        return new SubjectGrant(this, refs, basic);
    }


    /**
     * @return the target
     */
    @ManyToOne
    public Subject getTarget () {
        return this.target;
    }


    /**
     * @param target
     *            the target to set
     */
    public void setTarget ( Subject target ) {
        this.target = target;
    }

}
