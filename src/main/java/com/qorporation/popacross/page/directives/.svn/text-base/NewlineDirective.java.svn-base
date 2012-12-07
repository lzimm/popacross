package com.qorporation.popacross.page.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class NewlineDirective implements TemplateDirectiveModel {

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {        
        if (body != null) {
            body.render(new NewlineFilterWriter(env.getOut()));
        } else {
            throw new RuntimeException("missing body");
        }
    }
	
    private static class NewlineFilterWriter extends Writer {
        private final Writer out;
        private NewlineFilterWriter(Writer out) { this.out = out; }
        public void flush() throws IOException { out.flush(); }
        public void close() throws IOException { out.close(); }
        
        public void write(char[] cbuf, int off, int len) throws IOException {
        	String[] parts = new String(cbuf).trim().split("\n");
        	StringBuilder buf = new StringBuilder("<p>");
        	for (String part: parts) {
        		buf.append(part).append("<br />");
        	}
        	
        	this.out.write(buf.append("</p>").toString());
        }
    }
	
}