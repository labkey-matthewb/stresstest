package stresstest;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MailHelper implements AutoCloseable
{
    final String host;
    final int port;
    final String folder;
    final String username;
    final String password;

    final Store store;
    final Folder inbox;

    // pop.host
    // pop.user
    // pop.password
    MailHelper(Properties props) throws MessagingException
    {
        host = props.getProperty("imap.host");
        username = props.getProperty("imap.username");
        port = Integer.parseInt(props.getProperty("imap.port", "993"));
        folder = props.getProperty("imap.folder", "inbox");
        password = props.getProperty("imap.password");
        System.err.println(new URLName("imaps", host, port, folder, username, "****"));

        // from java doc: It is expected that the client supplies values for the properties listed in Appendix A of the JavaMail spec (particularly mail.store.protocol, mail.transport.protocol, mail.host, mail.user, and mail.from) as the defaults are unlikely to work in all cases.
        // https://www.tutorialspoint.com/javamail_api/javamail_api_pop3_servers.htm
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(mailProps, null);
        store = session.getStore("imaps");
        store.connect(host, username, password);
        inbox = store.getFolder(folder);
        inbox.open(Folder.READ_ONLY);
    }

    @Override
    public void close() throws Exception
    {
        if (null != inbox)
            inbox.close(true);
        if (null != store)
            store.close();
    }

    Collection<Message> find(String email, long timeout, Predicate<Message> predicate) throws MessagingException
    {
        InternetAddress address = new InternetAddress(email);

        if (!inbox.isOpen())
            inbox.open(Folder.READ_ONLY);

        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        AddressTerm addressTerm = new RecipientTerm(Message.RecipientType.TO, address);
        AndTerm searchTerm = new AndTerm(addressTerm, unseenFlagTerm);

        long start = System.currentTimeMillis();
        do
        {
            Message[] messages = inbox.search(searchTerm);
            Collection<Message> ret = Arrays.asList(messages);
            if (null != predicate && !ret.isEmpty())
            {
                ret = ret.stream().filter(predicate).collect(Collectors.toList());
            }
            if (!ret.isEmpty())
                return ret;
        } while (start + timeout > System.currentTimeMillis());

        throw new SearchException("message not found");
    }


    // <MimeType,String>
    static Map.Entry<String,String> getMessageContent(Message message, String preferMimeType) throws MessagingException, IOException
    {
        Object content = message.getContent();
        if (content instanceof Multipart)
        {
            var plain = new StringBuilder();
            var html = new StringBuilder();
            Multipart multipart = (Multipart) content;
            for (int i=0; i<multipart.getCount(); i++)
            {
                if (multipart.getBodyPart(i).isMimeType("text/plain"))
                    plain.append(multipart.getBodyPart(i).getContent().toString());
                else if (multipart.getBodyPart(i).isMimeType("text/html"))
                    html.append(multipart.getBodyPart(i).getContent().toString());
            }

            if (html.length() > 0 && (plain.length()==0 || preferMimeType.equals("text/html")))
                return new AbstractMap.SimpleEntry<>("text/html", html.toString());
            return new AbstractMap.SimpleEntry<>("text/plain", plain.toString());
        }
        return new AbstractMap.SimpleEntry<>("text/plain", content.toString());
    }

    public static void main(String[] args) throws Exception
    {
        Properties props = new Properties();
        props.load(new FileReader(new File("stresstest.properties")));

        String email = props.getProperty("imap.username");
        int at = email.indexOf("@");
        String testEmail = email.substring(0,at) + "+test" + email.substring(at);
        System.out.println("recipient email: " + testEmail);

        try (MailHelper mh = new MailHelper(props))
        {
            var results = mh.find(testEmail, 60_000, null);
            for (Message message : results)
            {
                System.out.println(results);
                System.err.println("TO: " + message.getRecipients(Message.RecipientType.TO)[0]);
                System.err.println("DATE: " + message.getReceivedDate());
                System.err.println(getMessageContent(message,"text/html").getValue());
            }
        }
    }
}
