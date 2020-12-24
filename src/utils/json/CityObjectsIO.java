package utils.json;

import city_gen_model.CityObjectType;
import city_gen_model.CityObjects;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class CityObjectsIO extends JsonIO<CityObjects> {

    @Override
    public CityObjects read(String filePath) {
        // Initialize the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)new JSONParser().parse(new FileReader(filePath));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        CityObjects cityObjects = new CityObjects();

        if (jsonObject == null) {
            return null;
        }
        JSONObject cityConfigObject = (JSONObject) jsonObject.get("city_config");


        // Get initial quantity
        JSONObject quantityObject = (JSONObject)cityConfigObject.get("initial_quantity");

        cityObjects.setQuantity(CityObjectType.PERSON, getInt(quantityObject.get("population")));
        cityObjects.setQuantity(CityObjectType.HOUSE, getInt(quantityObject.get("house")));

        // Get growth rate
        JSONObject growRateObject = (JSONObject)cityConfigObject.get("growth_rate");

        cityObjects.setRelativeGrowthCoefficient(CityObjectType.PERSON, getDouble(growRateObject.get("population")));
        cityObjects.setRelativeGrowthCoefficient(CityObjectType.HOUSE, getDouble(growRateObject.get("house")));

        // Get capacity
        JSONObject capacityObject = (JSONObject)cityConfigObject.get("capacity");

        cityObjects.setCapacity(CityObjectType.PERSON, getDouble(capacityObject.get("population")));
        cityObjects.setCapacity(CityObjectType.HOUSE, getDouble(capacityObject.get("house")));

        return cityObjects;
    }
}
