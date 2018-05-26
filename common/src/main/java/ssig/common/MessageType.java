package ssig.common;

public enum MessageType {

    INVALID_MESSAGE(0),

    REQUEST_SENSOR_INFO(1);


    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static MessageType byCode(int code) {
        for (MessageType messageType : MessageType.values()) {
            if (code == messageType.code())
                return messageType;
        }
        throw new IllegalArgumentException("invalid code");
    }
}