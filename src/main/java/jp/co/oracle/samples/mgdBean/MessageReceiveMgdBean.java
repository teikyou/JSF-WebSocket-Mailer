package jp.co.oracle.samples.mgdBean;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import jp.co.oracle.samples.tasks.AllFolderHandlerTask;
import jp.co.oracle.samples.beans.FolderName;
import jp.co.oracle.samples.beans.MailHeader;
import jp.co.oracle.samples.beans.MailHeaderModel;
import jp.co.oracle.samples.tasks.SpecifiedMessageHandlerTask;
import jp.co.oracle.samples.tasks.SpecifiedNodeMailHeaderHandleTask;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Yoshio Terada
 */
@Named(value = "messageReceiveMgdBean")
@ViewScoped
public class MessageReceiveMgdBean implements Serializable {

    private Store store;
    private TreeNode root;
    private TreeNode selectedNode;
    private MailHeader selectedMailHeader;
    private MailHeaderModel mailHeaderModel;
    private String folderFullName;
    private String specifiedMessage;
    private int numberOfMessages = DEFAULT_NUMBER_OF_MESSAGE;
    private final static int DEFAULT_NUMBER_OF_MESSAGE = 10;

    private static final Logger logger = Logger.getLogger(MessageReceiveMgdBean.class.getPackage().getName());

    @Inject
    IndexLoginMgdBean login;

    @Resource
    ManagedExecutorService execService;

    /* コンストラクタ */
    public MessageReceiveMgdBean() {

    }

