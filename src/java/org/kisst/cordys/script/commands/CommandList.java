package org.kisst.cordys.script.commands;

import java.util.HashMap;

import org.kisst.cordys.script.Command;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.Script;


public class CommandList {
	private static CommandList basicCommands=new CommandList(null);
	
	static {
		basicCommands.addCommand("xmlns",  new GenericCommand(XmlnsStep.class));
		basicCommands.addCommand("var",    new GenericCommand(VarStep.class));
		basicCommands.addCommand("output", new GenericCommand(OutputStep.class));
		basicCommands.addCommand("import", new GenericCommand(ImportStep.class));
		basicCommands.addCommand("createXmlVar", new GenericCommand(CreateXmlVarStep.class));
		basicCommands.addCommand("delete", new GenericCommand(DeleteStep.class));
		basicCommands.addCommand("append", new GenericCommand(XmlAppendStep.class));
		basicCommands.addCommand("script", new GenericCommand(Script.class));
		basicCommands.addCommand("sleep",  new GenericCommand(SleepStep.class));
		basicCommands.addCommand("default",new GenericCommand(DefineDefaultStep.class));
		basicCommands.addCommand("call",   new GenericCommand(CallStep.class));
		basicCommands.addCommand("fault",  new GenericCommand(FaultStep.class));
		basicCommands.addCommand("stripPrefixes",  new GenericCommand(StripPrefixesStep.class));
		basicCommands.addCommand("switch",  new GenericCommand(SwitchStep.class));
	}

	public static CommandList getBasicCommands() {
		return basicCommands;
	}

	public CommandList(CommandList parent) {
		this.parent=parent;
	}
	private final CommandList parent;
	private final HashMap<String,Command> commands = new HashMap<String,Command>();
	
	public void addCommand(String name, Command type) {	commands.put(name, type); }
	public Command getCommand(String name) {
		Command cmd=commands.get(name);
		if (cmd!=null)
			return cmd;
		if (parent!=null)
			return parent.getCommand(name);
		throw new RuntimeException("Unknown command element <"+name+" ...>");
	}
}
