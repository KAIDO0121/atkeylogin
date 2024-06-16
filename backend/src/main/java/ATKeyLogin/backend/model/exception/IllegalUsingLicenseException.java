package ATKeyLogin.backend.model.exception;

public class IllegalUsingLicenseException extends RuntimeException {
    private final long userId;
    public IllegalUsingLicenseException(String message, long userId) {
        super(message);
        this.userId = userId;
    }

    public long getActiveUser() {
        return userId;
    }
}
