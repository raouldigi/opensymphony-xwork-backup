/*
 * Created on Nov 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.opensymphony.xwork.util;

import java.util.*;

import com.opensymphony.xwork.ObjectFactory;

import ognl.Evaluation;
import ognl.MapPropertyAccessor;
import ognl.Node;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.PropertyAccessor;

/**
 * Implementation of PropertyAccessor that sets and gets properties by storing and looking
 * up values in Maps.
 * @author Gabriel Zimmerman
 */
public class XWorkMapPropertyAccessor extends MapPropertyAccessor
{
	private static final String [] INDEX_ACCESS_PROPS=new String [] 
		{"size", "isEmpty", "keys", "values"};
	
	private static final XWorkConverter _converter=XWorkConverter.getInstance();
	
	public Object getProperty( Map context, Object target, Object name ) throws OgnlException
	{
		// if this is one of the regular index access
		// properties then just let the superclass deal with the 
		// get. 
		if (name instanceof String && contains(INDEX_ACCESS_PROPS, (String)name)) 
		{
			System.out.println("returning super.getProperty");
			return super.getProperty(context, target, name);
		}
			
		System.out.println("XWorkMapPropertyAccessor:getting map property");
		
		Object result=super.getProperty(context,target,name);
		System.out.println("Tried getting from superclass");
		if (result!=null) {System.out.println(result.toString());}
		if (result==null) 
		{
			System.out.println("result is null");
			//find the key class and convert the name to that class
			Class lastClass=(Class)context.get(XWorkConverter.LAST_BEAN_CLASS_ACCESSED);
			System.out.println("Last class: " + lastClass.toString());
			String lastProperty=(String)context.get(XWorkConverter.LAST_BEAN_PROPERTY_ACCESSED);
			System.out.println("Last property: " + lastProperty);
			Class keyClass=_converter.getObjectTypeDeterminer()
			.getKeyClass(lastClass,lastProperty);
			
			if(keyClass==null) {
				
				keyClass=java.lang.String.class;
			}
			System.out.println("key class: " + keyClass.getName());
			System.out.println("name: " + name);
			Object key=getKey(context, name);
			System.out.println("Key (class:value) - "
					+ key.getClass() + ":"+ key);
			Map map = (Map)target;
			result=map.get(key);
			
			System.out.println("Result: " + result);
			if (result==null && 
				context.get(InstantiatingNullHandler.CREATE_NULL_OBJECTS)!=null)
			{
				Class valueClass=_converter.getObjectTypeDeterminer().getElementClass(lastClass, lastProperty,key);
				System.out.println("Value class: " + valueClass);
				try {
					result=ObjectFactory.getObjectFactory().buildBean(valueClass);
					map.put(key, result);
				}	catch (Exception exc) {
					
				}
				
			}
		}
		System.out.println("Result: " + result);
		return result;
	}

	/**
	 * @param array
	 * @param name
	 * @return
	 */
	private boolean contains(String[] array, String name) 
	{
		for (int i=0; i<array.length; i++) {
			if (array[i].equals(name)) {
				return true;
			}
		}
		
		return false;
	}

	public void setProperty( Map context, Object target, Object name, Object value ) throws OgnlException
	{
		Object key=getKey(context, name);
		Map map = (Map)target;
		map.put( key, value );
	}
	
	private Object getKey(Map context, Object name) 
	{
		Class lastClass=(Class)context.get(XWorkConverter.LAST_BEAN_CLASS_ACCESSED);
		String lastProperty=(String)context.get(XWorkConverter.LAST_BEAN_PROPERTY_ACCESSED);		
		Class keyClass=_converter.getObjectTypeDeterminer()
			.getKeyClass(lastClass,lastProperty);
		if(keyClass==null) {
			keyClass=java.lang.String.class;
		}
		System.out.println("Converting object of name " + name + 
				" and class " + name.getClass().getName() + " to class "
				+ keyClass.getName());
		return _converter.convertValue(context, name, keyClass);
		
	}
}
