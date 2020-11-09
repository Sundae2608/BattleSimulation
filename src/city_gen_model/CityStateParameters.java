package city_gen_model;

import java.util.HashMap;
import java.util.Map;

public class CityStateParameters {
    Map<CityParamType, Double> quantityMap;

    public CityStateParameters() {
        quantityMap = new HashMap<>();
        for (CityParamType cityParamType : CityParamType.values()) {
            quantityMap.put(cityParamType, 0.0);
        }
    }

    public double getQuantity(CityParamType paramType) {
        return quantityMap.get(paramType);
    }

    public void setQuantity(CityParamType paramType, double value) {
        quantityMap.put(paramType, value);
    }
}
