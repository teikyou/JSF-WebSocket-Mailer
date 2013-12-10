package jp.co.oracle.samples.tasks;

import com.sun.mail.imap.IMAPFolder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.MessagingException;
/**
 *
 * @author Yoshio Terada
 */
public class InboxCheckRunnableTask implements Runnable {

    private final static int MAIL_CHECK_IDLE_TIME = 20000;
    Folder folder;
    private static final Logger logger = Logger.getLogger(InboxCheckRunnableTask.class.getPackage().getName());

    public InboxCheckRunnableTask(Folder folder) {
        this.folder = folder;
    }

    private void executeCheckForIdleDisable() throws InterruptedException, MessagingException {
        Thread.sleep(MAIL_CHECK_IDLE_TIME);
        int count = folder.getMessageCount();
    }

    @Override
    public void run() {

        boolean idleIsAvailable = true;
        while (true) {
            // IMAPFolder のインスタンスで isIdleEnable が true の時実行
            if (folder instanceof IMAPFolder) {
                IMAPFolder ifolder = (IMAPFolder) folder;
                if (idleIsAvailable == true) {
                    try {
                        ifolder.idle();
                    } catch (javax.mail.FolderClosedException fce) {

                    } catch (MessagingException ex) {
                        if (ex.getMessage().contains("IDLE not supported")) {
                            idleIsAvailable = false;
                        } else {
                            logger.log(Level.SEVERE, "IMAP Folder & Something error occured;", ex);
                            return;
                        }
                    }
                } else {
                    try {
                        executeCheckForIdleDisable();
                    } catch (InterruptedException | MessagingException ime) {
                        logger.log(Level.SEVERE, "THis is not IMAP Folder : ", ime);
                        return;
                    }
                }
            } else {
                return;
            }
        }

    }
}
