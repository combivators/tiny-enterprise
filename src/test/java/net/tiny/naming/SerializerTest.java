package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;


public class SerializerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testEncodeDecode() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("one", new Integer(1));
        map.put("million", new Long(100000L));
        map.put("list", Arrays.asList("Hoge", "Fuga"));
        map.put("Hoge", "hoge@my.com");
        map.put("Fuga", "fuga@your.com");
        String encoded = Serializer.encode(map);
        System.out.println(encoded);

        Object decoded = Serializer.decode(encoded);
        assertTrue(decoded instanceof Map);
        Map<String, Object> cloned = (Map<String, Object> )decoded;
        assertNotSame(map, cloned);
        assertTrue(cloned.containsKey("list"));
        assertTrue(cloned.containsValue("fuga@your.com"));
        assertEquals(new Integer(1), cloned.get("one"));
        assertEquals(new Long(100000L), cloned.get("million"));
        List<?> list = (List<?>)cloned.get("list");
        assertEquals("Hoge", list.get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEncodeDecodeStream() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("one", new Integer(1));
        map.put("million", new Long(100000L));
        map.put("list", Arrays.asList("Hoge", "Fuga"));
        map.put("Hoge", "hoge@my.com");
        map.put("Fuga", "fuga@your.com");

        InputStream in = Serializer.encodeStream(map);
        assertNotNull(in);
        Object decoded = Serializer.decodeStream(in);
        assertTrue(decoded instanceof Map);
        Map<String, Object> cloned = (Map<String, Object> )decoded;
        assertNotSame(map, cloned);
        assertTrue(cloned.containsKey("list"));
        assertTrue(cloned.containsValue("fuga@your.com"));
        assertEquals(new Integer(1), cloned.get("one"));
        assertEquals(new Long(100000L), cloned.get("million"));
        List<?> list = (List<?>)cloned.get("list");
        assertEquals("Hoge", list.get(0));
    }
}
