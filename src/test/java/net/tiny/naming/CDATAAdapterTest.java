package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;

import org.junit.jupiter.api.Test;

public class CDATAAdapterTest {

    @Test
    public void testJAXBAdpater() throws Exception {
        NormalizedStringAdapter nsa = new NormalizedStringAdapter();
        assertEquals(" abc\r\n\t xyz \r123 ", nsa.marshal(" abc\r\n\t xyz \r123 "));
        assertEquals(" abc    xyz  123 ", nsa.unmarshal(" abc\r\n\t xyz \r123 "));

        HexBinaryAdapter hba = new HexBinaryAdapter();
        assertEquals("2061626364652031323334", hba.marshal(" abcde 1234".getBytes()));
        assertEquals(" abcde 1234", new String(hba.unmarshal("2061626364652031323334")));

        CollapsedStringAdapter csa = new CollapsedStringAdapter();
        assertEquals(" abc\r\n\t xyz \r123 ", csa.marshal(" abc\r\n\t xyz \r123 "));
        assertEquals("abc xyz 123", csa.unmarshal(" abc\r\n\t xyz \r123 "));
    }

    @Test
    public void testCDATAAdapter() throws Exception {
        CDATAAdapter ca = new CDATAAdapter();
        assertEquals("<![CDATA[rO0ABXQACmFiY2RlZmcxMjM=]]>", ca.marshal("abcdefg123"));
        assertEquals("abcdefg123", ca.unmarshal("<![CDATA[rO0ABXQACmFiY2RlZmcxMjM=]]>"));
    }

    @Test
    public void testXMLAdapter() throws Exception {
        XmlDataAdapter xa = new XmlDataAdapter();
        ValueEntry entry = new ValueEntry("vstr", "abc123");
        String value = "<!--net.tiny.naming.ValueEntry--><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<value name=\"vstr\" type=\"string\">abc123</value>\n]]>";
        assertEquals(value, xa.marshal(entry));
        ValueEntry ve = (ValueEntry)xa.unmarshal(value);
        assertEquals("abc123", ve.getValue());
    }
}
