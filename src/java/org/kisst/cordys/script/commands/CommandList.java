package org.kisst.cordys.script.commands;

import java.util.HashMap;

import org.kisst.cordys.script.Command;
import org.kisst.cordys.script.GenericCommand;


public class CommandList {
	private static CommandList basicCommands=new CommandList(null);
	
	static {
		basicCommands.addCommand("xmlns",  new GenericCommand(XmlnsStep.class));
		basicCommands.addCommand("var",    new GenericCommand(VarStep.class));
		basicCommands.addCommand("output", new GenericCommand(OutputStep.class));
		basicCommands.addCommand("import", new GenericCommand(ImportStep.class));
		basicCommands.addCommand("delete", new GenericCommand(DeleteStep.class));
		basicCommands.addCommand("append", new GenericCommand(XmlAppendStep.class));
		basicCommands.addCommand("sleep",  new GenericCommand(SleepStep.class));
		basicCommands.addCommand("default",new GenericCommand(DefineDefaultStep.class));
		basicCommands.addCommand("call",   new GenericCommand(CallStep.class));
		basicCommands.addCommand("fault",  new GenericCommand(FaultStep.class));
		basicCommands.addCommand("switch", new GenericCommand(SwitchStep.class));
		basicCommands.addCommand("groovy", new GenericCommand(GroovyStep.class));
		basicCommands.addCommand("createXmlVar",   new GenericCommand(CreateXmlVarStep.class));
		basicCommands.addCommand("stripPrefixes",  new GenericCommand(StripPrefixesStep.class));
		basicCommands.addCommand("replaceText",    new GenericCommand(ReplaceTextStep.class));
		basicCommands.addCommand("getConfigValue", new GenericCommand(GetConfigValueStep.class));
		basicCommands.addCommand("wsaTransformReplyTo", new GenericCommand(WsaTransformReplyToStep.class));
		basicCommands.addCommand("soapMerge", new GenericCommand(SoapMergeStep.class));
	}
	public static void addBasicCommand(String name, Command type) {	basicCommands.addCommand(name, type); }


	public CommandList() {
		this.parent=basicCommands;
	}
	private CommandList(CommandList parent) {
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
