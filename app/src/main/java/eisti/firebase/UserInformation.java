package eisti.firebase;

public class UserInformation {

    private String name;
    private int cbCode;

    public UserInformation() {

    }

    public UserInformation(String name, int cbCode) {
        this.name = name;
        this.cbCode = cbCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCbCode() {
        return cbCode;
    }

    public void setCbCode(int cbCode) {
        this.cbCode = cbCode;
    }
}
