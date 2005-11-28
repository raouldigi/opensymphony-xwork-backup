/*
 * Copyright (c) 2002-2005 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.interceptor.annotations;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.AnnotationUtils;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.interceptor.PreResultListener;

/**
 * <p>Invokes any annotated methods on the action. Specifically, it supports the following
 * annotations:
 * <ul>
 * <li> &#64;{@link Before} - will be invoked before the action method. If the returned value is not null, it is
 * returned as the action result code</li>
 * <li> &#64;{@link BeforeResult} - will be invoked after the action method but before the result execution</li>
 * <li> &#64;{@link After} - will be invoked after the action method and result execution</li>
 * </ul>
 * <p/>
 * <p>There can be multiple methods marked with the same annotations, but the order of their execution
 * is not guaranteed. However, the annotated methods on the superclass chain are guaranteed to be invoked before the
 * annotated method in the current class in the case of a {@link Before} annotations and after, if the annotations is
 * {@link After}.
 * <p/>
 * <p>Example</p>
 * <pre>
 *  public class BaseAnnotatedAction {
 *  	protected String log = "";
 * <p/>
 *  	&#64;Before
 *  	public String baseBefore() {
 *  		log = log + "baseBefore-";
 *  		return null;
 *  	}
 *  }
 * <p/>
 *  public class AnnotatedAction extends BaseAnnotatedAction {
 *  	&#64;Before
 *  	public String before() {
 *  		log = log + "before";
 *  		return null;
 *  	}
 * <p/>
 *  	public String execute() {
 *  		log = log + "-execute";
 *  		return Action.SUCCESS;
 *  	}
 * <p/>
 *  	&#64;BeforeResult
 *  	public void beforeResult() throws Exception {
 *  		log = log +"-beforeResult";
 *  	}
 * <p/>
 *  	&#64;After
 *  	public void after() {
 *  		log = log + "-after";
 *  	}
 *  }
 *  </pre>
 * <p/>
 * <p>With the interceptor applied and the action executed on <code>AnnotatedAction</code> the log
 * instance variable will contain <code>baseBefore-before-execute-beforeResult-after</code>.
 *
 * @author Zsolt Szasz, zsolt at lorecraft dot com
 */
public class AnnotationWorkflowInterceptor implements Interceptor, PreResultListener {

    /**
     * Discovers annotated methods on the action and calls them according to the workflow
     *
     * @see com.opensymphony.xwork.interceptor.Interceptor#intercept(com.opensymphony.xwork.ActionInvocation)
     */
    public String intercept(ActionInvocation invocation) throws Exception {
        final Object action = invocation.getAction();
        invocation.addPreResultListener(this);
        for (Method m : AnnotationUtils.findAnnotatedMethods(action.getClass(), Before.class)) {
            // action superclass methods first then action methods
            final String resultCode = (String) m.invoke(action, (Object[]) null);
            if (resultCode != null) {
                // shortcircuit execution
                return resultCode;
            }
        }

        String invocationResult = invocation.invoke();

        // invoke any @After methods
        List<Method> list = AnnotationUtils.findAnnotatedMethods(action.getClass(), After.class);
        // action methods first then action superclass methods
        Collections.reverse(list);
        for (Method m : list) {
            m.invoke(action, (Object[]) null);
        }

        return invocationResult;
    }

    public void destroy() {
    }

    public void init() {
    }

    /**
     * Invokes any &#64;BeforeResult annotated methods
     *
     * @see com.opensymphony.xwork.interceptor.PreResultListener#beforeResult(com.opensymphony.xwork.ActionInvocation, String)
     */
    public void beforeResult(ActionInvocation invocation, String resultCode) {
        Object action = invocation.getAction();
        List<Method> methods = AnnotationUtils.findAnnotatedMethods(action.getClass(),
                BeforeResult.class);
        for (Method m : methods) {
            try {
                m.invoke(action, (Object[]) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}