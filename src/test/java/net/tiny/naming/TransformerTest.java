package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class TransformerTest {

    public static enum TestType {
        index,
        productList,
        productSearch
    }

    public static class TestBean implements Serializable {
        private static final long serialVersionUID = 1;
        private String name;
        private long value;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public long getValue() {
            return value;
        }
        public void setValue(long value) {
            this.value = value;
        }
    }

    @Test
    public void testFormat() throws Exception {
        assertEquals("abcd", Transformer.format("abcd"));
        assertEquals("1234", Transformer.format(1234));
        assertEquals("123.456", Transformer.format(123.456f));
        assertEquals("19730303", Transformer.format(new java.util.Date(100000000000L)));
        assertEquals("1973/03/03-18:46:40", Transformer.format(new java.sql.Date(100000000000L)));
        assertEquals("19730303184640001", Transformer.format(new java.sql.Timestamp(100000000001L)));
        //
        assertEquals("['abc', 'efg', 'xyz']", Transformer.format(new String[]{"abc","efg","xyz"}));
        assertEquals("[true, false, true, false]", Transformer.format(new boolean[]{true, false, true, false}));
        assertEquals("[0, 123, 456, 789]", Transformer.format(new int[]{0, 123, 456,789}));
        assertEquals("[0, 123, 456789]", Transformer.format(new long[]{0L, 123L, 456789L}));
        assertEquals("[0.0, 12.3, 456.789]", Transformer.format(new float[]{0.0f, 012.3f, 456.7890f}));
        assertEquals("[0.0, 12.3, 456.789]", Transformer.format(new double[]{0.0d, 012.3d, 456.7890d}));
        assertEquals("YWJjZGVmZ2g=", Transformer.format("abcdefgh".getBytes()));
        assertEquals("\"abcdefgh\"", Transformer.format("abcdefgh".toCharArray()));
        List<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("123");
        list.add("EFG");
        list.add("789");
        assertEquals("['abc', '123', 'EFG', '789']", Transformer.format(list));

        Properties prop = new Properties();
        prop.setProperty("tom", "abc123");
        prop.setProperty("cat", "456");
        String ret = Transformer.format(prop);
        assertTrue(ret.contains("tom:'abc123'"));
        assertTrue(ret.contains("cat:'456'"));

        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("tom", "abc123");
        map.put("cat", "456");
        ret = Transformer.format(map);
        assertTrue(ret.contains("\"cat\":\"456\""));
        assertTrue(ret.contains("\"tom\":\"abc123\""));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1445086050000l);
        assertEquals("2015-10-17 21:47:30 JST", Transformer.format(calendar));
        assertEquals("productSearch", Transformer.format(TestType.productSearch));
    }

    @Test
    public void testParse() throws Exception {
        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

        assertEquals("abcd", Transformer.parse("abcd", String.class));
        assertEquals(1234, (int)Transformer.parse("1234", int.class));
        assertEquals(123456789L, (long)Transformer.parse("123456789", long.class));
        assertEquals(123.456f, (float)Transformer.parse("123.456", float.class));
        assertEquals(.123456d, (double)Transformer.parse("0.123456", double.class));

        assertEquals("19730303", DATE_FORMAT.format(Transformer.parse("19730303", java.util.Date.class)));
        assertEquals(new java.sql.Date(100000000000L), Transformer.parse("1973/03/03-18:46:40", java.sql.Date.class));
        assertEquals(new java.sql.Timestamp(100000000001L), Transformer.parse("19730303184640001", java.sql.Timestamp.class));

        assertTrue(Arrays.equals(new String[]{"abc","efg","xyz"}, (String[])Transformer.parse("['abc', 'efg', 'xyz']", String[].class)));
        assertTrue(Arrays.equals(new boolean[]{true, false, true, false}, (boolean[])Transformer.parse("[true, false, true, false]", boolean[].class)));

        assertTrue(Arrays.equals(new int[]{0, 123, 456,789}, (int[])Transformer.parse("[0, 123, 456, 789]", int[].class)));
        assertTrue(Arrays.equals(new long[]{0L, 123L, 456789L}, (long[])Transformer.parse("[0, 123, 456789]", long[].class)));
        assertTrue(Arrays.equals(new float[]{0.0f, 012.3f, 456.7890f}, (float[])Transformer.parse("[0.0, 12.3, 456.789]", float[].class)));
        assertTrue(Arrays.equals(new double[]{0.0d, 012.3d, 456.7890d}, (double[])Transformer.parse("[0.0, 12.3, 456.789]", double[].class)));

        assertEquals("abcdefgh", new String(Transformer.parse("YWJjZGVmZ2g=", byte[].class)));
        assertEquals("abcdefgh", new String(Transformer.parse("\"abcdefgh\"", char[].class)));

        List<?> list = Transformer.parse("'abc', '123', 'EFG', '789'", List.class);
        assertEquals("abc", list.get(0));
        assertEquals("123", list.get(1));
        assertEquals("EFG", list.get(2));
        assertEquals("789", list.get(3));

        Properties prop = Transformer.parse("tom:'abc123', cat:'456'", Properties.class);
        assertEquals("abc123", prop.get("tom"));
        assertEquals("456", prop.get("cat"));

        Map<?,?> map = Transformer.parse("{\"cat\":\"456\", \"tom\":\"abc123\"}", Map.class);
        assertEquals("456", map.get("cat"));
        assertEquals("abc123", map.get("tom"));

        TestEntity testEntity = new TestEntity("t1", "t2");
        try {
            Transformer.format(testEntity);
            fail();
        } catch(Exception ex) {
            assertTrue( ex instanceof IllegalArgumentException);
        }


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1445086050000l);
        Calendar cal = Transformer.parse("2015-10-17 21:47:30 JST", Calendar.class);
        assertEquals(calendar, cal);

        TestType testType =Transformer.parse("productSearch", TestType.class);
        assertEquals(TestType.productSearch, testType);
    }

    @Test
    public void testFormatParse() throws Exception {
        ValueEntry entry = new ValueEntry("vstr", "abc123");
        String value = "<!--net.tiny.naming.ValueEntry--><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<value name=\"vstr\" type=\"string\">abc123</value>\n]]>";
        assertEquals(value, Transformer.format(entry));
        ValueEntry ve = Transformer.parse(value, ValueEntry.class);
        assertEquals("abc123", ve.getValue());

        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "tom");
        map.put("value", "abc123");
        value = "{\"name\":\"tom\", \"value\":\"abc123\"}";
        assertEquals(value, Transformer.format(map));


        HashMap<?,?> hm = Transformer.parse(value, HashMap.class);
        assertNotNull(hm);
        assertEquals("tom", hm.get("name"));
        assertEquals("abc123", hm.get("value"));
        Map<?,?> m = Transformer.parse(value, Map.class);
        assertEquals("tom", m.get("name"));
        assertEquals("abc123", m.get("value"));

        value = "[Item1, Item2, Item3]";
        List<?> list = Transformer.parse(value, List.class);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("Item1", list.get(0));
        assertEquals("Item3", list.get(2));

        TestBean bean = new TestBean();
        bean.setName("tom");
        bean.setValue(123456789L);
        String data = "<![CDATA[rO0ABXNyAChuZXQudGlueS5uYW1pbmcuVHJhbnNmb3JtZXJUZXN0JFRlc3RCZWFuAAAAAAAAAAECAAJKAAV2YWx1ZUwABG5hbWV0ABJMamF2YS9sYW5nL1N0cmluZzt4cAAAAAAHW80VdAADdG9t]]>";
        assertEquals(data, Transformer.format(bean));
        Serializable s = Transformer.parse(data, Serializable.class);
        assertNotNull(s);
        assertTrue(s instanceof TestBean);
        TestBean b = (TestBean)s;
        assertEquals("tom", b.getName());
        assertEquals(123456789L, b.getValue());
    }


    /*
    public void testParse() throws Exception {
         assertEquals("abcd", Transformer.parse("abcd", "string"));
         assertEquals(1234, Transformer.parse("1234", "int"));
         assertEquals(123.456f, Transformer.parse("float", "123.4560"));
         Date date = (Date)Transformer.parse("date", "19730303");
//		 assertEquals("19730303", DataType.DATE_FORMAT.format(date));
//		 byte[] data = (byte[])DataType.parse(DataType.BYTE_ARRAY, "YWJjZGVmZ2g=");
//		 assertEquals("abcdefgh", new String(data));
    }
    */
}
