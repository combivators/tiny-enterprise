package net.tiny.service.communication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MailProvider implements Consumer<MailProvider.Mail> {

    private static final Logger LOGGER = Logger.getLogger(MailProvider.class.getName());

    static final String EMAIL_ADDRESS_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public static final Predicate<String> MAIL_VALIDATOR = validator();

    private final Builder builder;

    private MailProvider(Builder builder) {
        this.builder = builder;
    }

    public Mail to(String to) {
        return new Mail(this, builder.mail).to(to);
    }

    static Predicate<String> validator() {
        return new Predicate<String>() {
            @Override
            public boolean test(String s) {
                if(s == null || s.isEmpty())
                    return false;
                return Pattern.compile(EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE).matcher(s).matches();
            }
        };
    }
    /**
     * 发送邮件
     */
    @Override
    public void accept(Mail mail) {
        send(mail);
    }

    /**
     * 发送邮件
     */
    private void send(Mail mail) {
        javax.mail.Message message = new MimeMessage(builder.smtp.session());
        try {
            message.setFrom(new InternetAddress(MimeUtility.encodeWord(mail.address())));
            message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(mail.to));
            message.setSubject(mail.subject);
            message.setSentDate(new Date());
            // Send the actual Text or HTML message
            if ("text/plain".equalsIgnoreCase(builder.type)) {
                message.setContent(mail.content, builder.type);
            } else {
                Multipart multipart = new MimeMultipart();
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(mail.content, builder.type);
                multipart.addBodyPart(htmlPart);
                if (mail.attachment != null) {
                    multipart.addBodyPart(attachment(mail.attachmentName, mail.attachment, mail.attachmentType));
                }
                message.setContent(multipart);
            }

            if (builder.executor != null) {
                //非同期邮件发送
                builder.executor.execute(new Runnable() {
                    public void run() {
                        try {
                            Transport.send(message);
                        } catch (MessagingException e) {
                            LOGGER.log(Level.WARNING, String.format("[MAIL] Send '%s' error : %s ", mail.to, e.getMessage()), e);
                        }
                    }
                });
            } else {
                Transport.send(message);
            }
        } catch (MessagingException | IOException ex) {
            LOGGER.log(Level.WARNING, String.format("[MAIL] Send '%s' error : %s ", mail.to, ex.getMessage()), ex);
            throw new RuntimeException(ex);
        }
    }

    private MimeBodyPart attachment(String name, byte[] data, String type) throws MessagingException {
        MimeBodyPart attachment = new MimeBodyPart();
        InputStream attachmentDataStream = new ByteArrayInputStream(data);
        attachment.setFileName(name);
        attachment.setContent(attachmentDataStream, type);
        return attachment;
    }

    static class SmtpOption {
        String host;
        int port;
        String user;
        String password;

        public Session session() {
            return Session.getInstance(properties(),
                new javax.mail.Authenticator() {
                  protected PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(user, password);
                  }
                });
        }

        Properties properties() {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", new Integer(port));
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            return props;
        }
    }

    public static class Mail {
        final Consumer<Mail> consumer;
        final String from;
        String subject = "Notitle";
        String to, content, issuer;
        String encode = "iso-2022-jp";
        byte[] attachment;
        String attachmentName;
        String attachmentType;
        //, Map<String, Object> model,
        Mail(Consumer<Mail> c, String f) {
            consumer = c;
            from = f;
        }
        public Mail to(String s) {
            if (!MAIL_VALIDATOR.test(s))
                throw new IllegalArgumentException(String.valueOf(s));
            to = s;
            return this;
        }
        public Mail subject(String s) { subject = s; return this; }
        public Mail issuer(String s) { issuer = s; return this; }
        public Mail content(String s) { content = s; return this; }
        public Mail attachment(String n, byte[] s, String t) {
            attachmentName = n;
            attachment = s;
            attachmentType = t;
            return this;
        }

        String address() {
            return String.format("%s <%s>", (issuer != null ? issuer : ""), from);
        }

        public void send() {
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("Mail content is empty.");
            }
            consumer.accept(this);
        }
    }
    public static class Builder {
        SmtpOption smtp = new SmtpOption();
        ExecutorService executor;
        String mail;
        String type = "text/plain"; // "text/html"

        public Builder smtp(String h, int p, String u, String s) {
            smtp.host = h;
            smtp.port = p;
            smtp.user = u;
            smtp.password = s;
            return this;
        }
        public Builder executor(ExecutorService e) { executor = e; return this; }
        public Builder mail(String m) {
            if (!MAIL_VALIDATOR.test(m))
                throw new IllegalArgumentException(String.valueOf(m));
            mail = m;
            return this;
        }
        public Builder type(String t) { type = t; return this; }
        public MailProvider build() {
            return new MailProvider(this);
        }
    }

}
