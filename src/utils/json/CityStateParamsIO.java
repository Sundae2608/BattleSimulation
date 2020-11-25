package utils.json;

import city_gen_model.CityObjectType;
import city_gen_model.CityObjects;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class CityStateParamsIO extends JsonIO<CityObjects> {

    @Override
    public CityObjects read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        CityObjects cityParams = new CityObjects();

        if (jsonObject == null) {
            return null;
        }
        JSONObject cityConfigObject = (JSONObject) jsonObject.get("city_config");


        // Get initial quantity
        JSONObject quantityObject = (JSONObject)cityConfigObject.get("initial_quantity");

        cityParams.setQuantity(CityObjectType.PERSON, getInt(quantityObject.get("population")));
        cityParams.setQuantity(CityObjectType.HOUSE, getInt(quantityObject.get("house")));

        // Get growth rate
        JSONObject growRateObject = (JSONObject)cityConfigObject.get("growth_rate");

        cityParams.setRelativeGrowthCoefficient(CityObjectType.PERSON, getDouble(growRateObject.get("population")));
        cityParams.setRelativeGrowthCoefficient(CityObjectType.HOUSE, getDouble(growRateObject.get("house")));

        // Get capacity
        JSONObject capacityObject = (JSONObject)cityConfigObject.get("capacity");

        cityParams.setCapacity(CityObjectType.PERSON, getDouble(capacityObject.get("population")));
        cityParams.setCapacity(CityObjectType.HOUSE, getDouble(capacityObject.get("house")));

        return cityParams;
    }
}
