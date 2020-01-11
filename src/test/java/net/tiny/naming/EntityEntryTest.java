package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;


public class EntityEntryTest {

    @Test
    public void testAnyType() throws Exception {
        TestEntity sample = new TestEntity("abcde", "12345");

        EntityEntry entry = new EntityEntry("hello", sample);
        assertTrue(entry.getClassType().equals(TestEntity.class));

        StringWriter writer = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(EntityEntry.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entry, writer);
        String xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        StringReader reader = new StringReader(xml);
        EntityEntry other = JAXB.unmarshal(reader, EntityEntry.class);
        assertNotNull(other);
        TestEntity te = (TestEntity)other.toValue();
        assertEquals("abcde", te.getTest1());
        assertEquals("12345", te.getTest2());
    }

    @Test
    public void testJavaType() throws Exception {
        EntityEntry entry = new EntityEntry("hello", "abcdef");
        assertTrue(entry.getClassType().equals(String.class));

        StringWriter writer = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(EntityEntry.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(entry, writer);
        String xml = writer.toString();
        System.out.println(xml);
        System.out.println();

        StringReader reader = new StringReader(xml);
        EntityEntry other = JAXB.unmarshal(reader, EntityEntry.class);
        assertNotNull(other);
        assertTrue(other.toValue() instanceof String);
        assertEquals("abcdef", other.toValue());
    }
}
