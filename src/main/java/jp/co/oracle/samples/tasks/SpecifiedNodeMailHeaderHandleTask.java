package jp.co.oracle.samples.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import jp.co.oracle.samples.beans.MailHeaderModel;
import jp.co.oracle.samples.beans.MailHeader;

/**
 *
 * @author Yoshio Terada
 */
public class SpecifiedNodeMailHeaderHandleTask implements Callable<MailHeaderModel> {

    private final Store store;
    private final String folderFullName;
    private final int numberOfMessage;

    private static final Logger logger = Logger.getLogger(SpecifiedNodeMailHeaderHandleTask.class.getPackage().getName());
    
    public SpecifiedNodeMailHeaderHandleTask(Store store,String folderFullName,int numberOfMessage){
        this.store = store;
        this.folderFullName = folderFullName;
        this.numberOfMessage = numberOfMessage;
    }    
    
    @Override
    public MailHeaderModel call() throws Exception {
        MailHeaderModel model = null;
        if (store != null) {
            try {
                Folder folder = store.getFolder(folderFullName);
                if (!folder.isOpen()) {
                    folder.open(javax.mail.Folder.READ_WRITE);
                }

                int endMsgs = folder.getMessageCount();
                int startMsgs = endMsgs - (numberOfMessage - 1);

                Message[] msgs = folder.getMessages(startMsgs, endMsgs);
                List<MailHeader> data = new ArrayList<>();
                for (Message msg : msgs) {
                    MailHeader oneMsg = new MailHeader(msg.getSubject(), msg.getFrom(), msg.getSentDate(), msg.getSize(), msg.getMessageNumber());
                    data.add(oneMsg);
                }
                model = new MailHeaderModel(data);
            } catch (MessagingException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return model;
    }
}
