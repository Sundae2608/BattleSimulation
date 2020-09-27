package utils.json;
import java.io.IOException;

public abstract class JsonIO<T> {

    public T read(String filePath) throws IOException {
        return null;
    }

    protected String getString(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).toString();
        } else {
            return (String) obj;
        }
    }

    protected Double getDouble(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else {
            return (Double) obj;
        }
    }

    protected Float getFloat(Object obj) {
        if (obj instanceof Long) {
            return ((Long) obj).floatValue();
        } else if (obj instanceof Double) {
            return ((Double) obj).floatValue();
        } else {
            return (Float) obj;
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

