/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate.internal;


import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;

import eu.agno3.runtime.db.schema.orm.hibernate.HibernateIndexingException;

import liquibase.structure.core.Column;
import liquibase.structure.core.Column.AutoIncrementInformation;
import liquibase.structure.core.DataType;
import liquibase.structure.core.DataType.ColumnSizeUnit;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;


/**
 * @author mbechler
 * 
 */
public class HibernateColumnIndexer {

    /**
     * 
     */
    private static final Pattern VARCHAR_PATTERN = Pattern
            .compile(".*((VAR)?CHAR)\\s*\\((\\d*)\\)\\s+FOR\\s+BIT\\s+DATA.*", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    /**
     * 
     */
    private static final DataType BINARY_DATA_TYPE = new DataType("java.sql.Types.BINARY"); //$NON-NLS-1$
    /**
     * 
     */
    private static final DataType VARBINARY_DATA_TYPE = new DataType("java.sql.Types.VARBINARY"); //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(HibernateColumnIndexer.class);
    private static final Pattern DATATYPE_PATTERN = Pattern.compile("([^\\(]*)\\s*\\(?\\s*(\\d*)?\\s*,?\\s*(\\d*)?\\s*([^\\(]*?)\\)?"); //$NON-NLS-1$

    private Dialect dialect;
    private Mapping mapping;


    /**
     * @param dialect
     * @param mapping
     * 
     */
    public HibernateColumnIndexer ( Dialect dialect, Mapping mapping ) {
        this.dialect = dialect;
        this.mapping = mapping;
    }


    /**
     * @param tbl
     * @param t
     * @param idCol
     * @throws HibernateIndexingException
     */
    public void mapColumns ( org.hibernate.mapping.Table tbl, Table t, String idCol ) throws HibernateIndexingException {
        // columns
        Iterator<org.hibernate.mapping.Column> columnIt = tbl.getColumnIterator();
        while ( columnIt.hasNext() ) {
            org.hibernate.mapping.Column col = columnIt.next();

            if ( col.isFormula() ) {
                continue;
            }

            if ( idCol != null && idCol.equals(col.getName()) ) {
                Column targetIdColumn = this.setupColumn(t, col);
                AutoIncrementInformation autoIncrementInformation = new AutoIncrementInformation();
                targetIdColumn.setAutoIncrementInformation(autoIncrementInformation);
                t.getColumns().add(targetIdColumn);
                continue;
            }

            t.getColumns().add(this.setupColumn(t, col));
        }
    }


    /**
     * @param t
     * @param mapping
     * @param dialect
     * @param columnIt
     * @return
     * @throws HibernateIndexingException
     */
    private Column setupColumn ( Table t, org.hibernate.mapping.Column col ) throws HibernateIndexingException {

        Column destCol = new Column();
        destCol.setSnapshotId(UUID.randomUUID().toString());
        destCol.setName(col.getName());
        destCol.setRelation(t);
        destCol.setDefaultValue(col.getDefaultValue());
        destCol.setNullable(col.isNullable());
        destCol.setRemarks(col.getComment());

        if ( col.isUnique() ) {
            setupColumnUniqueConstraint(t, destCol);
        }

        DataType type = this.getColumnType(t, col);

        destCol.setType(type);

        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "+ adding column %s of type %s", //$NON-NLS-1$
                col.getName(),
                type.toString()));
        }
        return destCol;
    }


    /**
     * @param t
     * @param destCol
     */
    private static void setupColumnUniqueConstraint ( Table t, Column destCols ) {
        UniqueConstraint uc = new UniqueConstraint();
        String name = "UC_" + t.getName().toUpperCase() + //$NON-NLS-1$
                t.getName().toUpperCase() + "_COL"; //$NON-NLS-1$
        if ( name.length() > 64 ) {
            name = name.substring(0, 63);
        }
        // Index backingIndex = new Index();
        // backingIndex.setUnique(true);
        // backingIndex.setColumns(Arrays.asList(destCols));
        // backingIndex.setTable(t);
        // backingIndex.setName(name);
        uc.setName(name);
        uc.setColumns(Arrays.asList(destCols));
        uc.setTable(t);
        // uc.setBackingIndex(backingIndex);
        t.getUniqueConstraints().add(uc);
    }


    /**
     * @param t
     * @param col
     * @param mapping
     * @param dialect
     * @return
     * @throws HibernateIndexingException
     */
    protected DataType getColumnType ( Table t, org.hibernate.mapping.Column col ) throws HibernateIndexingException {
        String colType = null;
        try {
            colType = col.getSqlType(this.dialect, this.mapping);
        }
        catch ( MappingException e ) {
            throw new HibernateIndexingException(String.format(
                "Failed to get column type for column %s.%s:", //$NON-NLS-1$
                t.getName(),
                col.getName()), e);
        }
        // hack because liquibase cannot handle a type name with arguments embedded
        int sqlTypeCode = col.getSqlTypeCode(this.mapping);
        if ( sqlTypeCode == -2 || sqlTypeCode == -3 ) {
            Matcher m = VARCHAR_PATTERN.matcher(colType);

            if ( !m.matches() ) {
                throw new HibernateIndexingException("Failed to parse binary type: " + colType); //$NON-NLS-1$
            }
            int size = Integer.parseInt(m.group(3));
            DataType type;
            if ( sqlTypeCode == -3 ) {
                type = VARBINARY_DATA_TYPE;
            }
            else if ( sqlTypeCode == -2 ) {
                type = BINARY_DATA_TYPE;
            }
            else {
                throw new HibernateIndexingException("Failed to determine binary type: " + colType); //$NON-NLS-1$
            }
            type.setColumnSize(size);
            type.setCharacterOctetLength(size);
            type.setColumnSizeUnit(ColumnSizeUnit.BYTE);
            type.setDataTypeId(col.getSqlTypeCode());
            return type;
        }

        return toDataType(colType, col.getSqlTypeCode());
    }


    protected DataType toDataType ( String hibernateType, Integer sqlTypeCode ) {
        Matcher matcher = DATATYPE_PATTERN.matcher(hibernateType);
        if ( !matcher.matches() ) {
            return null;
        }
        DataType dataType = new DataType(matcher.group(1));
        if ( matcher.group(3).isEmpty() && !matcher.group(2).isEmpty() ) {
            dataType.setColumnSize(Integer.valueOf(matcher.group(2)));
            dataType.setColumnSizeUnit(ColumnSizeUnit.BYTE);
        }
        else if ( !matcher.group(3).isEmpty() && !matcher.group(2).isEmpty() ) {
            dataType.setColumnSizeUnit(ColumnSizeUnit.BYTE);
            dataType.setColumnSize(Integer.valueOf(matcher.group(2)));
            dataType.setDecimalDigits(Integer.valueOf(matcher.group(3)));
        }

        dataType.setDataTypeId(sqlTypeCode);
        return dataType;
    }
}
