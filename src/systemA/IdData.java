package systemA;

public class IdData {
    public byte[] bytes;
    public int id;

    IdData(ReadData readData) {
        this.bytes = readData.bytes;
        this.id = readData.id;
    }
}
