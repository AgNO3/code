/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.terms;


import java.util.Comparator;

import eu.agno3.orchestrator.config.terms.TermsDefinition;


/**
 * @author mbechler
 *
 */
public class TermsConfigComparator implements Comparator<TermsDefinition> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( TermsDefinition o1, TermsDefinition o2 ) {
        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        String i1 = o1.getTermsId();
        String i2 = o2.getTermsId();

        if ( i1 == null && i2 == null ) {
            return 0;
        }
        else if ( i1 == null ) {
            return -1;
        }
        else if ( i2 == null ) {
            return 1;
        }

        return i1.compareTo(i2);
    }

}
