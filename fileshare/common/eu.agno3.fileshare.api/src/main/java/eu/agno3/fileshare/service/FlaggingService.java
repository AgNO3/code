/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public interface FlaggingService {

    /**
     * @return the current user's favorite entities
     * @throws FileshareException
     */
    List<VFSEntity> getFavoriteEntities () throws FileshareException;


    /**
     * @return the hidden entity ids for the current user
     * @throws FileshareException
     */
    Set<EntityKey> getHiddenEntities () throws FileshareException;


    /**
     * 
     * @return the hidden subject ids for the current user
     * @throws FileshareException
     */
    Set<UUID> getHiddenSubjects () throws FileshareException;


    /**
     * 
     * @return the favorite entity ids for the current user
     * @throws FileshareException
     */
    Set<EntityKey> getFavoriteEntityIds () throws FileshareException;


    /**
     * @return the favorite subject ids for the current user
     * @throws FileshareException
     */
    Set<UUID> getFavoriteSubjectIds () throws FileshareException;


    /**
     * @return current user's favorite subjects
     * @throws FileshareException
     */
    Set<Subject> getFavoriteSubjects () throws FileshareException;


    /**
     * 
     * @return current user's mail favorites
     * @throws FileshareException
     */
    Set<String> getFavoriteMails () throws FileshareException;


    /**
     * 
     * @return current user's hidden mail
     * @throws FileshareException
     */
    Set<String> getHiddenMails () throws FileshareException;


    /**
     * @param es
     * @throws FileshareException
     */
    void markEntitiesHidden ( Collection<EntityKey> es ) throws FileshareException;


    /**
     * @param es
     * @throws FileshareException
     */
    void markSubjectsHidden ( Collection<UUID> es ) throws FileshareException;


    /**
     * 
     * @param es
     * @throws FileshareException
     */
    void markEntitiesFavorite ( Collection<EntityKey> es ) throws FileshareException;


    /**
     * 
     * @param es
     * @throws FileshareException
     */
    void markSubjectsFavorite ( Collection<UUID> es ) throws FileshareException;


    /**
     * 
     * @param es
     * @throws FileshareException
     */
    void markEntitiesVisible ( Collection<EntityKey> es ) throws FileshareException;


    /**
     * 
     * @param es
     * @throws FileshareException
     */
    void markSubjectsVisible ( Collection<UUID> es ) throws FileshareException;


    /**
     * 
     * @param es
     * @throws FileshareException
     */
    void unmarkEntitiesFavorite ( Collection<EntityKey> es ) throws FileshareException;


    /**
     * 
     * @param es
     * @throws FileshareException
     */
    void unmarkSubjectsFavorite ( Collection<UUID> es ) throws FileshareException;


    /**
     * 
     * @param addr
     * @throws FileshareException
     */
    void markMailFavorite ( String addr ) throws FileshareException;


    /**
     * 
     * @param addr
     * @throws FileshareException
     */
    void unmarkMailFavorite ( String addr ) throws FileshareException;


    /**
     * @throws FileshareException
     * 
     */
    void markLinksFavorite () throws FileshareException;


    /**
     * @throws FileshareException
     * 
     */
    void unmarkLinksFavorite () throws FileshareException;


    /**
     * 
     * @param addr
     * @throws FileshareException
     */
    void markMailHidden ( String addr ) throws FileshareException;


    /**
     * 
     * @param addr
     * @throws FileshareException
     */
    void unmarkMailHidden ( String addr ) throws FileshareException;


    /**
     * @throws FileshareException
     * 
     */
    void markLinksHidden () throws FileshareException;


    /**
     * @throws FileshareException
     * 
     */
    void unmarkLinksHidden () throws FileshareException;


    /**
     * @param targetId
     * @throws FileshareException
     */
    void trackEntityFavorityUsage ( EntityKey targetId ) throws FileshareException;

}
