package city_gen_model;

import java.util.HashMap;
import java.util.Map;

/**
 * Store quantities, relative growth as well as capacity for each of the CityObjectType
 */
public class CityObjects {
    Map<CityObjectType, Double> quantityMap;
    Map<CityObjectType, Double> relativeGrowthCoefficientMap; // Used for logistic function
    Map<CityObjectType, Double> capacityMap; // Used for logistic function

    public CityObjects() {
        quantityMap = new HashMap<>();
        relativeGrowthCoefficientMap = new HashMap<>();
        capacityMap = new HashMap<>();

        for (CityObjectType cityObjectType : CityObjectType.values()) {
            quantityMap.put(cityObjectType, 0.0);
            relativeGrowthCoefficientMap.put(cityObjectType, 0.0);
            capacityMap.put(cityObjectType, 0.0);
        }
    }

    public double getQuantity(CityObjectType paramType) {
        return quantityMap.get(paramType);
    }

    public void setQuantity(CityObjectType paramType, double value) {
        quantityMap.put(paramType, value);
    }

    public double getRelativeGrowthCoefficient(CityObjectType paramType) {
        return relativeGrowthCoefficientMap.get(paramType);
    }

    public void setRelativeGrowthCoefficient(CityObjectType paramType, double value) {
        relativeGrowthCoefficientMap.put(paramType, value);
    }

    public double getCapacity(CityObjectType paramType) {
        return capacityMap.get(paramType);
    }

    public void setCapacity(CityObjectType paramType, double value) {
        capacityMap.put(paramType, value);
    }
}
