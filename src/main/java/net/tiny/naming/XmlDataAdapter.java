package net.tiny.naming;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlDataAdapter extends XmlAdapter<String, Object> {

	private static String COMMENT_PREFIX = "<!";
	private static String COMMENT_SUBFIX = ">";
	private static String COMMENT_FIX = "--";
	private static String PREFIX = COMMENT_PREFIX + "[CDATA[";
	private static String SUBFIX = "]]" + COMMENT_SUBFIX;

	@Override
	public String marshal(Object object) throws Exception {
		StringWriter writer = new StringWriter();
		JAXB.marshal(object, writer);
		writer.close();
		StringBuilder sb = new StringBuilder();
		sb.append(COMMENT_PREFIX);
		sb.append(COMMENT_FIX);
		sb.append(object.getClass().getName());
		sb.append(COMMENT_FIX);
		sb.append(COMMENT_SUBFIX);
		sb.append(PREFIX);
		sb.append(writer.toString());
		sb.append(SUBFIX);
		return sb.toString();
	}

	@Override
	public Object unmarshal(String data) throws Exception {
		int pos = data.indexOf("-->");
		if (pos == -1) {
			return null;
		}
		String className = data.substring(4, pos);
		Class<?> classType = Class.forName(className);
		StringReader reader = new StringReader(data.substring(pos + 12, data.length()-3));
		return JAXB.unmarshal(reader, classType);
	}
}
