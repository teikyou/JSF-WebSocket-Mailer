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

import java.util.Date;
import javax.faces.view.ViewScoped;
import javax.mail.Address;

/**
 *
 * @author Yoshio Terada
 */
@ViewScoped
public class MailHeader {

    private String subject;
    private Address[] fromAddress;
    private Date sendDate;
    private int size;
    private Integer messageCount;
    

    /**
     * Creates a new instance of MessageModelBean
     */
    public MailHeader() {
    }

    public MailHeader(String subject, Address[] fromAddress, Date sendDate, int size, Integer messageCount) {
        this.subject = subject;
        this.fromAddress = fromAddress;
        this.sendDate = sendDate;
        this.size = size ;
        this.messageCount = messageCount;
    }    
    
    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the fromAddress
     */
    public Address[] getFromAddress() {
        return fromAddress;
    }

    /**
     * @param fromAddress the fromAddress to set
     */
    public void setFromAddress(Address[] fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * @return the sendDate
     */
    public Date getSendDate() {
        return sendDate;
    }

    /**
     * @param sendDate the sendDate to set
     */
    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the messageCount
     */
    public Integer getMessageCount() {
        return messageCount;
    }

    /**
     * @param messageCount the messageCount to set
     */
    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}
