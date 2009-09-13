package org.kisst.cordys.as400.pcml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.ReflectionUtil;
import org.kisst.cordys.util.convert.Convertor;

import com.eibus.xml.nom.Node;
import com.ibm.as400.access.AS400DataType;

public abstract class PcmlDataElement extends PcmlElement {
	protected final String defaultValue;
	private final boolean trim;
	private final Convertor convertor;

	protected PcmlDataElement(CompilationContext context, PcmlStruct parent, int dataNode, AS400DataType elementType)
    {
		super(context, parent, dataNode, elementType);
		String init = Node.getAttribute(dataNode, "init");
		String emptyValue = Node.getAttribute(dataNode, "emptyValue");
		if (init!=null && emptyValue!=null)
			throw new RuntimeException("data parameter "+getName()+" has both an init value and and emptyValue");
		if (emptyValue!=null) 
			defaultValue = emptyValue;
		else
			defaultValue = init;
		
		// default behaviour is to trim strings, because leading/trailing spaces and XML
		// do not mix that well
		// TODO: maybe default should be false
		trim = NomUtil.getBooleanAttribute(dataNode, "trim", true);
		if (isInput() && isOptional() && ! hasDefaultValue()) {
			throw new RuntimeException("input data parameter "+getName()+" is optional but does not have any attribute init or emptyValue");
		}
		String classname= Node.getAttribute(dataNode, "convert");

		if (classname==null || classname.trim().length()==0)
			convertor=null;
		else {
			if (classname.indexOf('.')<0)
				classname="org.kisst.cordys.util.convert."+classname;
			try {
				Class cls = Class.forName(classname);
				Constructor cons = ReflectionUtil.getConstructor(cls, new Class[] {int.class});
				if (cons != null )
					convertor = (Convertor) cons.newInstance(new Object[] {dataNode});
				else if (ReflectionUtil.getConstructor(cls, new Class[] {})!=null) // has default constructor
					convertor = (Convertor) cls.newInstance();
				else
					throw new RuntimeException("No default or (int) constructor for class "+classname);
			}
			catch (InstantiationException e) { throw new RuntimeException(e); }
			catch (IllegalAccessException e) { throw new RuntimeException(e); }
			catch (ClassNotFoundException e) { throw new RuntimeException(e); }
			catch (InvocationTargetException e) { throw new RuntimeException(e); }
		}
    }

	abstract protected Object createObject(String value);

	protected Object parseSingleNode(int dataNode) {
		String fieldValue = Node.getData(dataNode);
		if (dataNode==0)
				fieldValue=defaultValue;
		else if (fieldValue==null)
			fieldValue="";
		if (convertor!=null)
			fieldValue=convertor.convert(fieldValue);
		return createObject(fieldValue);
	}

	public boolean appendSingleObjectToOutputNode(int outputNode, Object obj) {
		String str=obj.toString();
		if (trim)
			str=str.trim();
		boolean empty = str.equals(defaultValue);
		if (empty && isOptional())
			return true;
		if (convertor!=null)
			str=convertor.convert(str);
		if (createOutputSubNode(outputNode, str)==0)
			return true; // no output node created, so an empty element
		return empty;
	}


	public boolean hasDefaultValue() {
		return defaultValue!=null;
	}

	public Object getDefaultValue() {
		return createObject(defaultValue);
	}
	
}