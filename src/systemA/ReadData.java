package systemA;

public class ReadData {
    byte[] bytes;
    int id;
    long measurement;

    ReadData(byte[] bytes, int id) {
        this.bytes = bytes;
        this.id = id;
    }

    ReadData(byte[] bytes, long measurement) {
        this.bytes = bytes;
        this.measurement = measurement;
    }
}
