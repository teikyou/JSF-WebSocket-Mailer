package jp.co.oracle.samples.tasks;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import jp.co.oracle.samples.msgutil.MessageDumpUtil;

/**
 *
 * @author Yoshio Terada
 */
public class SpecifiedMessageHandlerTask implements Callable<String> {

    private final Store store;
    private final String folderFullName;
    private final int msgCount;
    private static final Logger logger = Logger.getLogger(SpecifiedMessageHandlerTask.class.getPackage().getName());
    
    public SpecifiedMessageHandlerTask(Store store, String folderFullName, int msgCount) {
        this.store = store;
        this.folderFullName = folderFullName;
        this.msgCount = msgCount;
    }

    @Override
    public String call() throws Exception {
        String returnMsg ="";
        if (store != null) {
            Folder folder;
            try {
                folder = store.getFolder(folderFullName);
                if (!folder.isOpen()) {
                    folder.open(javax.mail.Folder.READ_WRITE);
                }
                Message msg = folder.getMessage(msgCount);
                MessageDumpUtil dumpUtil = new MessageDumpUtil();
                returnMsg = dumpUtil.getText(msg);
            } catch (MessagingException | IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        return returnMsg;
    }
}
