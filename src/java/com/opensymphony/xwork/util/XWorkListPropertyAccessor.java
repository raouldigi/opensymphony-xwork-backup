/*
 * Copyright (c) 2005 Opensymphony. All Rights Reserved.
 */
package com.opensymphony.xwork.util;

import com.opensymphony.xwork.ObjectFactory;
import ognl.ListPropertyAccessor;
import ognl.OgnlException;

import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Zimmerman
 *
 * Overrides the list property accessor so in the case of trying
 * to add properties of a given bean and the JavaBean is not present,
 * this class will create the necessary blank JavaBeans.
 */
public class XWorkListPropertyAccessor extends ListPropertyAccessor {
	
	/* (non-Javadoc)
	 * @see ognl.PropertyAccessor#getProperty(java.util.Map, java.lang.Object, java.lang.Object)
	 */
	public Object getProperty(Map context, Object target, Object name)
			throws OgnlException {
		
		//System.out.println("Entering XWorkListPropertyAccessor. Name: " + name);
		if (name instanceof Number
				&& context.get(InstantiatingNullHandler.CREATE_NULL_OBJECTS)!=null){
					
			//System.out.println("Getting index from List");
			List list=(List)target;
			int index=((Number)name).intValue();
			int listSize=list.size();
			if (listSize<=index) {
				//System.out.println("adding objects to list.");
				Object result=null;
				Class lastClass=(Class)context.get(XWorkConverter.LAST_BEAN_CLASS_ACCESSED);
				String lastProperty=(String)context.get(XWorkConverter.LAST_BEAN_PROPERTY_ACCESSED);
				Class beanClass=XWorkConverter.getInstance()
					.getObjectTypeDeterminer().getElementClass(lastClass,lastProperty,name);
				for (int i=listSize; i<index; i++) {
					
					list.add(i, null);

				}
				try {
					list.add(index, result=ObjectFactory.getObjectFactory().buildBean(beanClass));
				}	catch (Exception exc) {
					
				}
				return result;
			}	else if (list.get(index)==null) {
				
			}
		}
		return super.getProperty(context, target, name);
	}
	
	
	/* (non-Javadoc)
	 * @see ognl.PropertyAccessor#setProperty(java.util.Map, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public void setProperty(Map context, Object target, Object name, Object value)
			throws OgnlException {
		
		Class lastClass=(Class)context.get(XWorkConverter.LAST_BEAN_CLASS_ACCESSED);
		String lastProperty=(String)context.get(XWorkConverter.LAST_BEAN_PROPERTY_ACCESSED);
		Class convertToClass=XWorkConverter.getInstance()
			.getObjectTypeDeterminer().getElementClass(lastClass,lastProperty,name);
		
		Object realValue=getRealValue(context, value, convertToClass);
		
		if (target instanceof List && name instanceof Number) {
			//make sure there are enough spaces in the List to set
			List list=(List)target;
			int listSize=list.size();
			int count=((Number)name).intValue();
			if (count>=listSize) {
				for (int i=listSize; i<=count; i++) {
					list.add(null);
				}
			}
		}
		
		super.setProperty(context, target, name, realValue);
	}
	
	private Object getRealValue(Map context, Object value, Class convertToClass) {
		if (value==null || convertToClass==null) {
			return value;
		}
		return XWorkConverter.getInstance().convertValue(context, value, convertToClass);
	}
}