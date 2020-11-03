package city_gen_model;

import java.util.HashMap;
import java.util.Map;

public class CityStateParameters {
    Map<CityParamType, Integer> quantityMap;

    public CityStateParameters() {
        quantityMap = new HashMap<>();
        for (CityParamType cityParamType : CityParamType.values()) {
            quantityMap.put(cityParamType, 0);
        }
    }

    public int getQuantity(CityParamType paramType) {
        return quantityMap.get(paramType);
    }

    public void setQuantity(CityParamType paramType, int value) {
        quantityMap.put(paramType, value);
    }
}
