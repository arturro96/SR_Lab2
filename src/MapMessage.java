import java.io.Serializable;

public class MapMessage implements Serializable{
    private String key;
    private String value;
    private OperationType operationType;

    public MapMessage(String key, String value, OperationType operationType) {
        this.key = key;
        this.value = value;
        this.operationType = operationType;
    }

    public MapMessage(String key, OperationType operationType) {
        this.key = key;
        this.operationType = operationType;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
