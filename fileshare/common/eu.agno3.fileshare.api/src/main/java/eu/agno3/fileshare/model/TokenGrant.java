/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
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
@Table ( name = "token_grants" )
public class TokenGrant extends Grant {

    /**
     * 
     */
    private static final long serialVersionUID = 1270598245442034092L;
    private String token;
    private String identifier;

    private String comment;

    private String password;

    private boolean passwordProtected;


    /**
     * 
     */
    public TokenGrant () {}


    /**
     * 
     * @param g
     * @param refs
     * @param basic
     */
    public TokenGrant ( TokenGrant g, boolean refs, boolean basic ) {
        super(g, refs, basic);
        this.identifier = g.identifier;
        this.passwordProtected = g.getPasswordProtected();
        if ( !basic ) {
            this.token = g.token;
            this.comment = g.comment;
            this.password = g.password;
        }
    }


    /**
     * @return whether this grant is password protected
     */
    @Transient
    public boolean getPasswordProtected () {
        if ( this.password != null ) {
            return true;
        }
        return this.passwordProtected;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.Grant#cloneShallow(boolean, boolean)
     */
    @Override
    public TokenGrant cloneShallow ( boolean refs, boolean basic ) {
        return new TokenGrant(this, refs, basic);
    }


    /**
     * @return the token
     */
    @Column ( length = 64, nullable = false )
    public String getToken () {
        return this.token;
    }


    /**
     * @param token
     *            the token to set
     */
    public void setToken ( String token ) {
        this.token = token;
    }


    /**
     * @return the identifier
     */
    @Column ( nullable = true )
    public String getIdentifier () {
        return this.identifier;
    }


    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier ( String identifier ) {
        this.identifier = identifier;
    }


    /**
     * @return the comment
     */
    @Lob
    public String getComment () {
        return this.comment;
    }


    /**
     * @param comment
     *            the comment to set
     */
    public void setComment ( String comment ) {
        this.comment = comment;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.password = password;
    }
}
