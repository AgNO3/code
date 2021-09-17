/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.FlaggingService;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface FlaggingServiceInternal extends FlaggingService {

    /**
     * @param tx
     * @return the hidden entity ids for the current user
     * @throws FileshareException
     */
    Set<EntityKey> getHiddenEntities ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * 
     * @param tx
     * @return the hidden subject ids for the current user
     * @throws FileshareException
     */
    Set<UUID> getHiddenSubjects ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * 
     * @param tx
     * @return the favorite entity ids for the current user
     * @throws FileshareException
     */
    Set<EntityKey> getFavoriteEntityIds ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * @param tx
     * @return current user's favorite entities
     * @throws FileshareException
     */
    List<VFSEntity> getFavoriteEntities ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * 
     * @param tx
     * @return the favorite subject ids for the current user
     * @throws FileshareException
     */
    Set<UUID> getFavoriteSubjectIds ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * @param tx
     * @return current user's favorite subjects
     * @throws FileshareException
     */
    Set<Subject> getFavoriteSubjects ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * 
     * @param tx
     * @return current user's mail favorites
     * @throws FileshareException
     */
    Set<String> getFavoriteMails ( EntityTransactionContext tx ) throws FileshareException;


    /**
     * 
     * @param tx
     * @return current user's hidden mail
     * @throws FileshareException
     */
    Set<String> getHiddenMails ( EntityTransactionContext tx ) throws FileshareException;

}
