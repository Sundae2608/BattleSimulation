package utils.json;
import java.io.IOException;

public abstract class JsonIO<T> {

    public T read(String filePath) throws IOException {
        return null;
    }

    protected Double getDouble(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else {
            return (Double) obj;
        }
    }

    protected Integer getInt(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        } else {
            return (Integer) obj;
        }
    }
}

