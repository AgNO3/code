/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.quota;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;


/**
 * @author mbechler
 *
 */
public abstract class AbstractQuotaEditor implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5367226934395473455L;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private boolean quotaEnabled;

    private long quotaSize;
    private int quotaExponent = 2;


    protected abstract void setQuota ( Long quota ) throws FileshareException;


    protected abstract Long getCurrentQuota ();


    /**
     * 
     */
    public AbstractQuotaEditor () {
        super();
    }


    /**
     * 
     */
    public void setQuota () {
        Long quota = null;
        if ( this.quotaEnabled ) {
            quota = this.quotaSize * (long) Math.pow(1000, this.quotaExponent);
        }
        try {
            this.setQuota(quota);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            this.exceptionHandler.handleException(e);
        }
    }


    /**
     * 
     */
    @PostConstruct
    public void revert () {
        Long current = getCurrentQuota();
        if ( current == null || current == 0 ) {
            this.quotaEnabled = false;
            this.quotaSize = 0;
            this.quotaExponent = 2;
        }
        else {
            this.quotaEnabled = true;
            this.quotaExponent = QuotaFormatter.getBaseExponent(current);
            this.quotaSize = current / (long) Math.pow(1000, this.quotaExponent);
        }
    }


    /**
     * @return the quotaEnabled
     */
    public boolean getQuotaEnabled () {
        return this.quotaEnabled;
    }


    /**
     * @param quotaEnabled
     *            the quotaEnabled to set
     */
    public void setQuotaEnabled ( boolean quotaEnabled ) {
        this.quotaEnabled = quotaEnabled;
    }


    /**
     * @return the quotaExponent
     */
    public int getQuotaExponent () {
        return this.quotaExponent;
    }


    /**
     * @param quotaExponent
     *            the quotaExponent to set
     */
    public void setQuotaExponent ( int quotaExponent ) {
        this.quotaExponent = quotaExponent;
    }


    /**
     * @return the quotaSize
     */
    public long getQuotaSize () {
        return this.quotaSize;
    }


    /**
     * @param quotaSize
     *            the quotaSize to set
     */
    public void setQuotaSize ( long quotaSize ) {
        this.quotaSize = quotaSize;
    }

}