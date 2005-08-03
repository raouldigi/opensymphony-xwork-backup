/*
 * Created on 6/11/2004
 */
package com.opensymphony.xwork.spring.interceptor;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.interceptor.PreResultListener;
import com.opensymphony.xwork.util.OgnlValueStack;

/**
 * @author Simon Stewart
 */
public class TestActionInvocation implements ActionInvocation {
	private Action action;
	private boolean executed;

	public TestActionInvocation(Action wrappedAction) {
		this.action = wrappedAction;
	}
	
	public Action getAction() {
		return action;
	}

	public boolean isExecuted() {
		return executed;
	}

	public ActionContext getInvocationContext() {
		return null;
	}

	public ActionProxy getProxy() {
		return null;
	}

	public Result getResult() throws Exception {
		return null;
	}

	public String getResultCode() {
		return null;
	}

    public void setResultCode(String resultCode) {
                                                   
    }

    public OgnlValueStack getStack() {
        return null;
    }

	public void addPreResultListener(PreResultListener listener) {
	}

	public String invoke() throws Exception {
		executed = true;
		return action.execute();
	}
}