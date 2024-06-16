package ATKeyLogin.backend.model;

public enum LicenseStateEnum {
    ACTIVE(0),
    INACTIVE(1),
    SUSPEND(2),
    EXPIRED(3),
    TRIAL(4),
    DELETED(5);

    private final int stateValue;

    LicenseStateEnum(int stateValue) {
        this.stateValue = stateValue;
    }

    public int valueOf() {
        return stateValue;
    }
}
