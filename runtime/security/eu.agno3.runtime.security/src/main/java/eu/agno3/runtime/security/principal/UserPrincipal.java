/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.runtime.security.principal;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@Embeddable
@SafeSerialization
public class UserPrincipal implements Externalizable {

    /**
     * 
     */
    private static final long serialVersionUID = -6321315089821147278L;

    private UUID userId;
    private String userName;
    private String realmName;


    /**
     * 
     */
    public UserPrincipal () {}


    /**
     * @param realmName
     * @param userId
     * @param userName
     */
    public UserPrincipal ( String realmName, UUID userId, String userName ) {
        this.realmName = realmName;
        this.userId = userId;
        this.userName = userName;
    }


    /**
     * @return the userId
     */
    @Basic
    @Column ( length = 16 )
    public UUID getUserId () {
        return this.userId;
    }


    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId ( UUID userId ) {
        this.userId = userId;
    }


    /**
     * @return the realmName
     */
    @Basic
    public String getRealmName () {
        return this.realmName;
    }


    /**
     * @param realmName
     *            the realmName to set
     */
    public void setRealmName ( String realmName ) {
        this.realmName = realmName;
    }


    /**
     * @return the userName
     */
    @Basic
    public String getUserName () {
        return this.userName;
    }


    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName ( String userName ) {
        this.userName = userName;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s@%s", this.userName, this.realmName); //$NON-NLS-1$
    }


    @Override
    public void writeExternal ( ObjectOutput oos ) throws IOException {
        oos.writeBoolean(this.userId != null);
        if ( this.userId != null ) {
            oos.writeLong(this.userId.getMostSignificantBits());
            oos.writeLong(this.userId.getLeastSignificantBits());
        }
        oos.writeBoolean(this.realmName != null);
        if ( this.realmName != null ) {
            oos.writeUTF(this.realmName);
        }

        oos.writeBoolean(this.userName != null);
        if ( this.userName != null ) {
            oos.writeUTF(this.userName);
        }
    }


    @Override
    public void readExternal ( ObjectInput ois ) throws IOException {
        if ( ois.readBoolean() ) {
            long msb = ois.readLong();
            long lsb = ois.readLong();
            this.userId = new UUID(msb, lsb);
        }

        if ( ois.readBoolean() ) {
            this.realmName = ois.readUTF();
        }

        if ( ois.readBoolean() ) {
            this.userName = ois.readUTF();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.realmName == null ) ? 0 : this.realmName.hashCode() );
        result = prime * result + ( ( this.userId == null ) ? 0 : this.userId.hashCode() );
        result = prime * result + ( ( this.userName == null ) ? 0 : this.userName.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        UserPrincipal other = (UserPrincipal) obj;
        if ( this.realmName == null ) {
            if ( other.realmName != null )
                return false;
        }
        else if ( !this.realmName.equals(other.realmName) )
            return false;
        if ( this.userId == null ) {
            if ( other.userId != null )
                return false;
        }
        else if ( !this.userId.equals(other.userId) )
            return false;
        if ( this.userName == null ) {
            if ( other.userName != null )
                return false;
        }
        else if ( !this.userName.equals(other.userName) )
            return false;
        return true;
    }
    // -GENERATED

}
