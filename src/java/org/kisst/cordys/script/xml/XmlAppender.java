package org.kisst.cordys.script.xml;

import org.kisst.cordys.script.ExecutionContext;

interface XmlAppender {
	void append(ExecutionContext context, int toNode);
}
