package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;

public class ValueEntryTest {

    @Test
    public void testValueEntryShort() throws Exception {
        ValueEntry entry = new ValueEntry("tom", (short)123);
        StringWriter writer = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ValueEntry.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entry, writer);
        String xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        assertEquals("tom", entry.getName());
        Object value = entry.toValue();
        assertEquals((short)123, value);
    }

    @Test
    public void testValueEntryXML() throws Exception {
        ValueEntry entry = new ValueEntry("tom", "abc123");
        StringWriter writer = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ValueEntry.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entry, writer);
        String xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        assertEquals("tom", entry.getName());
        Object value = entry.toValue();
        assertTrue(value instanceof String);
        assertEquals("abc123", value);

        Entry entry2 = new Entry(DataType.INTEGER, 123);
        writer = new StringWriter();
        jc = JAXBContext.newInstance(Entry.class);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entry2, writer);
        xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        value = entry2.toValue();
        assertEquals(123, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEntityXML() throws Exception {
        SampleEntity entity = new SampleEntity();
        List<ValueEntry> mapEntries = new ArrayList<ValueEntry>();
        ValueEntry entry = new ValueEntry("tom", "abc");
        mapEntries.add(entry);
        entry = new ValueEntry();
        entry = new ValueEntry("vint", 123);
        mapEntries.add(entry);
        entity.setMapEntries(mapEntries);

        List<Entry> listEntries = new ArrayList<Entry>();
        Entry entry2 = new Entry("xyz");
        listEntries.add(entry2);
        entry2 = new Entry(987);
        listEntries.add(entry2);
        entity.setListEntries(listEntries);

        Map<String, String> obj = new HashMap<String, String>();
        obj.put("name", "abc123");
        entity.setObject(obj);

        Properties prop = new Properties();
        prop.setProperty("tom", "abc123");
        entity.setProp(prop);

        StringWriter writer = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(SampleEntity.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(entity, writer);

        String xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        StringReader reader = new StringReader(xml);
        SampleEntity other = JAXB.unmarshal(reader, SampleEntity.class);
        assertNotNull(other);
        Map<String, String> map = (Map<String, String>)entity.getObject();
        assertEquals("abc123", map.get("name"));

        Properties prop2 = entity.getProp();
        assertEquals("abc123", prop2.getProperty("tom"));
    }

    @Test
    public void testByteArray() throws Exception {
        ValueEntry entry = new ValueEntry("bytes", "abcdefghijklmnopqrstuvwxyz123567890".getBytes());
        StringWriter writer = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ValueEntry.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entry, writer);
        String xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        StringReader reader = new StringReader(xml);
        ValueEntry other = JAXB.unmarshal(reader, ValueEntry.class);
        assertNotNull(other);
        assertTrue(other.toValue() instanceof byte[]);
        assertEquals("abcdefghijklmnopqrstuvwxyz123567890", new String((byte[])other.toValue()));
    }

}
