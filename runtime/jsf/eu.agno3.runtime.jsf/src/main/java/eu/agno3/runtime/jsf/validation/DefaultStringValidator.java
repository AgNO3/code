/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.runtime.jsf.validation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
@FacesValidator ( DefaultStringValidator.VALIDATOR_ID )
public class DefaultStringValidator implements Validator {

    /**
     * 
     */
    public static final String VALIDATOR_ID = "stringDefaultValidator"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(DefaultStringValidator.class);

    static final Set<Integer> NEWLINE_CHARS = new HashSet<>(Arrays.asList(0x0A, 0x0B, 0x0C, 0x0D, 0x85, 0x2028, 0x2029));

    private static enum Flag {
        NO_NEWLINE, NO_CONTROL, NO_BINARY, NO_MULTI_SPACE, NO_TRAILING_SPACE, NO_LEADING_SPACE
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public void validate ( FacesContext ctx, UIComponent comp, Object val ) throws ValidatorException {

        if ( ! ( comp instanceof UIInput ) ) {
            log.debug("Not an ui component"); //$NON-NLS-1$
            return;
        }

        final Set<Flag> flags = getFlags(comp);
        final Collection<FacesMessage> messages = new ArrayList<>();

        if ( val instanceof String ) {
            String strval = (String) val;
            doCheck(flags, strval, messages);
        }
        else if ( val instanceof String[] ) {
            for ( String s : (String[]) val ) {
                doCheck(flags, s, messages);
            }
        }
        else if ( val instanceof Collection<?> ) {
            for ( Object s : (Collection<Object>) val ) {
                if ( s instanceof String ) {
                    doCheck(flags, (String) s, messages);
                }
            }
        }
    }


    /**
     * @param flags
     * @param strval
     * @param messages
     */
    protected void doCheck ( final Set<Flag> flags, String strval, final Collection<FacesMessage> messages ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Validating " + strval); //$NON-NLS-1$
        }

        if ( !strval.isEmpty() ) {
            if ( flags.contains(Flag.NO_LEADING_SPACE) && Character.isWhitespace(strval.charAt(0)) ) {
                String msg = BaseMessages.format("string.leadingSpace", strval.charAt(0)); //$NON-NLS-1$
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            }
            else if ( flags.contains(Flag.NO_TRAILING_SPACE) && Character.isWhitespace(strval.charAt(strval.length() - 1)) ) {
                String msg = BaseMessages.format("string.trailingSpace", strval.charAt(strval.length() - 1)); //$NON-NLS-1$
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            }
        }

        boolean valid = strval.codePoints().allMatch(

            new IntPredicate() {

                private boolean lastWasSpace;


                @Override
                public boolean test ( int cp ) {
                    if ( !Character.isValidCodePoint(cp) ) {
                        addMessage("string.invalid", cp); //$NON-NLS-1$
                        return false;
                    }

                    if ( cp == 0 && flags.contains(Flag.NO_BINARY) ) {
                        addMessage("string.binary", cp); //$NON-NLS-1$
                        return false;
                    }
                    else if ( flags.contains(Flag.NO_CONTROL)
                            && ( !NEWLINE_CHARS.contains(cp) && Character.isISOControl(cp) && !Character.isWhitespace(cp) ) ) {
                        addMessage("string.control", cp); //$NON-NLS-1$
                        return false;
                    }
                    else if ( flags.contains(Flag.NO_NEWLINE) && NEWLINE_CHARS.contains(cp) ) {
                        addMessage("string.newline", cp); //$NON-NLS-1$
                        return false;
                    }
                    else if ( flags.contains(Flag.NO_MULTI_SPACE) && this.lastWasSpace && Character.isWhitespace(cp) ) {
                        addMessage("string.multispace", cp); //$NON-NLS-1$
                        return false;
                    }
                    else if ( flags.contains(Flag.NO_MULTI_SPACE) && Character.isWhitespace(cp) ) {
                        this.lastWasSpace = true;
                    }
                    else if ( flags.contains(Flag.NO_MULTI_SPACE) ) {
                        this.lastWasSpace = false;
                    }
                    return true;
                }


                private void addMessage ( String key, int cp ) {
                    String msg = BaseMessages.format(key, cp);
                    messages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                }
            }

        );

        if ( !valid ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Invalid '%s' (flags: %s)", strval, flags)); //$NON-NLS-1$
            }
            throw new ValidatorException(messages);
        }
    }


    /**
     * @param comp
     * @return
     */
    private static Set<Flag> getFlags ( UIComponent comp ) {
        Set<Flag> flags = EnumSet.allOf(Flag.class);
        if ( comp instanceof HtmlInputTextarea ) {
            flags.remove(Flag.NO_NEWLINE);
            flags.remove(Flag.NO_MULTI_SPACE);
            flags.remove(Flag.NO_TRAILING_SPACE);
            flags.remove(Flag.NO_LEADING_SPACE);
        }
        return flags;
    }

}
