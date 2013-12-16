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
package jp.co.oracle.samples.websockets;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import jp.co.oracle.samples.mgdBean.IndexLoginMgdBean;
import jp.co.oracle.samples.tasks.InboxCheckRunnableTask;

/**
 *
 * @author Yoshio Terada
 */
@ServerEndpoint(value = "/inbox-check",
        encoders = {MessageEncoder.class})
public class InboxCheck {

    private Store store;

    private static final Logger logger = Logger.getLogger(InboxCheck.class.getPackage().getName());

    @Resource
    ManagedThreadFactory threadFactory;

    InboxCheckRunnableTask invokeCheck = null;

    @Inject
    IndexLoginMgdBean login;

    @OnOpen
    public void onOpen(javax.websocket.Session session) {
        try {
            initStore(login.getImapServer(), login.getUsername(), login.getPassword());
            checkNewMessage(session);
        } catch (MessagingException mes) {
            try {
                logger.log(Level.SEVERE, "Exception occured on monitoring INBOX ", mes);
                session.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to close session", ex);
            }
        }
    }

    @OnClose
    public void onClose(javax.websocket.Session session) {
        if (invokeCheck != null) {
            invokeCheck.terminateRealTimeCheck();
        }
    }

    @OnError
    public void onError(Throwable t) {
        logger.log(Level.SEVERE, "Error Occured", t);
    }

    private void checkNewMessage(final javax.websocket.Session session) throws MessagingException {

        // INBOX のフォルダを対象
        Folder folder = store.getFolder("INBOX");
        if (!folder.isOpen()) {
            folder.open(javax.mail.Folder.READ_WRITE);
        }
        // フォルダのメッセージ・カウント数を監視
        folder.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                Message[] msgs = e.getMessages();
                Message msg = msgs[msgs.length - 1];
                try {
                    // WebSocket のクライアント・エンドポイントに送信
                    session.getBasicRemote().sendObject(msg);
                } catch (IOException | EncodeException ioencx) {
                    logger.log(Level.SEVERE, "Failed to Send Message ", ioencx);
                }
            }
        });
        // 別スレッドでメッセージの到着を監視
        newInboxCheckThreadWithRetryCount(folder);
    }

    private void newInboxCheckThreadWithRetryCount(Folder folder) {
        invokeCheck = new InboxCheckRunnableTask(folder);
        Thread runTask = threadFactory.newThread(invokeCheck);
        runTask.start();
    }

    // Store の初期化（ページのロード時）
    private void initStore(String imapServer, String username, String password) throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);
        javax.mail.Store initStore = session.getStore("imaps");
        initStore.connect(imapServer, username, password);

        this.store = initStore;
    }
}
