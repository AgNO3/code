/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2015 by mbechler
 */
package eu.agno3.fileshare.util;


import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.UserInfo;


/**
 * @author mbechler
 *
 */
public final class SubjectCompareUtil {

    /**
     * 
     */
    private SubjectCompareUtil () {}


    /**
     * @param a
     * @param b
     * @return compare result
     */
    public static int sortBySubject ( SubjectInfo a, SubjectInfo b ) {

        if ( ! ( a != null ) && ! ( b != null ) ) {
            return 0;
        }
        else if ( ! ( a != null ) ) {
            return -1;
        }
        else if ( ! ( b != null ) ) {
            return 1;
        }

        if ( a instanceof UserInfo && b instanceof UserInfo ) {
            return sortByUserInternal((UserInfo) a, (UserInfo) b);
        }
        else if ( a instanceof GroupInfo && b instanceof GroupInfo ) {
            return sortByGroupInternal((GroupInfo) a, (GroupInfo) b);
        }

        return sortBySubjectTypeInternal(a, b);
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int sortBySubjectTypeInternal ( SubjectInfo a, SubjectInfo b ) {
        return a.getClass().getName().compareTo(b.getClass().getName());
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int sortByGroupInternal ( GroupInfo a, GroupInfo b ) {

        if ( a.getName() == null && b.getName() == null ) {
            return 0;
        }
        else if ( a.getName() == null ) {
            return -1;
        }
        else if ( b.getName() == null ) {
            return 1;
        }

        return a.getName().compareTo(b.getName());
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int sortByUserInternal ( UserInfo a, UserInfo b ) {
        return a.getUserDisplayName().compareTo(b.getUserDisplayName());
    }
}
