package jp.co.oracle.samples.beans;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import org.primefaces.model.SelectableDataModel;

/**
 *
 * @author Yoshio Terada
 */
@ViewScoped
public class MailHeaderModel extends ListDataModel<MailHeader> implements SelectableDataModel<MailHeader> {

    List<MailHeader> header;
    
    public MailHeaderModel() {
    }

    public MailHeaderModel(List<MailHeader> header) {
        super(header);
        this.header = header;
        // 最新のメッセージが最初に表示されるようにソート
        Collections.sort(header, new Comparator<MailHeader>() {
            @Override
            public int compare(MailHeader m1, MailHeader m2) {
                return m2.getMessageCount() - m1.getMessageCount();
            }
        });
    }

    @Override
    public Object getRowKey(MailHeader header) {
        return header.getMessageCount();
    }

    @Override
    public MailHeader getRowData(String rowKey) {
        List<MailHeader> headers = (List<MailHeader>) getWrappedData();

        for (MailHeader header : headers) {
            if (header.getMessageCount().toString().equals(rowKey)) {
                return header;
            }
        }
        return null;
    }
    
    public List<MailHeader> getAllHeader(){
        return header;
    }
    
}
