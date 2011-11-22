/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

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
		basicCommands.addCommand("rename", new GenericCommand(RenameStep.class));
		basicCommands.addCommand("append", new GenericCommand(XmlAppendStep.class));
		basicCommands.addCommand("sleep",  new GenericCommand(SleepStep.class));
		basicCommands.addCommand("default",new GenericCommand(DefineDefaultStep.class));
		basicCommands.addCommand("call",   new GenericCommand(CallStep.class));
		basicCommands.addCommand("relay-call", new GenericCommand(RelayCallStep.class));
		basicCommands.addCommand("fault",  new GenericCommand(FaultStep.class));
		basicCommands.addCommand("switch", new GenericCommand(SwitchStep.class));
		basicCommands.addCommand("groovy", new GenericCommand(GroovyStep.class));
		basicCommands.addCommand("createXmlVar",   new GenericCommand(CreateXmlVarStep.class));
		basicCommands.addCommand("stripPrefixes",  new GenericCommand(StripPrefixesStep.class));
		basicCommands.addCommand("replaceText",    new GenericCommand(ReplaceTextStep.class));
		basicCommands.addCommand("getConfigValue", new GenericCommand(GetConfigValueStep.class));
		basicCommands.addCommand("wsaTransformReplyTo", new GenericCommand(WsaTransformReplyToStep.class));
		basicCommands.addCommand("soapMerge", new GenericCommand(SoapMergeStep.class));
		basicCommands.addCommand("convert", new GenericCommand(ConvertStep.class));
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
