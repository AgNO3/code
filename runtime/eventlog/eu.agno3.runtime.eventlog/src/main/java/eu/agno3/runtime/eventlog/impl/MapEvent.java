/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.impl;


import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import eu.agno3.runtime.eventlog.EventSeverity;
import eu.agno3.runtime.eventlog.EventWithProperties;


/**
 * @author mbechler
 *
 */
public class MapEvent extends AbstractMap<String, Object> implements EventWithProperties, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -653535196019623792L;

    /**
     * 
     */
    public static final String TIMESTAMP = "timestamp"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String ID = "_id"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String EXPIRE = "expire"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TYPE = "type"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String SEVERITY = "severity"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String AUDIT_STATUS = "audit_status"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String AUDIT_ACTION = "audit_action"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String DEDUP_KEY = "dedup_key"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String FIRST_SEEN = "first_seen"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String LAST_SEEN = "last_seen"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String COUNT_SEEN = "count_seen"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String MESSAGE = "message"; //$NON-NLS-1$

    private Map<String, Object> properties = new HashMap<>();


    /**
     * 
     */
    public MapEvent () {}


    private MapEvent ( Map<String, Object> data ) {
        this.properties = data;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getId()
     */
    @Override
    public String getId () {
        return (String) this.get(ID);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getDedupKey()
     */
    @Override
    public String getDedupKey () {
        return (String) this.get(DEDUP_KEY);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getSeverity()
     */
    @Override
    public EventSeverity getSeverity () {
        return EventSeverity.valueOf((String) this.get(SEVERITY));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return (String) this.get(TYPE);
    }


    /**
     * 
     * @return the message
     */
    public String getMessage () {
        return (String) this.get(MESSAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getTimestamp()
     */
    @Override
    public DateTime getTimestamp () {
        Long object = (Long) this.get(TIMESTAMP);
        if ( object == null ) {
            return null;
        }
        return new DateTime(object);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getExpiration()
     */
    @Override
    public DateTime getExpiration () {
        Long object = (Long) this.get(EXPIRE);
        if ( object == null ) {
            return null;
        }
        return new DateTime(object);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventWithProperties#getProperties()
     */
    @Override
    public Map<String, Object> getProperties () {
        return this.properties;
    }


    /**
     * @param key
     * @param val
     */
    @Override
    public Object put ( String key, Object val ) {
        return this.properties.put(key, val);
    }


    /**
     * @param key
     * @param val
     */
    @JsonAnySetter
    public void putJSON ( String key, Object val ) {
        this.put(key, val);
    }


    /**
     * 
     * @param key
     * @return the property value, null if not set
     */
    public Object get ( String key ) {
        return this.properties.get(key);
    }


    /**
     * 
     * @see java.util.Map#size()
     */
    @Override
    public int size () {
        return this.properties.size();
    }


    /**
     * 
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty () {
        return this.properties.isEmpty();
    }


    /**
     * @param key
     * 
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey ( Object key ) {
        return this.properties.containsKey(key);
    }


    /**
     * @param value
     * 
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue ( Object value ) {
        return this.properties.containsValue(value);
    }


    /**
     * @param key
     * 
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get ( Object key ) {
        return this.properties.get(key);
    }


    /**
     * @param key
     * 
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove ( Object key ) {
        return this.properties.remove(key);
    }


    /**
     * @param m
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll ( Map<? extends String, ? extends Object> m ) {
        this.properties.putAll(m);
    }


    /**
     * 
     * @see java.util.Map#clear()
     */
    @Override
    public void clear () {
        this.properties.clear();
    }


    /**
     * 
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet () {
        return this.properties.keySet();
    }


    /**
     * 
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values () {
        return this.properties.values();
    }


    /**
     * 
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet () {
        return this.properties.entrySet();
    }


    /**
     * @param o
     * 
     * @see java.util.Map#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object o ) {
        return this.properties.equals(o);
    }


    /**
     * 
     * @see java.util.Map#hashCode()
     */
    @Override
    public int hashCode () {
        return this.properties.hashCode();
    }


    /**
     * @param key
     * @param defaultValue
     * 
     * @see java.util.Map#getOrDefault(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getOrDefault ( Object key, Object defaultValue ) {
        return this.properties.getOrDefault(key, defaultValue);
    }


    /**
     * @param action
     * @see java.util.Map#forEach(java.util.function.BiConsumer)
     */
    @Override
    public void forEach ( BiConsumer<? super String, ? super Object> action ) {
        this.properties.forEach(action);
    }


    /**
     * @param function
     * @see java.util.Map#replaceAll(java.util.function.BiFunction)
     */
    @Override
    public void replaceAll ( BiFunction<? super String, ? super Object, ? extends Object> function ) {
        this.properties.replaceAll(function);
    }


    /**
     * @param key
     * @param value
     * 
     * @see java.util.Map#putIfAbsent(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object putIfAbsent ( String key, Object value ) {
        return this.properties.putIfAbsent(key, value);
    }


    /**
     * @param key
     * @param value
     * 
     * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean remove ( Object key, Object value ) {
        return this.properties.remove(key, value);
    }


    /**
     * @param key
     * @param oldValue
     * @param newValue
     * 
     * @see java.util.Map#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean replace ( String key, Object oldValue, Object newValue ) {
        return this.properties.replace(key, oldValue, newValue);
    }


    /**
     * @param key
     * @param value
     * 
     * @see java.util.Map#replace(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object replace ( String key, Object value ) {
        return this.properties.replace(key, value);
    }


    /**
     * @param key
     * @param mappingFunction
     * 
     * @see java.util.Map#computeIfAbsent(java.lang.Object, java.util.function.Function)
     */
    @Override
    public Object computeIfAbsent ( String key, Function<? super String, ? extends Object> mappingFunction ) {
        return this.properties.computeIfAbsent(key, mappingFunction);
    }


    /**
     * @param key
     * @param remappingFunction
     * 
     * @see java.util.Map#computeIfPresent(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public Object computeIfPresent ( String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction ) {
        return this.properties.computeIfPresent(key, remappingFunction);
    }


    /**
     * @param key
     * @param remappingFunction
     * 
     * @see java.util.Map#compute(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public Object compute ( String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction ) {
        return this.properties.compute(key, remappingFunction);
    }


    /**
     * @param key
     * @param value
     * @param remappingFunction
     * 
     * @see java.util.Map#merge(java.lang.Object, java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public Object merge ( String key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction ) {
        return this.properties.merge(key, value, remappingFunction);
    }


    /**
     * @param data
     * @return an event from the given data
     */
    public static MapEvent fromMap ( Map<String, Object> data ) {
        return new MapEvent(data);
    }

}