    /* 
      ページのロード時に IMAP サーバ上の全フォルダを取得
      INBOX の最新メッセージ10件のサブジェクト等を取得
      最新のメッセージを取得
      それぞれ並列で処理を実行し結果表示を少し改善
    */
    public void onPageLoad() {
        String imapServer = login.getImapServer();
        String username = login.getUsername();
        String password = login.getPassword();
        initStore(imapServer, username, password);

        if (getRoot() == null) {
            //全フォルダリストの取得
            Future<TreeNode> folderHandlesubmit = execService.submit(new AllFolderHandlerTask(store));
            int num = getNumberOfMessages();
            if (num == 0) {
                num = DEFAULT_NUMBER_OF_MESSAGE;
            }
            // デフォルトで INBOX のメッセージの取得
            folderFullName = "INBOX";
            Future<MailHeaderModel> headerHandlerSubmit = execService.submit(new SpecifiedNodeMailHeaderHandleTask(store, folderFullName, num));
            Future<String> messageHandlerSubmit = null;
            try {
                // デフォルトで INBOX の最新のメッセージ取得
                messageHandlerSubmit = execService.submit(new SpecifiedMessageHandlerTask(store, folderFullName, store.getFolder(folderFullName).getMessageCount()));
            } catch (MessagingException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            try {
                //左ペインのツリーの一覧を設定
                root = folderHandlesubmit.get();
                //右ペインのテーブルの設定
                MailHeaderModel mailmodel = headerHandlerSubmit.get();
                setMailHeaderModel(mailmodel);
                List<MailHeader> headers = mailmodel.getAllHeader();
                //デフォルトで最新のメッセージを選択された状態に設定

                //ここは並列処理しているのでバグがあります (^_^;)
                //画面右下で実際に描画しているメッセージの MessageのID をもとに
                //選択状態にすべきです。
                //取り急ぎ、デフォルトでメッセージを選択状態にできる事を
                //示したかったので、今このように簡単に実装しています。
                if (headers != null && !headers.isEmpty()) {
                    MailHeader latestMailHeader = headers.get(0);
                    selectedMailHeader = latestMailHeader;
                }

                if (messageHandlerSubmit != null) {
                    specifiedMessage = messageHandlerSubmit.get();
                }
            } catch (InterruptedException | ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    // ツリーが選択された際に呼び出されるイベント
    public void onNodeSelect(NodeSelectEvent event) {
        folderFullName = ((FolderName) selectedNode.getData()).getFullName();
        int num = getNumberOfMessages();
        if (num == 0) {
            num = DEFAULT_NUMBER_OF_MESSAGE;
        }
        // 選択したフォルダのメールヘッダを更新
        Future<MailHeaderModel> headerHandlerSubmit = execService.submit(new SpecifiedNodeMailHeaderHandleTask(store, folderFullName, num));

        // 選択したフォルダの最新メッセージを取得
        try {
            MailHeaderModel mailmodel = headerHandlerSubmit.get();
            //メールヘッダの更新
            setMailHeaderModel(mailmodel);
            // 最新のメッセージ取得
            Future<String> messageHandlerSubmit = execService.submit(new SpecifiedMessageHandlerTask(store, folderFullName, store.getFolder(folderFullName).getMessageCount()));
            specifiedMessage = messageHandlerSubmit.get();

            List<MailHeader> headers = mailmodel.getAllHeader();
            //デフォルトで最新のメッセージを選択された状態に設定

            //ここは並列処理しているのでバグがあります (^_^;)
            //画面右下で実際に描画しているメッセージの MessageのID をもとに
            //選択状態にすべきです。
            //取り急ぎ、デフォルトでメッセージを選択状態にできる事を
            //示したかったので、今このように簡単に実装しています。
            if (headers != null && !headers.isEmpty()) {
                MailHeader latestMailHeader = headers.get(0);
                selectedMailHeader = latestMailHeader;
            }
        } catch (MessagingException | InterruptedException | ExecutionException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    // メッセージが選択された際に呼び出されるイベント
    public void onMessageSelect(SelectEvent event) {

        int msgCount = ((MailHeader) event.getObject()).getMessageCount();
        try {
            Future<String> messageHandlerSubmit = execService.submit(new SpecifiedMessageHandlerTask(store, folderFullName, msgCount));
            specifiedMessage = messageHandlerSubmit.get();
        } catch (InterruptedException | ExecutionException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    // メッセージのカウンタが更新された際の処理
    // 10 よりしたの値が入力された場合、何もしない。
    public void updateMessageCount() {
        String folderName = folderFullName;
        if (folderName.isEmpty()) {
            folderName = "INBOX";
        }
        int num = getNumberOfMessages();
        if (num > 10) {
            Future<MailHeaderModel> headerHandlerSubmit = execService.submit(new SpecifiedNodeMailHeaderHandleTask(store, folderName, num));
            try {
                setMailHeaderModel(headerHandlerSubmit.get());
            } catch (InterruptedException | ExecutionException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    // Store の初期化（ページのロード時）
    private void initStore(String imapServer, String username, String password) {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        javax.mail.Store initStore;
        try {
            Session session = Session.getDefaultInstance(props, null);
            initStore = session.getStore("imaps");
            initStore.connect(imapServer, username, password);
            this.store = initStore;
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the selectedNode
     */
    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    /**
     * @param selectedNode the selectedNode to set
     */
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    /**
     * @return the selectedMailHeader
     */
    public MailHeader getSelectedMailHeader() {
        return selectedMailHeader;
    }

    /**
     * @param selectedMailHeader the selectedMailHeader to set
     */
    public void setSelectedMailHeader(MailHeader selectedMailHeader) {
        this.selectedMailHeader = selectedMailHeader;
    }

    /**
     * @return the mailHeaderModel
     */
    public MailHeaderModel getMailHeaderModel() {
        return mailHeaderModel;
    }

    /**
     * @param mailHeaderModel the mailHeaderModel to set
     */
    public void setMailHeaderModel(MailHeaderModel mailHeaderModel) {
        this.mailHeaderModel = mailHeaderModel;
    }

    /**
     * @return the specifiedMessage
     */
    public String getSpecifiedMessage() {
        return specifiedMessage;
    }

    /**
     * @param specifiedMessage the specifiedMessage to set
     */
    public void setSpecifiedMessage(String specifiedMessage) {
        this.specifiedMessage = specifiedMessage;
    }

    /**
     * @return the numberOfMessages
     */
    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * @param numberOfMessages the numberOfMessages to set
     */
    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    /**
     * @return the root
     */
    public TreeNode getRoot() {
        return root;
    }

}
