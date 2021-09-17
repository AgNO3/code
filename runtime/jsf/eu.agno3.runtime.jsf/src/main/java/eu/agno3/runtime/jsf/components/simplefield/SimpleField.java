/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2013 by mbechler
 */
package eu.agno3.runtime.jsf.components.simplefield;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.api.InputHolder;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.context.RequestContext;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.util.ComponentUtils;

import eu.agno3.runtime.jsf.components.ResettableComponent;


/**
 * @author mbechler
 * 
 */
public class SimpleField extends UINamingContainer implements ResettableComponent {

    private static final String INPUT_ID = "inputId"; //$NON-NLS-1$
    private static final String STYLE_CLASS = "styleClass"; //$NON-NLS-1$
    private static final String ONLY_IF = "onlyIf"; //$NON-NLS-1$
    private static final String INHERITED_DEFAULT = "inheritedDefault"; //$NON-NLS-1$
    private static final String ENFORCED_VALUE = "enforced"; //$NON-NLS-1$
    private static final String READ_ONLY = "readOnly"; //$NON-NLS-1$
    private static final String READ_ONLY_VALUE_MAPPER = "readOnlyValueMapper"; //$NON-NLS-1$

    private static final String VALUE = "value"; //$NON-NLS-1$
    private static final String EDIT_MODE = "editMode"; //$NON-NLS-1$
    private static final String COLLECTION = "collection"; //$NON-NLS-1$
    private static final Object MAP = "map"; //$NON-NLS-1$

