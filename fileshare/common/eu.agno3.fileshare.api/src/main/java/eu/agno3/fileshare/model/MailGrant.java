/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "fileshare" )
@Entity
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "mail_grants", indexes = {
    @Index ( columnList = "mailAddress" )
})
public class MailGrant extends TokenGrant {

    /**
     * 
     */
    private static final long serialVersionUID = 5460297553685779568L;

    private String mailAddress;


    /**
     * 
     */
    public MailGrant () {}


    /**
     * 
     * @param g
     * @param refs
     * @param basic
     */
    public MailGrant ( MailGrant g, boolean refs, boolean basic ) {
        super(g, refs, basic);
        this.mailAddress = g.mailAddress;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Grant#cloneShallow(boolean, boolean)
     */
    @Override
    public MailGrant cloneShallow ( boolean refs, boolean basic ) {
        return new MailGrant(this, refs, basic);
    }


    /**
     * @return the mailAddress
     */
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.mailAddress = mailAddress;
    }
}
