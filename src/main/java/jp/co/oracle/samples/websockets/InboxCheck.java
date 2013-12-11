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
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import jp.co.oracle.samples.msgutil.MessageDumpUtil;
import jp.co.oracle.samples.mgdBean.IndexLoginMgdBean;
import jp.co.oracle.samples.tasks.InboxCheckRunnableTask;

/**
 *
 * @author Yoshio Terada
 */
@ServerEndpoint("/inbox-check")
public class InboxCheck {

    private final static int SUMMARY_SIZE = 80;
    private Store store;
    private final static int MAX_RETRY_COUNT = 3;

    private static final Logger logger = Logger.getLogger(InboxCheck.class.getPackage().getName());

    @Resource
    ManagedThreadFactory threadFactory;
    
    InboxCheckRunnableTask invokeCheck = null;
    
    
    @Inject
    IndexLoginMgdBean login;

    @OnOpen
    public void onOpen(javax.websocket.Session session) {
        logger.log(Level.INFO, "Connection is opened: {0}", session.getId());
        initStore(login.getImapServer(), login.getUsername(), login.getPassword());
        checkNewMessage(session);
    }

    @OnClose
    public void onClose(javax.websocket.Session session) {
        if(invokeCheck != null){
            invokeCheck.terminateRealTimeCheck();
        }
        logger.log(Level.INFO, "Connection is closed: {0}", session.getId());
    }

    @OnError
    public void onError(Throwable t) {
        logger.log(Level.SEVERE, null, t);
    }

    private void checkNewMessage(final javax.websocket.Session session) {
        try {
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
                        String jsonString = convertJSonString(msg);
                        System.out.println(jsonString);
                        // WebSocket のクライアント・エンドポイントに送信
                        session.getBasicRemote().sendText(jsonString);
                    } catch (MessagingException | IOException ex) {
                        logger.log(Level.SEVERE, "WebSocket Send failed:", ex);
                    }
                }
            });
            // 別スレッドでメッセージの到着を監視
            newInboxCheckThreadWithRetryCount(folder);
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void newInboxCheckThreadWithRetryCount(Folder folder) {
        invokeCheck = new InboxCheckRunnableTask(folder);
        Thread runTask = threadFactory.newThread(invokeCheck);
        runTask.start();
    }

    // Message オブジェクトから JSON の文字列を生成
    private String convertJSonString(Message msg) throws MessagingException, IOException {
        // Address[] から JSon 配列を作成
        Address[] addresses = msg.getFrom();
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (Address adres : addresses) {
            InternetAddress iAddress = (InternetAddress) adres;
            array.add(iAddress.toUnicodeString());
        }

        // メッセージ・サマリを取得・作成
        MessageDumpUtil dumpUtil = new MessageDumpUtil();
        String msgSummary = dumpUtil.getText(msg);
        if (!msgSummary.isEmpty() && msgSummary.length() > SUMMARY_SIZE) {
            String tmp = msgSummary.replaceAll("(\r|\n)", "");
            msgSummary = tmp.substring(0, SUMMARY_SIZE);
            msgSummary = msgSummary + "  ......";
        }

        //JSon オブジェクトを生成
        JsonObject model = Json.createObjectBuilder()
                .add("subject", msg.getSubject())
                .add("address", array)
                .add("summary", msgSummary)
                .build();
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(model);
        }
        String jsonString = stWriter.toString();
        return jsonString;
    }

    // Store の初期化（ページのロード時）
    private void initStore(String imapServer, String username, String password) {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        javax.mail.Store initStore = null;
        try {
            Session session = Session.getDefaultInstance(props, null);
            initStore = session.getStore("imaps");
            initStore.connect(imapServer, username, password);
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        this.store = initStore;
    }
}
