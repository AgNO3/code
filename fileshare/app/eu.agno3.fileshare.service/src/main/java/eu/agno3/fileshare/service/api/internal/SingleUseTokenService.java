/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.TokenValidationException;
import eu.agno3.fileshare.model.tokens.SingleUseToken;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface SingleUseTokenService {

    /**
     * Checks that a token is still usable
     * 
     * @param tx
     * @param tok
     * @throws TokenValidationException
     */
    public void checkToken ( EntityTransactionContext tx, SingleUseToken tok ) throws TokenValidationException;


    /**
     * Invalidates a token for all further use
     * 
     * @param tx
     * @param tok
     * @param tokExpires
     * @throws TokenValidationException
     */
    public void invalidateToken ( EntityTransactionContext tx, SingleUseToken tok, DateTime tokExpires ) throws TokenValidationException;


    /**
     * @param tx
     * @return the number of removed tokens
     */
    int cleanup ( EntityTransactionContext tx );
}
