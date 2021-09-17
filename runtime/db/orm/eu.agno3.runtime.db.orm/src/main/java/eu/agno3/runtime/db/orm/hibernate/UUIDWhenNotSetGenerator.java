/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2016 by mbechler
 */
package eu.agno3.runtime.db.orm.hibernate;


import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;


/**
 * @author mbechler
 *
 */
public class UUIDWhenNotSetGenerator extends UUIDGenerator {

    private String entityName;


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.id.UUIDGenerator#configure(org.hibernate.type.Type, java.util.Properties,
     *      org.hibernate.service.ServiceRegistry)
     */
    @Override
    public void configure ( Type type, Properties props, ServiceRegistry sr ) throws MappingException {
        this.entityName = props.getProperty(ENTITY_NAME);
        super.configure(type, props, sr);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.id.UUIDGenerator#generate(org.hibernate.engine.spi.SharedSessionContractImplementor,
     *      java.lang.Object)
     */
    @Override
    public Serializable generate ( SharedSessionContractImplementor session, Object object ) throws HibernateException {
        Serializable id = session.getEntityPersister(this.entityName, object).getIdentifier(object, session);
        if ( id != null ) {
            return id;
        }
        return super.generate(session, object);
    }

}
