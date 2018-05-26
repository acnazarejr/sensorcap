package ssig.btmanager;

import java.io.Serializable;

public class Message implements Serializable {

    private int messageType;
    private Serializable content;

    public Message(int messageType, Serializable content){
        this.messageType = messageType;
        this.content = content;
    }


    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public Serializable getContent() {
        return content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }

}
