package org.kisst.cordys.as400.pcml;

import java.beans.PropertyVetoException;

import org.kisst.cordys.script.CompilationContext;

import com.eibus.xml.nom.Node;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

public class PcmlProgram extends PcmlStruct {
	private final String programName;
	private final String programPath;
	
	public PcmlProgram(CompilationContext context, int programNode)
    {
		super(context, null, programNode);
    	programName=Node.getAttribute(programNode, "name");
    	programPath=Node.getAttribute(programNode, "path");
    }

    public String getProgramPath() {
    	if (programPath!=null && ! programPath.trim().equals(""))
    		return programPath;
    	return  "/QSYS.LIB/*LIBL.LIB/" + programName + ".PGM";
    }

    public ProgramCall prepareProgramCall(int inputNode) {
    	ProgramParameter[] as400par = new ProgramParameter[params.length];
		for (int i=0; i<as400par.length; i++) {
			AS400DataType dataType = params[i].getDataType(); 
			if (params[i].isInput()) {
				Object obj=params[i].parseInputNode(inputNode);
				if (obj==null) {
					if (params[i].isOptional()) {
						as400par[i] = new ProgramParameter();
						as400par[i].setNullParameter(true);
					}
					else {
						throw new RuntimeException("Parameter "+params[i].getName()+" is not passed, but is not optional");
					}
				}
				else {
					byte[] bytes  = dataType.toBytes(obj);
					if (params[i].isOutput())
						as400par[i]  = new ProgramParameter(ProgramParameter.PASS_BY_VALUE, bytes, dataType.getByteLength());
					else
						as400par[i]  = new ProgramParameter(ProgramParameter.PASS_BY_VALUE, bytes);
				}
			}
			else {
				as400par[i] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, dataType.getByteLength());
			}
		}
    	ProgramCall call = new ProgramCall();
    	try {
    		call.setProgram(getProgramPath(), as400par);
    	}
    	catch (PropertyVetoException e) { throw new RuntimeException(e); }
    	return call;
    }

    public void processCallResult(ProgramParameter[] as400par , int outputNode) {
		for (int i=0; i<as400par.length; i++) {
			if (params[i].isOutput()) {
				Object obj= params[i].getDataType().toObject(as400par[i].getOutputData());
				params[i].appendToOutputNode(outputNode, obj);
			}
		}
    }	

}
