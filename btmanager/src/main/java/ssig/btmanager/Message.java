package ssig.btmanager;

import java.io.Serializable;

public class Message implements Serializable {

    private int code;
    private Serializable content;

    public Message(int code, Serializable content){
        this.code = code;
        this.content = content;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Serializable getContent() {
        return content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }

}
