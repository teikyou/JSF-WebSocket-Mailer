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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import jp.co.oracle.samples.msgutil.MessageDumpUtil;

/**
 *
 * @author tyoshio2002
 */
public class MessageEncoder implements Encoder.Text<Message> {

    private final static int SUMMARY_SIZE = 80;

    private static final Logger logger = Logger.getLogger(MessageEncoder.class.getPackage().getName());
    
    @Override
    public String encode(Message msg) throws EncodeException {
        try {
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
            return stWriter.toString();
        } catch (MessagingException | IOException ex) {
            EncodeException ee = new EncodeException(msg, "Encode failed ", ex);
            logger.log(Level.SEVERE, null, ex);
            throw ee;
        }
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }
}
