/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.Set;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface VFSEntity {

    /**
     * @return the entity key
     */
    EntityKey getEntityKey ();


    /**
     * 
     * @return the entity type
     */
    EntityType getEntityType ();


    /**
     * 
     * @return a persistent file identifier
     */
    byte[] getInode ();


    /**
     * @return the file name
     */
    String getLocalName ();


    /**
     * @param newName
     */
    void setLocalName ( String newName );


    /**
     * @return the creating user
     */
    User getCreator ();


    /**
     * @return the creating grant
     */
    Grant getCreatorGrant ();


    /**
     * @return the owner of the entity
     */
    Subject getOwner ();


    /**
     * @param owner
     */
    void setOwner ( Subject owner );


    /**
     * @return the last modified time
     */
    DateTime getLastModified ();


    /**
     * @return the creation time
     */
    DateTime getCreated ();


    /**
     * @return the attached security label
     */
    SecurityLabel getSecurityLabel ();


    /**
     * @param securityLabel
     */
    void setSecurityLabel ( SecurityLabel securityLabel );


    /**
     * @return the expiry time
     */
    DateTime getExpires ();


    /**
     * @param newExpires
     */
    void setExpires ( DateTime newExpires );


    /**
     * @param b
     * @return a cloned entry
     */
    VFSEntity cloneShallow ( boolean b );


    /**
     * @return a cloned entry
     */
    VFSEntity cloneShallow ();


    /**
     * @param now
     */
    void setLastModified ( DateTime now );


    /**
     * @param currentUser
     */
    void setLastModifier ( User currentUser );


    /**
     * @return the last modifier
     */
    User getLastModifier ();


    /**
     * @return the last modifying grant
     */
    Grant getLastModifiedGrant ();


    /**
     * @param grant
     */
    void setLastModifiedGrant ( Grant grant );


    /**
     * @param currentUser
     */
    void setCreator ( User currentUser );


    /**
     * @param now
     */
    void setCreated ( DateTime now );


    /**
     * @param grant
     */
    void setCreatorGrant ( Grant grant );


    /**
     * @return whether a parent entity exists
     */
    boolean hasParent ();


    /**
     * @return whether grants are attached to the object
     */
    boolean hasGrants ();


    /**
     * @return whether the object is locally shared
     */
    boolean hasLocalValidGrants ();


    /**
     * 
     * @return whether this object is readonly, even when other permission exists
     */
    boolean isStaticReadOnly ();


    /**
     * @return local valid grants
     */
    Set<Grant> getLocalValidGrants ();


    /**
     * @return whether this entities vfs allows sharing
     */
    boolean isStaticSharable ();

}
