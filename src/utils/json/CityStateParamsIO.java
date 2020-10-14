package utils.json;

import city_gen_model.CityStateParameters;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class CityStateParamsIO extends JsonIO<CityStateParameters> {

    @Override
    public CityStateParameters read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        JSONObject config = (JSONObject) jsonObject.get("city_config");

        CityStateParameters cityParams = new CityStateParameters();
        cityParams.setPopulation(getInt(config.get("population")));
        cityParams.setNumHouses(getInt(config.get("num_houses")));

        return cityParams;
    }
}
