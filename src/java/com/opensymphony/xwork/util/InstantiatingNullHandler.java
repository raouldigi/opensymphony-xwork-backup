/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;

import com.opensymphony.xwork.ObjectFactory;

import ognl.NullHandler;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Normally does nothing, but if {@link #CREATE_NULL_OBJECTS} is in the action context
 * with a value of true, then this class will attempt to create null objects when Ognl
 * requests null objects be created.
 * <p/>
 * The following rules are used:
 * <ul>
 * <li>If the null property is a simple bean with a no-arg constructor, it will simply be
 * created using ObjectFactory's {@link ObjectFactory#buildBean(java.lang.Class) buildBean} method.</li>
 * <li>If the property is declared <i>exactly</i> as a {@link Collection} or {@link List}, then this class
 * will look in the conversion property file (see {@link XWorkConverter}) for an entry
 * with a key of the form "Collection_[propertyName]". Using the value of this key as
 * the class type in which the collection will be holding, an {@link XWorkList} will be
 * created, allowing simple dynamic insertion.</li>
 * <li>If the property is declared as a {@link Map}, then the same rules are applied for
 * list, except that an {@link XWorkMap} will be created instead.</li>
 * </ul>
 *
 * @author Matt Ho
 * @author Patrick Lightbody
 */
public class InstantiatingNullHandler implements NullHandler {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final String CREATE_NULL_OBJECTS = "xwork.NullHandler.createNullObjects";
    private static final Log LOG = LogFactory.getLog(InstantiatingNullHandler.class);

    //~ Methods ////////////////////////////////////////////////////////////////

    public Object nullMethodResult(Map context, Object target, String methodName, Object[] args) {
        return null;
    }

    public Object nullPropertyValue(Map context, Object target, Object property) {
        Boolean create = (Boolean) context.get(CREATE_NULL_OBJECTS);
        boolean c = ((create == null) ? false : create.booleanValue());

        if (!c) {
            return null;
        }

        if ((target == null) || (property == null)) {
            return null;
        }

        try {
            Class clazz = null;
            Object o = target;

            if (target instanceof CompoundRoot) {
                CompoundRoot root = (CompoundRoot) target;

                for (Iterator iterator = root.iterator(); iterator.hasNext();) {
                    o = iterator.next();

                    if (OgnlRuntime.hasSetProperty((OgnlContext) context, o, property.toString())) {
                        clazz = OgnlRuntime.getPropertyDescriptor(o.getClass(), property.toString()).getPropertyType();

                        break;
                    }
                }
            } else {
                clazz = OgnlRuntime.getPropertyDescriptor(o.getClass(), property.toString()).getPropertyType();
            }

            if (clazz == null) {
                // can't do much here!
                return null;
            }

            Object param = createObject(clazz, o, property.toString());

            Ognl.setValue(property.toString(), context, o, param);

            //OgnlRuntime.setProperty((OgnlContext) context, target, property.toString(), param);
            //method.invoke(target, new Object[]{param});
            return param;
        } catch (Exception e) {
            LOG.error("Could not create and/or set value back on to object", e);
        }

        return null;
    }

    protected Class getCollectionType(Class clazz, String property) {
        return (Class) XWorkConverter.getInstance().getConverter(clazz, "Collection_" + property);
    }

    private Object createObject(Class clazz, Object target, String property) throws Exception {
        if ((clazz == Collection.class) || (clazz == List.class)) {
            Class collectionType = getCollectionType(target.getClass(), property);

            if (collectionType == null) {
                return null;
            }

            return new XWorkList(collectionType);
        } else if (clazz == Map.class) {
            Class collectionType = getCollectionType(target.getClass(), property);

            if (collectionType == null) {
                return null;
            }

            return new XWorkMap(collectionType);
        }

        return ObjectFactory.getObjectFactory().buildBean(clazz);
    }
}
