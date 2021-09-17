/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import eu.agno3.fileshare.model.TokenShare;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "tokenSharesBean" )
public class TokenSharesBean extends AbstractSharesBean {

    /**
     * 
     */
    private static final long serialVersionUID = -6077980084437274902L;

    private TokenShare generatedTokenShare;

    private String tokenIdentifier;

    private String tokenPassword;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.share.AbstractSharesBean#init()
     */
    @Override
    @PostConstruct
    public void init () {
        super.init();

        if ( this.isRequirePassword() ) {
            enablePasswordProtection();
        }
    }


    /**
     * @return the generatedTokenShare
     */
    public TokenShare getGeneratedTokenShare () {
        return this.generatedTokenShare;
    }


    /**
     * @param generatedTokenShare
     *            the generatedTokenShare to set
     */
    public void setGeneratedTokenShare ( TokenShare generatedTokenShare ) {
        this.generatedTokenShare = generatedTokenShare;
    }


    /**
     * @return the tokenIdentifier
     */
    public String getTokenIdentifier () {
        return this.tokenIdentifier;
    }


    /**
     * @param tokenIdentifier
     *            the tokenIdentifier to set
     */
    public void setTokenIdentifier ( String tokenIdentifier ) {
        this.tokenIdentifier = tokenIdentifier;
    }


    /**
     * @return the comment
     */
    public String getComment () {
        return this.getShareProperties().getMessage();
    }


    /**
     * @param comment
     *            the comment to set
     */
    public void setComment ( String comment ) {
        this.getShareProperties().setMessage(comment);
    }


    /**
     * @return the tokenPassword
     */
    public String getTokenPassword () {
        return this.tokenPassword;
    }


    /**
     * @param tokenPassword
     *            the tokenPassword to set
     */
    public void setTokenPassword ( String tokenPassword ) {
        this.tokenPassword = tokenPassword;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.share.AbstractSharesBean#reset()
     */
    @Override
    public void reset () {
        super.reset();
        this.tokenIdentifier = null;
    }


    /**
     * 
     * @return null
     */
    public String generateMore () {
        reset();
        this.generatedTokenShare = null;
        this.tokenPassword = null;
        return null;
    }
}
