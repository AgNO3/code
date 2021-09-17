/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.runtime.security.principal;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default user info implementation
     */
    public UserInfo createUserInfo () {
        return new UserInfoImpl();
    }
}
