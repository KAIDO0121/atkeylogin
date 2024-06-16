package ATKeyLogin.backend.model.exception;

public class BusinessLogicException extends Exception {
    public BusinessLogicException(String errorMessage) {
        super(errorMessage);
    }
}