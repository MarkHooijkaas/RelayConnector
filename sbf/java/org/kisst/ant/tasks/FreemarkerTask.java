package org.kisst.ant.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerTask extends Task {
	public static class VarDef {
		public String name;
		public String value;
		public void setName(String name) { this.name = name; }
		public void setValue(String value) { this.value = value; }
	}

	private String outputFile;
	private String templateFile;
    private final Vector<VarDef> vars= new Vector<VarDef>();
    
	// The method executing the task
	public void execute() {
		try {
			Logger.selectLoggerLibrary(Logger.LIBRARY_NONE);
		} catch (ClassNotFoundException e1) {/* ignore, should not happen, since no library is selected */	}
		System.out.println(System.getProperty("user.dir"));
		Configuration cfg = new Configuration();
		try {
			cfg.setDirectoryForTemplateLoading(new File(templateFile).getParentFile());
			cfg.setObjectWrapper(new DefaultObjectWrapper());

			Template temp = cfg.getTemplate(new File(templateFile).getName());
			
			SimpleHash data = new SimpleHash();
			for(VarDef v: vars)
				data.put(v.name,v.value);

			Writer out = new FileWriter(outputFile);
			temp.process(data, out);
			out.flush();
		} 
		catch (TemplateException e) { throw new BuildException(e);}
		catch (IOException e) { throw new BuildException(e);}
	}

	public void setOutput(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setTemplate(String templateFile) {
		this.templateFile = templateFile;
	}
	
	public VarDef createVar() {
		VarDef v=new VarDef();
		vars.add(v);
		return v;
	}
}