package utils.json;

import city_gen_model.CityParamType;
import city_gen_model.CityStateParameters;
import org.json.simple.JSONArray;
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

        CityStateParameters cityParams = new CityStateParameters();

        JSONObject cityConfigObject = (JSONObject) jsonObject.get("city_config");


        // Get initial quantity
        JSONObject quantityObject = (JSONObject)cityConfigObject.get("initial_quantity");

        cityParams.setQuantity(CityParamType.PERSON, getInt(quantityObject.get("population")));
        cityParams.setQuantity(CityParamType.HOUSE, getInt(quantityObject.get("house")));

        // Get growth rate
        JSONObject growRateObject = (JSONObject)cityConfigObject.get("growth_rate");

        cityParams.setRelativeGrowthCoefficient(CityParamType.PERSON, getDouble(growRateObject.get("population")));
        cityParams.setRelativeGrowthCoefficient(CityParamType.HOUSE, getDouble(growRateObject.get("house")));

        // Get capacity
        JSONObject capacityObject = (JSONObject)cityConfigObject.get("capacity");

        cityParams.setCapacity(CityParamType.PERSON, getDouble(capacityObject.get("population")));
        cityParams.setCapacity(CityParamType.HOUSE, getDouble(capacityObject.get("house")));

        return cityParams;
    }
}
