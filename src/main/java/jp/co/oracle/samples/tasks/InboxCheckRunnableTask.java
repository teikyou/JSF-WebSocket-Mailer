/*
* Copyright 2013 Yoshio Terada
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
    volatile boolean isRunnable = true;
    
    public InboxCheckRunnableTask(Folder folder) {
        this.folder = folder;
    }

    private void executeCheckForIdleDisable() throws InterruptedException, MessagingException {
        Thread.sleep(MAIL_CHECK_IDLE_TIME);
        int count = folder.getMessageCount();
    }

    public void terminateRealTimeCheck() {
        isRunnable = false;
    }

    @Override
    public void run() {

        boolean idleIsAvailable = true;
        while (isRunnable) {
            // IMAPFolder のインスタンスで isIdleEnable が true の時実行
            if (folder instanceof IMAPFolder) {
                IMAPFolder ifolder = (IMAPFolder) folder;
                if (idleIsAvailable) {
                    try {
                        ifolder.idle();
                    } catch (javax.mail.FolderClosedException fce) {
                        logger.log(Level.SEVERE, "IMAP Folder closed:", fce);
                        isRunnable = false;
                    } catch (MessagingException ex) {
                        if (ex.getMessage().contains("IDLE not supported")) {
                            idleIsAvailable = false;
                        } else {
                            logger.log(Level.SEVERE, "IMAP Folder & Something error occured;", ex);
                            isRunnable = false;
                        }
                    }
                } else {
                    try {
                        executeCheckForIdleDisable();
                    } catch (InterruptedException | MessagingException ime) {
                        logger.log(Level.SEVERE, "Some error occured on executeCheckForIdleDisable() : ", ime);
                        isRunnable = false;
                    }
                }
            } else {
                logger.log(Level.SEVERE, "THis is not IMAP Folder.");
                isRunnable = false;
            }
        }
    }
}
