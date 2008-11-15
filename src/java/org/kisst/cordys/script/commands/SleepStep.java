package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class SleepStep implements Step {
	private final Expression millis;
	
	public SleepStep(CompilationContext compiler, final int node) {
		millis = ExpressionParser.parse(compiler, Node.getAttribute(node, "millis"));
	}

	public void executeStep(ExecutionContext context) {
		String m=millis.getString(context);
		long m2=Long.parseLong(m);
		try {
			Thread.sleep(m2);
		} 
		catch (InterruptedException e) { throw new RuntimeException("sleep "+m+" call interrupted");}
	}
}
