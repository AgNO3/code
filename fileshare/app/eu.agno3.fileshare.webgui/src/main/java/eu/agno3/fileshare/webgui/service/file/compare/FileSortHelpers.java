/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.compare;


import java.text.Collator;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
@Named ( "fileSortHelpers" )
@ApplicationScoped
public class FileSortHelpers {

    /**
     * @param a
     * @param b
     * @return
     */
    protected static int sortByFileNameInternal ( VFSEntity a, VFSEntity b ) {
        if ( a instanceof VFSContainerEntity && b instanceof VFSContainerEntity ) {}
        else if ( a instanceof VFSContainerEntity ) {
            return -1;
        }
        else if ( b instanceof VFSContainerEntity ) {
            return 1;
        }

        if ( a.getLocalName() == null && b.getLocalName() == null ) {
            return 0;
        }
        else if ( a.getLocalName() == null ) {
            return -1;
        }
        else if ( b.getLocalName() == null ) {
            return 0;
        }

        return fileNameCompare(a.getLocalName(), b.getLocalName());
    }


    /**
     * @param localName
     * @param localName2
     * @return
     */
    private static int fileNameCompare ( String a, String b ) {
        return compareStringsInLocale(a, b);
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int compareStringsInLocale ( String a, String b ) {
        return Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()).compare(a, b);
    }


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

        return compareStringsInLocale(a.getName(), b.getName());
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int sortByUserInternal ( UserInfo a, UserInfo b ) {
        return compareStringsInLocale(a.getUserDisplayName(), b.getUserDisplayName());
    }
}
