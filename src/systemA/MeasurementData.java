package systemA;

public class MeasurementData {
    public byte[] bytes;
    public long measurement;

    MeasurementData(ReadData readData) {
        this.bytes = readData.bytes;
        this.measurement = readData.measurement;
    }
}
