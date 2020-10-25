package city_gen_model.algorithms.geometry.house_generation;

public class HouseSizeSettings {
    private double houseWidth;
    private double houseWidthWiggle;
    private double houseArea;
    private double houseAreaWiggle;

    public HouseSizeSettings(double houseWidth, double houseWidthWiggle, double houseArea, double houseAreaWiggle) {
        this.houseWidth = houseWidth;
        this.houseWidthWiggle = houseWidthWiggle;
        this.houseArea = houseArea;
        this.houseAreaWiggle = houseAreaWiggle;
    }

    public double getHouseWidth() {
        return houseWidth;
    }
    public void setHouseWidth(double houseWidth) {
        this.houseWidth = houseWidth;
    }

    public double getHouseWidthWiggle() {
        return houseWidthWiggle;
    }
    public void setHouseWidthWiggle(double houseWidthWiggle) {
        this.houseWidthWiggle = houseWidthWiggle;
    }

    public double getHouseArea() {
        return houseArea;
    }
    public void setHouseArea(double houseArea) {
        this.houseArea = houseArea;
    }

    public double getHouseAreaWiggle() {
        return houseAreaWiggle;
    }
    public void setHouseAreaWiggle(double houseAreaWiggle) {
        this.houseAreaWiggle = houseAreaWiggle;
    }
}
