/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.11.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.autoapply.internal;


import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.StringDiff;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;


/**
 * @author mbechler
 *
 */
public class NoRemoveDiffWrapper extends DiffResult {

    private DiffResult delegate;


    /**
     * @param diff
     */
    public NoRemoveDiffWrapper ( DiffResult diff ) {
        super(null, null, null);
        this.delegate = diff;
    }


    /**
     * @param obj
     * @param differences
     * @see liquibase.diff.DiffResult#addChangedObject(liquibase.structure.DatabaseObject,
     *      liquibase.diff.ObjectDifferences)
     */
    @Override
    public void addChangedObject ( DatabaseObject obj, ObjectDifferences differences ) {
        this.delegate.addChangedObject(obj, differences);
    }


    /**
     * @param obj
     * @see liquibase.diff.DiffResult#addMissingObject(liquibase.structure.DatabaseObject)
     */
    @Override
    public void addMissingObject ( DatabaseObject obj ) {
        this.delegate.addMissingObject(obj);
    }


    /**
     * @param obj
     * @see liquibase.diff.DiffResult#addUnexpectedObject(liquibase.structure.DatabaseObject)
     */
    @Override
    public void addUnexpectedObject ( DatabaseObject obj ) {}


    /**
     * @throws DatabaseException
     * @throws IOException
     * @see liquibase.diff.DiffResult#areEqual()
     */
    @Override
    public boolean areEqual () throws DatabaseException, IOException {
        return this.delegate.areEqual();
    }


    /**
     * @param arg0
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object arg0 ) {
        return this.delegate.equals(arg0);
    }


    /**
     * @see liquibase.diff.DiffResult#getChangedObjects()
     */
    @Override
    public Map<DatabaseObject, ObjectDifferences> getChangedObjects () {
        return this.delegate.getChangedObjects();
    }


    /**
     * @param type
     * @param comparator
     * @see liquibase.diff.DiffResult#getChangedObjects(java.lang.Class, java.util.Comparator)
     */
    @Override
    public <T extends DatabaseObject> SortedMap<T, ObjectDifferences> getChangedObjects ( Class<T> type, Comparator<DatabaseObject> comparator ) {
        return this.delegate.getChangedObjects(type, comparator);
    }


    /**
     * @param arg0
     * @see liquibase.diff.DiffResult#getChangedObjects(java.lang.Class)
     */
    @Override
    public <T extends DatabaseObject> Map<T, ObjectDifferences> getChangedObjects ( Class<T> arg0 ) {
        return this.delegate.getChangedObjects(arg0);
    }


    /**
     * @see liquibase.diff.DiffResult#getCompareControl()
     */
    @Override
    public CompareControl getCompareControl () {
        return this.delegate.getCompareControl();
    }


    /**
     * @see liquibase.diff.DiffResult#getComparedTypes()
     */
    @Override
    public Set<Class<? extends DatabaseObject>> getComparedTypes () {
        return this.delegate.getComparedTypes();
    }


    /**
     * @see liquibase.diff.DiffResult#getComparisonSnapshot()
     */
    @Override
    public DatabaseSnapshot getComparisonSnapshot () {
        return this.delegate.getComparisonSnapshot();
    }


    /**
     * @see liquibase.diff.DiffResult#getMissingObjects()
     */
    @Override
    public Set<? extends DatabaseObject> getMissingObjects () {
        return this.delegate.getMissingObjects();
    }


    /**
     * @param type
     * @param comparator
     * @see liquibase.diff.DiffResult#getMissingObjects(java.lang.Class, java.util.Comparator)
     */
    @Override
    public <T extends DatabaseObject> SortedSet<T> getMissingObjects ( Class<T> type, Comparator<DatabaseObject> comparator ) {
        return this.delegate.getMissingObjects(type, comparator);
    }


    /**
     * @param arg0
     * @see liquibase.diff.DiffResult#getMissingObjects(java.lang.Class)
     */
    @Override
    public <T extends DatabaseObject> Set<T> getMissingObjects ( Class<T> arg0 ) {
        return this.delegate.getMissingObjects(arg0);
    }


    /**
     * @see liquibase.diff.DiffResult#getProductNameDiff()
     */
    @Override
    public StringDiff getProductNameDiff () {
        return this.delegate.getProductNameDiff();
    }


    /**
     * @see liquibase.diff.DiffResult#getProductVersionDiff()
     */
    @Override
    public StringDiff getProductVersionDiff () {
        return this.delegate.getProductVersionDiff();
    }


    /**
     * @see liquibase.diff.DiffResult#getReferenceSnapshot()
     */
    @Override
    public DatabaseSnapshot getReferenceSnapshot () {
        return this.delegate.getReferenceSnapshot();
    }


    /**
     * @see liquibase.diff.DiffResult#getUnexpectedObjects()
     */
    @Override
    public Set<? extends DatabaseObject> getUnexpectedObjects () {
        return Collections.EMPTY_SET;
    }


    /**
     * @param type
     * @param comparator
     * @see liquibase.diff.DiffResult#getUnexpectedObjects(java.lang.Class, java.util.Comparator)
     */
    @Override
    public <T extends DatabaseObject> SortedSet<T> getUnexpectedObjects ( Class<T> type, Comparator<DatabaseObject> comparator ) {
        return new TreeSet<>();
    }


    /**
     * @param arg0
     * @see liquibase.diff.DiffResult#getUnexpectedObjects(java.lang.Class)
     */
    @Override
    public <T extends DatabaseObject> Set<T> getUnexpectedObjects ( Class<T> arg0 ) {
        return Collections.EMPTY_SET;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    /**
     * @param productNameDiff
     * @see liquibase.diff.DiffResult#setProductNameDiff(liquibase.diff.StringDiff)
     */
    @Override
    public void setProductNameDiff ( StringDiff productNameDiff ) {
        this.delegate.setProductNameDiff(productNameDiff);
    }


    /**
     * @param productVersionDiff
     * @see liquibase.diff.DiffResult#setProductVersionDiff(liquibase.diff.StringDiff)
     */
    @Override
    public void setProductVersionDiff ( StringDiff productVersionDiff ) {
        this.delegate.setProductVersionDiff(productVersionDiff);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.delegate.toString();
    }

}
