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
