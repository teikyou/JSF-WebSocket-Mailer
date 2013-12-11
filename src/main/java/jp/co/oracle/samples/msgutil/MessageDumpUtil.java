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

package jp.co.oracle.samples.msgutil;

import com.sun.mail.util.BASE64DecoderStream;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

/**
 *
 * @author Yoshio Terada
 */
public class MessageDumpUtil {

    private boolean textIsHtml = false;

    public String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            textIsHtml = p.isMimeType("text/html");
            if (true == textIsHtml) {
                return (String) p.getContent();
            } else {
                return getPreString((String) p.getContent());
            }
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.getContent() instanceof BASE64DecoderStream) {
                    return "現在 Base 64 のコンテンツには現在未対応です。";
                } else if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getPreString(getText(bp));
                    }
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return getPreString(getText(bp));
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }

        return null;
    }

    private String getPreString(String data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<PRE style=\"font-size:12px;\">");
        sb.append(data);
        sb.append("</PRE>");
        String s = sb.toString();
        return s;
    }
}
