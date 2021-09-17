/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.02.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 *
 */
public class PersistentReferenceWalkerContext implements ReferenceWalkerContext {

    private final @NonNull EntityManager em;
    private @NonNull PersistenceUtil persistenceUtil;


    /**
     * @param em
     * @param pu
     */
    public PersistentReferenceWalkerContext ( @NonNull EntityManager em, @NonNull PersistenceUtil pu ) {
        this.em = em;
        this.persistenceUtil = pu;
    }


    /**
     * @return the em
     */
    public @NonNull EntityManager getEnityManager () {
        return this.em;
    }


    /**
     * @return the peristence util
     */
    public @NonNull PersistenceUtil getPersistenceUtil () {
        return this.persistenceUtil;
    }

}
