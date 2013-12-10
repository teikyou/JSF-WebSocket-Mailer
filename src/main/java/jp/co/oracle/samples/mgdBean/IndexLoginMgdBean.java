package jp.co.oracle.samples.mgdBean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author Yoshio Terada
 */
@Named(value = "login")
@SessionScoped
public class IndexLoginMgdBean implements Serializable {

    /* * 本当ならばパスワードをセッションなんかに持たせたくない。
     * WebSocket サーバ・エンドポイントの実装側に IMAP のパスワードを
     * 渡すうまい方法があればこのセッションはいらない。すいません検討中。。。。
     * 逆をいうと SessionScoped の情報は簡単に WebSocket 側でインジェクションできます。
     */
    private String imapServer;
    private String username;
    private String password;

    private static final Logger logger = Logger.getLogger(IndexLoginMgdBean.class.getPackage().getName());

    public String checkIMAPLogin() {
        if (imapServer.isEmpty() || username.isEmpty() || password.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ログインに失敗しました。", "メール・サーバ,ユーザ名、パスワードを入力してください。"));
            return "";
        }

        try {
            initStore();
        } catch (MessagingException ex) {
            logger.log(Level.INFO, null, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ログインに失敗しました。", "メール・サーバのログインに失敗しました。"));
            return "";
        }
        return "/received/folders-show.xhtml?faces-redirect=true";

    }

    // 接続確認
    private void initStore() throws MessagingException {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect(imapServer, username, password);
        store.close();
    }

    /**
     * Creates a new instance of IndexLoginMgdBean
     */
    public IndexLoginMgdBean() {
    }

    /**
     * @return the imapServer
     */
    public String getImapServer() {
        return imapServer;
    }

    /**
     * @param imapServer the imapServer to set
     */
    public void setImapServer(String imapServer) {
        this.imapServer = imapServer;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
