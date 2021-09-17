/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.10.2015 by mbechler
 */
package org.primefaces.fixed.component.treetable;


import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.primefaces.component.api.UIColumn;


/**
 * @author mbechler
 *
 */
public class ColumnComparator implements Comparator<UIColumn>, Serializable {

    private static final Logger log = Logger.getLogger(ColumnComparator.class);

    /**
     * 
     */
    private static final long serialVersionUID = -4452841159927562348L;
    private final List<String> prefixColumns;
    private final List<String> columnOrder;
    private final List<String> suffixColumns;


    /**
     * @param prefixColumns
     * @param columnOrder
     * @param suffixColumns
     */
    public ColumnComparator ( List<String> prefixColumns, List<String> columnOrder, List<String> suffixColumns ) {
        this.prefixColumns = prefixColumns;
        this.columnOrder = columnOrder;
        this.suffixColumns = suffixColumns;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( UIColumn o1, UIColumn o2 ) {

        if ( o1 == null || o2 == null ) {
            return 0;
        }

        String colId1 = o1.getColumnKey();
        String colId2 = o2.getColumnKey();

        int sep1 = colId1.lastIndexOf(':');
        int sep2 = colId2.lastIndexOf(':');

        if ( sep1 < 0 || sep2 < 0 ) {
            return 0;
        }

        String colKey1 = colId1.substring(sep1 + 1);
        String colKey2 = colId2.substring(sep2 + 1);

        int prefIdx1 = this.prefixColumns.indexOf(colKey1);
        int prefIdx2 = this.prefixColumns.indexOf(colKey2);

        if ( prefIdx1 >= 0 && prefIdx2 >= 0 ) {
            return Integer.compare(prefIdx1, prefIdx2);
        }
        else if ( prefIdx1 >= 0 ) {
            return -1;
        }
        else if ( prefIdx2 >= 0 ) {
            return 1;
        }

        int suffIdx1 = this.suffixColumns.indexOf(colKey1);
        int suffIdx2 = this.suffixColumns.indexOf(colKey2);
        if ( suffIdx1 >= 0 && suffIdx2 >= 0 ) {
            return Integer.compare(suffIdx1, suffIdx2);
        }
        else if ( suffIdx1 >= 0 ) {
            return 1;
        }
        else if ( suffIdx2 >= 0 ) {
            return -1;
        }

        int oIdx1 = this.columnOrder.indexOf(colKey1);
        int oIdx2 = this.columnOrder.indexOf(colKey2);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Compare %s (%d) to %s (%d)", colKey1, oIdx1, colKey2, oIdx2)); //$NON-NLS-1$
        }

        return Integer.compare(oIdx1, oIdx2);
    }
}
