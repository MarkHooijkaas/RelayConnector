package org.kisst.ant.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerTask extends Task {

	private String outputFile;
	private String templateFile;

	// The method executing the task
	public void execute() {
		Configuration cfg = new Configuration();
		try {
			cfg.setDirectoryForTemplateLoading(new File(templateFile).getParentFile());
			cfg.setObjectWrapper(new DefaultObjectWrapper());

			Template temp = cfg.getTemplate(templateFile);
			
			SimpleHash data = new SimpleHash();
			Iterator it = this.getProject().getProperties().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				data.put(((String)pairs.getKey()).replaceAll("[.]", "_"),pairs.getValue());
			}			

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
}