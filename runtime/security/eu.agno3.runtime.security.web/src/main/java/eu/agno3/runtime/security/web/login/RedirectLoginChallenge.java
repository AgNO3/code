/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 6, 2016 by mbechler
 */
package eu.agno3.runtime.security.web.login;


import java.net.URI;

import eu.agno3.runtime.security.login.AbstractLoginChallenge;


/**
 * @author mbechler
 *
 */
public class RedirectLoginChallenge extends AbstractLoginChallenge<String> {

    /**
     * 
     */
    private static final long serialVersionUID = -5983472167258720035L;
    private URI absoluteTarget;
    private String relativeTarget;


    /**
     * @param id
     * 
     */
    public RedirectLoginChallenge ( String id ) {
        super(id, true, null, null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.AbstractLoginChallenge#isSecret()
     */
    @Override
    protected boolean isSecret () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getType()
     */
    @Override
    public String getType () {
        return "redirect"; //$NON-NLS-1$
    }


    /**
     * @return absolute target URL
     */
    public URI getAbsoluteTarget () {
        return this.absoluteTarget;
    }


    /**
     * @param absoluteTarget
     *            the absoluteTarget to set
     */
    public void setAbsoluteTarget ( URI absoluteTarget ) {
        this.absoluteTarget = absoluteTarget;
    }


    /**
     * @return relative (to servlet context) target path/query
     */
    public String getRelativeTarget () {
        return this.relativeTarget;
    }


    /**
     * @param relativeTarget
     *            the relativeTarget to set
     */
    public void setRelativeTarget ( String relativeTarget ) {
        this.relativeTarget = relativeTarget;
    }
}
