/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "content_user_grouping" )
public class UserGroupingEntity extends ContentEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -5999842520130696385L;
    private Set<Subject> subjects = new HashSet<>();


    /**
     * 
     */
    public UserGroupingEntity () {}


    /**
     * 
     * @param e
     * @param refs
     */
    public UserGroupingEntity ( UserGroupingEntity e, boolean refs ) {
        super(e, refs);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.ContentEntity#cloneShallow(boolean)
     */
    @Override
    public ContentEntity cloneShallow ( boolean refs ) {
        return new UserGroupingEntity(this, refs);
    }


    /**
     * @return the subjects
     */
    @ManyToMany
    public Set<Subject> getSubjects () {
        return this.subjects;
    }


    /**
     * @param subjects
     *            the subjects to set
     */
    public void setSubjects ( Set<Subject> subjects ) {
        this.subjects = subjects;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.VFSEntity#isStaticSharable()
     */
    @Override
    @Transient
    public boolean isStaticSharable () {
        return true;
    }

}
