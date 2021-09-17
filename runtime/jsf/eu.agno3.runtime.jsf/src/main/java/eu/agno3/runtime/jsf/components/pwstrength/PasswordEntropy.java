/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.runtime.jsf.components.pwstrength;


import javax.faces.component.UINamingContainer;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
public class PasswordEntropy extends UINamingContainer {

    /**
     * 
     * @return the minimum entropy
     */
    public int minEntropy () {
        Integer lowLimit = (Integer) this.getAttributes().get("entropyLowerLimit"); //$NON-NLS-1$

        if ( lowLimit == null ) {
            return 40;
        }

        return lowLimit;
    }


    /**
     * 
     * @return the current entropy capped to 100 bit
     */
    public int getCappedCurrentEntropy () {
        return Math.min(100, curEntropy());
    }


    /**
     * 
     * @return the current entropy
     */
    public int curEntropy () {
        return (int) this.getAttributes().get("entropy"); //$NON-NLS-1$
    }


    /**
     * 
     * @return the bar color reflecting the current state
     */
    public String getCurrentBarColor () {
        if ( curEntropy() < minEntropy() ) {
            return "red"; //$NON-NLS-1$
        }
        else if ( curEntropy() < 80 ) {
            return "orange"; //$NON-NLS-1$
        }
        return "green"; //$NON-NLS-1$
    }


    /**
     * 
     * @return formatted entropy requirement
     */
    public String getFormattedMinRequiredMessage () {
        Integer limit = (Integer) this.getAttributes().get("entropyLowerLimit"); //$NON-NLS-1$

        if ( limit == null ) {
            return BaseMessages.get("pwstrength.noPolicyLimitMsg"); //$NON-NLS-1$
        }

        return BaseMessages.format("pwstrength.minRequiredEntropyFmt", limit); //$NON-NLS-1$
    }


    /**
     * 
     * @return formatted entropy display
     */
    public String getFormattedEntropy () {
        return BaseMessages.format("pwstrength.entropyFmt", curEntropy()); //$NON-NLS-1$
    }


    /**
     * 
     * @return poor
     */
    public String getPoorMessage () {
        return BaseMessages.get("pwstrength.poorMsg"); //$NON-NLS-1$
    }


    /**
     * 
     * @return okay
     */
    public String getOkayMessage () {
        return BaseMessages.get("pwstrength.okayMsg"); //$NON-NLS-1$
    }


    /**
     * 
     * @return good
     */
    public String getGoodMessage () {
        return BaseMessages.get("pwstrength.goodMsg"); //$NON-NLS-1$
    }

}
