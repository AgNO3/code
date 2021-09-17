/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Validator;

import org.apache.log4j.Logger;
import org.hibernate.validator.path.PropertyNode;

import eu.agno3.runtime.validation.ValidatorContext.ValidatorContextEntry;


/**
 * @author mbechler
 *
 */
public class ConditionalValidator implements ConstraintValidator<ValidConditional, Object> {

    private static final Logger log = Logger.getLogger(ConditionalValidator.class);

    private ExpressionFactory ef;

    private String when;


    /**
     * @param ef
     */
    public ConditionalValidator ( ExpressionFactory ef ) {
        this.ef = ef;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidConditional vc ) {
        this.when = vc.when();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( Object o, ConstraintValidatorContext ctx ) {
        ctx.disableDefaultConstraintViolation();
        if ( o == null ) {
            return true;
        }
        List<Node> p = extractPath(ctx);
        ValidatorContextEntry vctx = ValidatorContext.getInstance().top();

        if ( p == null || p.size() < 2 || vctx == null ) {
            log.warn("Invalid call"); //$NON-NLS-1$
            return true;
        }

        Object parent = getValue(p.get(p.size() - 2));
        boolean tv = evaluateCondition(parent);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Conditional [%s==%s] validation on %s:%s (groups: %s)", //$NON-NLS-1$
                this.when,
                tv,
                o.getClass().getName(),
                p,
                vctx.getGroups() == null ? "[]" : Arrays.toString(vctx.getGroups()))); //$NON-NLS-1$
        }

        if ( !tv ) {
            return true;
        }

        return doNestedValidation(o, ctx, p, vctx);
    }


    /**
     * @param o
     * @param ctx
     * @param p
     * @param vctx
     * @return
     */
    private static boolean doNestedValidation ( Object o, ConstraintValidatorContext ctx, List<Node> p, ValidatorContextEntry vctx ) {
        Validator validator = vctx.getValidator();

        Set<ConstraintViolation<Object>> violations = validator.validate(o, vctx.getGroups());
        boolean haveViolation = false;
        for ( ConstraintViolation<Object> cv : violations ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Violation on %s.%s: %s", p, cv.getPropertyPath(), cv.getMessage())); //$NON-NLS-1$
            }
            ConstraintViolationBuilder cvb = ctx.buildConstraintViolationWithTemplate(cv.getMessageTemplate());
            addParentPath(p, cvb);
            addParentPath(toNodes(cv.getPropertyPath()), cvb);
            cvb.addConstraintViolation();
            haveViolation = true;
        }

        return !haveViolation;
    }


    /**
     * @param parent
     * @return
     */
    private boolean evaluateCondition ( Object parent ) {
        ELContext ec = new ValidationELContext(parent);
        Object testValue;
        try {
            ValueExpression ve = this.ef.createValueExpression(ec, this.when, Object.class);
            testValue = ve.getValue(ec);
        }
        catch ( Exception e ) {
            log.error("Failed to get test condition value", e); //$NON-NLS-1$
            return false;
        }
        return isTrue(testValue);
    }


    /**
     * @param testValue
     * @return
     */
    private static boolean isTrue ( Object testValue ) {
        if ( testValue == null ) {
            return false;
        }

        if ( Boolean.class == testValue.getClass() || boolean.class == testValue.getClass() ) {
            return (boolean) testValue;
        }

        return false;
    }


    /**
     * @param node
     * @return
     */
    private static Object getValue ( Node node ) {
        if ( node instanceof PropertyNode ) {
            return ( (PropertyNode) node ).getValue();
        }
        return null;
    }


    /**
     * @param p
     * @param cvb
     */
    private static void addParentPath ( List<Node> p, ConstraintViolationBuilder cvb ) {
        for ( Node n : p ) {
            switch ( n.getKind() ) {
            case BEAN:
                cvb.addBeanNode();
                break;

            case PROPERTY:
                cvb.addPropertyNode(n.getName());
                break;

            default:
                log.debug("Unhandled node type " + n.getKind()); //$NON-NLS-1$
                break;
            }
        }
    }


    /**
     * @param ctx
     */
    private static final List<Node> extractPath ( ConstraintValidatorContext ctx ) {
        if ( !"org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl".equals(ctx.getClass().getName()) ) { //$NON-NLS-1$
            log.warn("Not a hibernate constraint validator " + ctx.getClass().getName()); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        try {
            Field f = ctx.getClass().getDeclaredField("basePath"); //$NON-NLS-1$
            f.setAccessible(true);
            Path p = (Path) f.get(ctx);
            return toNodes(p);
        }
        catch (
            NoSuchFieldException |
            ClassCastException |
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException e ) {
            log.warn("Field basePath does not accessible", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * @param p
     * @return
     */
    private static List<Node> toNodes ( Path p ) {
        if ( p == null ) {
            return Collections.EMPTY_LIST;
        }

        List<Node> nodes = new ArrayList<>();
        for ( Node n : p ) {
            nodes.add(n);
        }
        return nodes;
    }

}