    private static final String CLONE = "clone"; //$NON-NLS-1$
    private static final String OUTPUT_STYLE = "outputStyle"; //$NON-NLS-1$
    private static final Object DESCRIPTION = "description"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        this.getStateHelper().remove(EDIT_MODE);
        return true;
    }


    /**
     * @return whether to show the field
     */
    public boolean shouldShow () {
        return getAttributes().get(ONLY_IF) == null || (boolean) getAttributes().get(ONLY_IF);
    }


    /**
     * @return whether a inherited default value exists
     */
    public boolean hasInheritedDefaultValue () {
        Object object = getInheritedValue();
        return object != null && !isEmptyCollection(object) && !isEmptyMap(object);
    }


    /**
     * 
     * @return the inherited value
     */
    public Object getInheritedValue () {
        return getAttributes().get(INHERITED_DEFAULT);
    }


    /**
     * @return whether an enforced value exists
     */
    public boolean hasEnforcedValue () {
        Object enforced = getEnforcedValue();
        return enforced != null && !isEmptyCollection(enforced) && !isEmptyMap(enforced);
    }


    /**
     * @return the description value
     */
    public String getDescriptionValue () {
        return (String) getAttributes().get(DESCRIPTION);
    }


    /**
     * 
     * @return the enforced value
     */
    public Object getEnforcedValue () {
        return getAttributes().get(ENFORCED_VALUE);
    }


    /**
     * @return whether the inherited default value is currently applied
     */
    public boolean isInheritedDefaultValue () {
        Object obj = getLocalValue();
        return ( obj == null || isEmptyCollection(obj) || isEmptyMap(obj) ) && this.hasInheritedDefaultValue();
    }


    /**
     * 
     * @return the local value
     */
    public Object getLocalValue () {
        return getAttributes().get(VALUE);
    }


    /**
     * 
     * @param val
     */
    protected void setLocalValue ( Object val ) {
        getValueExpression(VALUE).setValue(FacesContext.getCurrentInstance().getELContext(), val);
    }


    /**
     * 
     * @return whether the value is a collection
     */
    public boolean isCollectionValued () {
        return (boolean) getAttributes().getOrDefault(COLLECTION, false);
    }


    /**
     * 
     * @return whether the value is a map
     */
    public boolean isMapValued () {
        return (boolean) getAttributes().getOrDefault(MAP, false);
    }


    protected static boolean isEmptyCollection ( Object obj ) {
        return obj instanceof Collection && ( (Collection<?>) obj ).isEmpty();
    }


    /**
     * @param obj
     * @return
     */
    protected static boolean isEmptyMap ( Object obj ) {
        return obj instanceof Map && ( (Map<?, ?>) obj ).isEmpty();
    }


    /**
     * Resets the value to the current default value
     */
    public void resetToDefault () {
        this.setLocalValue(null);
        this.getStateHelper().put(EDIT_MODE, false);
    }


    /**
     * Start editing the value
     */
    public void doEdit () {
        this.getStateHelper().put(EDIT_MODE, true);
        Object inherited = getInheritedValue();

        if ( inherited instanceof Collection<?> ) {
            inherited = cloneList((Collection<?>) inherited);
        }
        else if ( inherited instanceof Map<?, ?> ) {
            inherited = cloneMap((Map<?, ?>) inherited);
        }
        else {
            inherited = cloneValue(inherited);
        }
        setLocalValue(inherited);
    }


    /**
     * @param inherited
     * @return
     */
    private Object cloneMap ( Map<?, ?> inherited ) {
        Map<Object, Object> cloned = new HashMap<>();

        for ( Entry<?, ?> obj : inherited.entrySet() ) {
            Entry<?, ?> clonedItem = (Entry<?, ?>) this.cloneValue(obj);
            cloned.put(clonedItem.getKey(), clonedItem.getValue());
        }

        return cloned;
    }


    /**
     * @param inherited
     * @return
     */
    private Object cloneList ( Collection<?> inherited ) {
        Collection<Object> cloned = null;
        if ( inherited instanceof List<?> ) {
            cloned = new LinkedList<>();
        }
        else if ( inherited instanceof Set<?> ) {
            cloned = new HashSet<>();
        }
        else {
            throw new FacesException("Unhandled collection type " + inherited.getClass().getName()); //$NON-NLS-1$
        }

        for ( Object obj : inherited ) {
            cloned.add(this.cloneValue(obj));
        }

        return cloned;
    }


    /**
     * @param obj
     * @return
     */
    protected Object cloneValue ( Object obj ) {
        MethodExpression cloneMethod = getCloneMethod();

        if ( cloneMethod == null ) {
            return obj;
        }

        return cloneMethod.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
            obj
        });
    }


    private MethodExpression getReadOnlyValueMapperMethod () {
        return (MethodExpression) getAttributes().get(READ_ONLY_VALUE_MAPPER);
    }


    protected MethodExpression getCloneMethod () {
        return (MethodExpression) getAttributes().get(CLONE);
    }


    /**
     * @param val
     * @return the mapped read only value
     */
    public Object mapReadOnlyValue ( Object val ) {
        MethodExpression roValueMapper = getReadOnlyValueMapperMethod();

        if ( roValueMapper != null ) {
            return doMapReadOnlyValue(val, roValueMapper);
        }

        return val;
    }


    /**
     * @param val
     * @return the value as collection, or a safe empty list of not a collection
     */
    @SuppressWarnings ( "unchecked" )
    public Collection<Object> toCollection ( Object val ) {
        if ( val instanceof Collection ) {
            return (Collection<Object>) val;
        }

        return Collections.EMPTY_LIST;
    }


    /**
     * @param val
     * @return val as map
     */
    @SuppressWarnings ( "unchecked" )
    public Map<Object, Object> toMap ( Object val ) {
        if ( val instanceof Map ) {
            return (Map<Object, Object>) val;
        }
        return Collections.EMPTY_MAP;
    }


    private static Object doMapReadOnlyValue ( Object val, MethodExpression roValueMapper ) {
        return roValueMapper.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {
            val
        });
    }


    /**
     * 
     * @param vals
     * @return mapped values
     */
    public Map<Object, Object> mapReadOnlyMap ( Map<Object, Object> vals ) {
        MethodExpression roValueMapper = getReadOnlyValueMapperMethod();

        if ( roValueMapper != null && vals != null ) {
            Map<Object, Object> mapped = new HashMap<>();
            for ( Entry<?, ?> val : vals.entrySet() ) {
                Entry<?, ?> m = (Entry<?, ?>) doMapReadOnlyValue(val, roValueMapper);
                mapped.put(m.getKey(), m.getValue());
            }
            return mapped;
        }
        return vals;
    }


    /**
     * @param vals
     * @return a mapped read only collection value
     */
    public Collection<Object> mapReadOnlyCollection ( Collection<Object> vals ) {
        MethodExpression roValueMapper = getReadOnlyValueMapperMethod();

        if ( roValueMapper != null ) {
            List<Object> res = new LinkedList<>();
            for ( Object val : vals ) {
                res.add(doMapReadOnlyValue(val, roValueMapper));
            }
            return res;
        }

        return vals;
    }


    /**
     * 
     * @return whether this field is currently in edit-mode
     */
    public boolean isInEditMode () {
        Object res = this.getStateHelper().get(EDIT_MODE);
        return res != null && (boolean) res;
    }


    /**
     * 
     * @return whether a edit field should be shown to the user
     */
    public boolean showEditField () {
        return !this.showReadOnly() && ( this.isInEditMode() || !this.isInheritedDefaultValue() );
    }


    /**
     * 
     * @return whether the default value should be shown to the user
     */
    public boolean showDefaultField () {
        return !this.showEditField() && !this.showReadOnly();
    }


    /**
     * @return whether to show this field as read only
     */
    public boolean showReadOnly () {
        return internalIsReadOnly() || this.hasEnforcedValue();
    }


    /**
     * 
     * @return whether this is a read only view
     */
    public boolean internalIsReadOnly () {
        return (boolean) getAttributes().get(READ_ONLY);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void encodeBegin ( FacesContext ctx ) throws IOException {
        if ( shouldShow() ) {
            @SuppressWarnings ( "resource" )
            ResponseWriter wr = ctx.getResponseWriter();
            wr.startElement("div", this); //$NON-NLS-1$

            String styleClass = (String) this.getAttributes().get(STYLE_CLASS);
            String style = "field simple"; //$NON-NLS-1$
            if ( styleClass != null ) {
                style = style + " " + styleClass;
            }
            wr.writeAttribute("class", style, STYLE_CLASS);

            // encode label
            this.encodeLabel(ctx, wr, this, (String) this.getAttributes().get(INPUT_ID), getLabelValue());

            super.encodeBegin(ctx);
        }
    }


    protected String getLabelValue () {
        return (String) this.getAttributes().get("label"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void encodeChildren ( FacesContext ctx ) throws IOException {
        if ( shouldShow() ) {
            @SuppressWarnings ( "resource" )
            ResponseWriter wr = ctx.getResponseWriter();

            if ( showEditField() || showDefaultField() ) {
                super.encodeChildren(ctx);
            }
            else {
                wr.startElement("div", this);
                wr.writeAttribute("class", "inputComponents", null);
                wr.startElement("div", this);
                if ( showReadOnly() ) {
                    this.encodeReadOnly(ctx);
                }
                else if ( showDefaultField() ) {
                    this.encodeDefaultField(ctx);
                }
                wr.endElement("div");
                wr.endElement("div");
            }

        }
    }


    /**
     * @param ctx
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    private void encodeDefaultField ( FacesContext ctx ) throws IOException {
        @SuppressWarnings ( "resource" )
        ResponseWriter wr = ctx.getResponseWriter();
        writeReadOnlyValue(ctx, wr, "i", this.getInheritedValue());

        // encode edit button
    }


    /**
     * @param ctx
     * @param enforced
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    private void encodeReadOnly ( FacesContext ctx ) throws IOException {
        @SuppressWarnings ( "resource" )
        ResponseWriter wr = ctx.getResponseWriter();

        String wrapElement;
        Object value;

        if ( this.hasEnforcedValue() ) {
            wrapElement = "b";
            value = this.getEnforcedValue();
        }
        else {
            wrapElement = "i";
            Object localValue = this.getLocalValue();
            if ( localValue != null ) {
                value = localValue;
            }
            else {
                value = this.getInheritedValue();
            }
        }

        writeReadOnlyValue(ctx, wr, wrapElement, value);
    }


    private void writeReadOnlyValue ( FacesContext ctx, ResponseWriter wr, String wrapElement, Object value ) throws IOException {

        Converter converter = ComponentUtils.getConverter(ctx, this);

        if ( this.isCollectionValued() ) {
            writeCollectionROValue(ctx, wr, wrapElement, value, converter);
        }
        else if ( this.isMapValued() ) {
            writeMapROValue(ctx, wr, wrapElement, value, converter);
        }
        else {
            writeSimpleROValue(ctx, wr, wrapElement, value, converter);
        }
    }


    /**
     * @param ctx
     * @param wr
     * @param wrapElement
     * @param value
     * @param converter
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    private void writeSimpleROValue ( FacesContext ctx, ResponseWriter wr, String wrapElement, Object value, Converter converter ) throws IOException {
        wr.startElement(wrapElement, this);
        String outputStyle = (String) this.getAttributes().get(OUTPUT_STYLE);
        if ( outputStyle != null ) {
            wr.writeAttribute("style", outputStyle, OUTPUT_STYLE);
        }
        Object mapped = this.mapReadOnlyValue(value);

        if ( converter != null ) {
            wr.writeText(converter.getAsString(ctx, this, mapped), null);
        }
        else if ( value != null ) {
            wr.writeText(mapped, null);
        }
        wr.endElement(wrapElement);
    }


    /**
     * @param ctx
     * @param wr
     * @param wrapElement
     * @param value
     * @param converter
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    private void writeMapROValue ( FacesContext ctx, ResponseWriter wr, String wrapElement, Object value, Converter converter ) throws IOException {

        boolean converterLoaded = false;
        Converter keyConverter = null;

        wr.startElement("dl", this);
        wr.writeAttribute("class", "readOnly", null);
        Map<Object, Object> values = this.mapReadOnlyMap(this.toMap(value));
        for ( Entry<Object, Object> val : values.entrySet() ) {

            if ( !converterLoaded ) {
                keyConverter = this.getKeyConverter(ctx, val.getKey().getClass());
                converterLoaded = true;
            }

            String outputStyle = (String) this.getAttributes().get(OUTPUT_STYLE);
            wr.startElement("dt", this);
            wr.startElement(wrapElement, this);

            if ( outputStyle != null ) {
                wr.writeAttribute("style", outputStyle, OUTPUT_STYLE);
            }
            if ( keyConverter != null ) {
                wr.writeText(keyConverter.getAsString(ctx, this, val.getKey()), null);
            }
            else {
                wr.writeText(val.getKey(), null);
            }
            wr.endElement(wrapElement);
            wr.endElement("dt");

            wr.startElement("dd", this);
            wr.startElement(wrapElement, this);
            if ( outputStyle != null ) {
                wr.writeAttribute("style", outputStyle, OUTPUT_STYLE);
            }
            if ( converter != null ) {
                wr.writeText(converter.getAsString(ctx, this, val.getValue()), null);
            }
            else {
                wr.writeText(val.getValue(), null);
            }

            wr.endElement(wrapElement);
            wr.endElement("dd");
        }

        // add values
        wr.endElement("dl");
    }


    /**
     * @param class1
     * @return
     */
    private Converter getKeyConverter ( FacesContext context, Class<? extends Object> keyType ) {
        Converter converter = (Converter) getAttributes().get("keyConverter"); //$NON-NLS-1$
        if ( converter != null ) {
            return converter;
        }

        if ( keyType == null || keyType == Object.class ) {
            // no conversion is needed
            return null;
        }

        if ( keyType == String.class && !RequestContext.getCurrentInstance().getApplicationContext().getConfig().isStringConverterAvailable() ) {
            return null;
        }
        return context.getApplication().createConverter(keyType);
    }


    /**
     * @param ctx
     * @param wr
     * @param wrapElement
     * @param value
     * @param converter
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    private void writeCollectionROValue ( FacesContext ctx, ResponseWriter wr, String wrapElement, Object value, Converter converter )
            throws IOException {
        wr.startElement("ul", this);
        wr.writeAttribute("class", "readOnly", null);
        Collection<Object> values = this.mapReadOnlyCollection(this.toCollection(value));
        for ( Object val : values ) {
            wr.startElement("li", this);
            wr.startElement(wrapElement, this);
            String outputStyle = (String) this.getAttributes().get(OUTPUT_STYLE);
            if ( outputStyle != null ) {
                wr.writeAttribute("style", outputStyle, OUTPUT_STYLE);
            }
            if ( converter != null ) {
                wr.writeText(converter.getAsString(ctx, this, val), null);
            }
            else {
                wr.writeText(val, null);
            }

            wr.endElement(wrapElement);
            wr.endElement("li");
        }

        // add values
        wr.endElement("ul");
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
     */
    @Override
    @SuppressWarnings ( "nls" )
    public void encodeEnd ( FacesContext ctx ) throws IOException {
        if ( shouldShow() ) {
            @SuppressWarnings ( "resource" )
            ResponseWriter wr = ctx.getResponseWriter();
            super.encodeEnd(ctx);
            wr.endElement("div");
        }
    }


    // Copied from primefaces OutputLabelRenderer
    @SuppressWarnings ( "nls" )
    protected void encodeLabel ( FacesContext ctx, ResponseWriter writer, UIComponent comp, String labelFor, String value ) throws IOException {
        UIComponent target = null;
        String targetClientId = null;
        UIInput input = null;
        String styleClass = OutputLabel.STYLE_CLASS + " fieldLabel";

        if ( labelFor != null ) {
            target = SearchExpressionFacade.resolveComponent(ctx, comp, labelFor);
            targetClientId = ( target instanceof InputHolder ) ? ( (InputHolder) target ).getInputClientId() : target.getClientId(ctx);

            if ( target instanceof UIInput ) {
                input = (UIInput) target;

                setLabelAttribute(value, input);

                if ( !input.isValid() ) {
                    styleClass = styleClass + " ui-state-error";
                }
            }
        }

        writeLabel(writer, comp, value, target, targetClientId, input, styleClass);
    }


    /**
     * @param writer
     * @param comp
     * @param value
     * @param target
     * @param targetClientId
     * @param input
     * @param styleClass
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    protected void writeLabel ( ResponseWriter writer, UIComponent comp, String value, UIComponent target, String targetClientId, UIInput input,
            String styleClass ) throws IOException {
        writer.startElement("label", comp);
        writer.writeAttribute("id", comp.getClientId(), "id");
        writer.writeAttribute("class", styleClass, "id");

        if ( target != null ) {
            writer.writeAttribute("for", targetClientId, "for");
        }

        if ( value != null ) {
            writer.startElement("span", this);
            writer.writeAttribute("class", "label-text", null);
            writer.writeText(value, "label");
            writer.endElement("span");
        }

        writeExtraLabel(writer, comp, input);

        writer.endElement("label");
    }


    @SuppressWarnings ( "nls" )
    protected void writeExtraLabel ( ResponseWriter writer, UIComponent comp, UIInput input ) throws IOException {
        if ( input != null && input.isRequired() ) {
            writer.startElement("span", comp);
            writer.writeAttribute("class", OutputLabel.REQUIRED_FIELD_INDICATOR_CLASS, null);
            writer.write("*");
            writer.endElement("span");
        }

        String description = this.getDescriptionValue();

        if ( !StringUtils.isBlank(description) ) {
            writer.startElement("div", this); //$NON-NLS-1$
            writer.writeAttribute("class", "field-description", null);
            writer.startElement("span", this);
            writer.writeAttribute("class", "field-description-icon ui-icon ui-icon-help", null);
            writer.endElement("span");
            writer.startElement("p", this);
            writer.writeText(description, null);
            writer.endElement("p");
            writer.endElement("div"); //$NON-NLS-1$
        }
    }


    /**
     * @param value
     * @param input
     */
    @SuppressWarnings ( "nls" )
    protected static void setLabelAttribute ( String value, UIInput input ) {
        if ( value != null && ( input.getAttributes().get("label") == null || input.getValueExpression("label") == null ) ) {
            String labelString = value;
            int colonPos = labelString.lastIndexOf(':');

            if ( colonPos != -1 ) {
                labelString = labelString.substring(0, colonPos);
            }

            input.getAttributes().put("label", labelString);
        }
    }

}
