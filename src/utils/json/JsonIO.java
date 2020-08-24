package utils.json;
import java.io.IOException;

public interface JsonIO<T> {

    T read(String filePath) throws IOException;

    void save(T data, String filePath) throws IOException;
}

