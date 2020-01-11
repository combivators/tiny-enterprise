package net.tiny.naming;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CDATAAdapter extends XmlAdapter<String, Object> {

    private static String PREFIX = "<![CDATA[";
    private static String SUBFIX = "]]>";

    @Override
    public String marshal(Object object) throws Exception {
        StringBuilder sb = new StringBuilder(PREFIX);
        sb.append(Serializer.encode(object));
        sb.append(SUBFIX);
        return sb.toString();
    }

    @Override
    public Object unmarshal(String data) throws Exception {
        String buf = data;
        if(data.startsWith(PREFIX) && data.endsWith(SUBFIX)) {
            buf = data.substring(9, data.length()-3);
        }
        return Serializer.decode(buf);
    }
}
