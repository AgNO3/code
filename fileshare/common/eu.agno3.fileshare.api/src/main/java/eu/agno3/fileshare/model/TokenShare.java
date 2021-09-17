/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class TokenShare implements LinkShareData, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8591160808837352640L;
    private Grant grant;
    private boolean viewable;
    private String downloadURL;
    private String viewURL;
    private boolean hideSensitive;


    /**
     * @return the grant
     */
    public Grant getGrant () {
        return this.grant;
    }


    /**
     * @param grant
     *            the grant to set
     */
    public void setGrant ( Grant grant ) {
        this.grant = grant;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#getViewable()
     */
    @Override
    public boolean getViewable () {
        return this.viewable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#setViewable(boolean)
     */
    @Override
    public void setViewable ( boolean viewable ) {
        this.viewable = viewable;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#getDownloadURL()
     */
    @Override
    public String getDownloadURL () {
        return this.downloadURL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#setDownloadURL(java.lang.String)
     */
    @Override
    public void setDownloadURL ( String downloadURL ) {
        this.downloadURL = downloadURL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#getViewURL()
     */
    @Override
    public String getViewURL () {
        return this.viewURL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#setViewURL(java.lang.String)
     */
    @Override
    public void setViewURL ( String viewURL ) {
        this.viewURL = viewURL;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.LinkShareData#getHideSensitive()
     */
    @Override
    public boolean getHideSensitive () {
        return this.hideSensitive;
    }


    /**
     * @param hideSensitive
     *            the hideSensitive to set
     */
    public void setHideSensitive ( boolean hideSensitive ) {
        this.hideSensitive = hideSensitive;
    }
}
