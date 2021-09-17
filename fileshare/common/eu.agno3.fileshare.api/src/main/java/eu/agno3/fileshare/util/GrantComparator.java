/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.util;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;


/**
 * @author mbechler
 *
 */
public class GrantComparator implements Comparator<Grant>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1212088675260051301L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Grant g1, Grant g2 ) {
        return compareStatic(g1, g2);
    }


    /**
     * 
     * @param g1
     * @param g2
     * @return comppare result
     */
    public static int compareStatic ( Grant g1, Grant g2 ) {
        if ( g1 instanceof SubjectGrant && g2 instanceof SubjectGrant ) {
            return compareSubjectGrants((SubjectGrant) g1, (SubjectGrant) g2);
        }
        else if ( g1 instanceof SubjectGrant ) {
            return -1;
        }
        else if ( g2 instanceof SubjectGrant ) {
            return 1;
        }

        if ( g1 instanceof MailGrant && g2 instanceof MailGrant ) {
            return compareMailGrants((MailGrant) g1, (MailGrant) g2);
        }
        else if ( g1 instanceof MailGrant ) {
            return -1;
        }
        else if ( g2 instanceof MailGrant ) {
            return 1;
        }

        if ( g1 instanceof TokenGrant && g2 instanceof TokenGrant ) {
            return compareTokenGrants((TokenGrant) g1, (TokenGrant) g2);
        }
        else if ( g1 instanceof TokenGrant ) {
            return -1;
        }
        else if ( g2 instanceof TokenGrant ) {
            return 1;
        }

        return compareGrantGeneric(g1, g2);
    }


    /**
     * @param g1
     * @param g2
     * @return
     */
    private static int compareTokenGrants ( TokenGrant g1, TokenGrant g2 ) {

        if ( g1.getIdentifier() == null && g2.getIdentifier() == null ) {
            return compareGrantGeneric(g1, g2);
        }
        else if ( g1.getIdentifier() == null ) {
            return 1;
        }
        else if ( g2.getIdentifier() == null ) {
            return -1;
        }

        return g1.getIdentifier().compareTo(g2.getIdentifier());
    }


    /**
     * @param g1
     * @param g2
     * @return
     */
    private static int compareMailGrants ( MailGrant g1, MailGrant g2 ) {

        if ( g1.getMailAddress() == null && g2.getMailAddress() == null ) {
            return 0;
        }
        else if ( g1.getMailAddress() == null ) {
            return -1;
        }
        else if ( g2.getMailAddress() == null ) {
            return 1;
        }

        return g1.getMailAddress().compareTo(g2.getMailAddress());
    }


    /**
     * @param g1
     * @param g2
     * @return
     */
    private static int compareGrantGeneric ( Grant g1, Grant g2 ) {
        int res = g1.getCreated().compareTo(g2.getCreated());
        if ( res != 0 ) {
            return res;
        }
        return g1.getId().compareTo(g2.getId());
    }


    /**
     * @param g1
     * @param g2
     * @return
     */
    private static int compareSubjectGrants ( SubjectGrant g1, SubjectGrant g2 ) {
        int res = SubjectCompareUtil.sortBySubject(g1.getTarget(), g2.getTarget());

        if ( res != 0 ) {
            return res;
        }

        return compareGrantGeneric(g1, g2);
    }
}
