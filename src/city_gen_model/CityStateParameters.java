package city_gen_model;

public class CityStateParameters {
    private int population;
    private int numHouses;
    private int numFarms;
    private int numMarkets;
    private int numSchools;
    private int numReligiousBuildings;
    private int numGovernmentBuildings;
    private int numFactories;

    public int getQuantity(CityParamType paramType) {
        switch (paramType) {
            case PERSON:
                return population;
            case HOUSE:
                return numHouses;
            case MARKET:
                return numMarkets;
            case FARM:
                return numFarms;
            case SCHOOL:
                return numSchools;
            case RELIGIOUS_BUILDING:
                return numReligiousBuildings;
            case GOVERNMENT_BUILDING:
                return numGovernmentBuildings;
            case FACTORY:
                return numFactories;
        }
        return 0;
    }

    public void setQuantity(CityParamType paramType, int value) {
        switch (paramType) {
            case PERSON:
                population = value;
                break;
            case HOUSE:
                numHouses = value;
                break;
            case MARKET:
                numMarkets = value;
                break;
            case FARM:
                numFarms = value;
                break;
            case SCHOOL:
                numSchools = value;
                break;
            case RELIGIOUS_BUILDING:
                numReligiousBuildings = value;
                break;
            case GOVERNMENT_BUILDING:
                numGovernmentBuildings = value;
                break;
            case FACTORY:
                numFactories = value;
                break;
        }
    }
}
