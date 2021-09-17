package eu.agno3.runtime.jsf.windowscope;


import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;


/**
 * 
 * @author mbechler
 */
public class WindowScopedContextExtension implements Extension {

    void beforeBeanDiscovery ( @Observes final BeforeBeanDiscovery event, BeanManager beanManager ) {
        event.addScope(WindowScoped.class, true, true);
    }


    void afterBeanDiscovery ( @Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager ) {
        afterBeanDiscovery.addContext(new WindowScopedContextImpl());
    }
}
