package utils.json;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface JsonIO<T> {

    T read(String filePath) throws IOException, ParseException;

    void save(T data);
}

