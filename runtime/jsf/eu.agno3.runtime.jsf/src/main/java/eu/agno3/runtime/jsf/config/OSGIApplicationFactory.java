/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ApplicationWrapper;


/**
 * @author mbechler
 * 
 */
public class OSGIApplicationFactory extends ApplicationFactory {

    private ApplicationFactory wrapped;
    private Application application;


    /**
     * 
     */
    public OSGIApplicationFactory () {}


    /**
     * 
     * @param wrapped
     */
    public OSGIApplicationFactory ( ApplicationFactory wrapped ) {
        this.wrapped = wrapped;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ApplicationFactory#getApplication()
     */
    @Override
    public Application getApplication () {
        if ( this.application == null ) {
            this.application = createApplication();
        }
        return this.application;
    }


    /**
     * @return
     */
    private Application createApplication () {
        Application app = this.wrapped.getApplication();
        while ( ! ( app instanceof OSGIApplicationWrapper ) && app instanceof ApplicationWrapper ) {
            app = ( (ApplicationWrapper) app ).getWrapped();
        }

        if ( ! ( app instanceof OSGIApplicationWrapper ) ) {
            app = new OSGIApplicationWrapper(app);
        }

        return app;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.application.ApplicationFactory#setApplication(javax.faces.application.Application)
     */
    @Override
    public void setApplication ( Application app ) {
        this.wrapped.setApplication(app);
    }

}
