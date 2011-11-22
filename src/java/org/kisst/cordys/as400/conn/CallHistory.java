package org.kisst.cordys.as400.conn;

import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

public class CallHistory {
	private final Object[] items;
	private final int size;
	private int index=0;
	
	public CallHistory(int size) {
		this.size=size;
		if (size>0)
			this.items = new Object[size];
		else
			this.items=null;
	}
	
	public void add(Object obj) {
		if (items==null)
			return;
		items[index]=obj;
		index = (index + 1) % size;
	}
	
	public String toString() {
		if (items==null)
			return null;
		StringBuilder result=new StringBuilder();
		for (int i=index+1; i<size; i++)
			addToResult(result,items[i]);
		for (int i=0; i<=index; i++)
			addToResult(result,items[i]);
		return result.toString();
	}

	private void addToResult(StringBuilder result, Object object) {
		if (object==null)
			return;
		if (object instanceof ProgramCall) {
			ProgramCall call = (ProgramCall) object;
			result.append(call.getProgram());
			ProgramParameter[] params = call.getParameterList();
			String sep="(";
			for (int i=0; i<params.length; i++) {
				byte[] bytes=params[i].getInputData();
				if (bytes==null) // this seems to be possible
					continue;
				// TODO: be able to work with structs etc.. this may not be best place to do this
				AS400Text conv = new AS400Text(bytes.length);
				result.append(sep+conv.toObject(bytes));
				sep=",";
			}
			// TODO: add output
			result.append(")");
		}
		else if (object instanceof CommandCall) {
			CommandCall call = (CommandCall) object;
			result.append(call.getCommand());
		}
		else
			result.append(object.toString());
		result.append("\n");
	}
}
