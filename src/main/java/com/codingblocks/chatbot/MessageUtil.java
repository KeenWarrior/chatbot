package com.codingblocks.chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    public static Map processMessage(Map message) {

        Map<String, Object> response = new HashMap<>();

        if(message.containsKey("text")){
            response.put("text", "please ask me to tell a joke");
        }

        if(message.containsKey("attachments")){
            ArrayList attachments = (ArrayList) message.get("attachments");
            attachments.forEach( a  -> {
                Map attachment = (Map) a;
                if (attachment.get("type").equals("image")){
                    Map payload = (Map) attachment.get("payload");
                    payload.put("url", ImageUtil.processImage(payload.get("url").toString()));
                    response.put("attachment", attachment);
                }
            });
        }

        return response;
    }
}
