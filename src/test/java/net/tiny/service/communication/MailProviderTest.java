package net.tiny.service.communication;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import net.tiny.service.communication.MailProvider;

public class MailProviderTest {

    @Test
    public void testEmailValidator() throws Exception {
        assertFalse(MailProvider.MAIL_VALIDATOR.test(null));
        assertFalse(MailProvider.MAIL_VALIDATOR.test(""));
        assertFalse(MailProvider.MAIL_VALIDATOR.test("  "));
        assertFalse(MailProvider.MAIL_VALIDATOR.test("abcde"));
        assertFalse(MailProvider.MAIL_VALIDATOR.test("abcde@"));
        assertFalse(MailProvider.MAIL_VALIDATOR.test("@com"));

        assertTrue(MailProvider.MAIL_VALIDATOR.test("info@ac.abc.com"));
        assertTrue(MailProvider.MAIL_VALIDATOR.test("in.f-o@a-c.abc.co.jp"));
        assertTrue(MailProvider.MAIL_VALIDATOR.test("i_n.120@a-c.abc.co.jp"));
        assertTrue(MailProvider.MAIL_VALIDATOR.test("i_n+120@a-c.abc.co.jp"));

        assertFalse(MailProvider.MAIL_VALIDATOR.test("i_n#120@a-c.abc.co.jp"));
        assertFalse(MailProvider.MAIL_VALIDATOR.test("i_n!120@a-c.abc.co.jp"));

    }

    @Test
    public void testBuildMail() throws Exception {
        MailProvider provider = new MailProvider.Builder()
            .smtp("localhost", 25, "smtp", "pass")
            .mail("info@company.com")
            .build();

        MailProvider.Mail mail = provider.to("hoge@customer.com")
            .subject("Register")
            .content("Hello");
        assertNotNull(mail);
        //mail.send();

    }
}
