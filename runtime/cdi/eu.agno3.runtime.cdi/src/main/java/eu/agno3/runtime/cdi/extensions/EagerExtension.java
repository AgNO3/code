/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2013 by mbechler
 */
package eu.agno3.runtime.cdi.extensions;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;

import eu.agno3.runtime.cdi.Eager;


/**
 * @author mbechler
 * 
 */
public class EagerExtension implements Extension {

    private List<Bean<?>> initializeOnLoadBeans = new ArrayList<>();


    /**
     * @param event
     */
    public <T> void collect ( @Observes ProcessBean<T> event ) {
        if ( event.getAnnotated().isAnnotationPresent(Eager.class) && event.getAnnotated().isAnnotationPresent(ApplicationScoped.class) ) {
            this.initializeOnLoadBeans.add(event.getBean());
        }
    }


    /**
     * @param event
     * @param beanManager
     */
    public void load ( @Observes AfterDeploymentValidation event, BeanManager beanManager ) {
        for ( Bean<?> bean : this.initializeOnLoadBeans ) {
            beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)).toString();
        }
    }
}
