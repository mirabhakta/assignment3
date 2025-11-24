package model;

// Runtime exception used for handling API-related errors.
// Stores both an error message and a HTTP status code (optional).

public class APIException extends RuntimeException {
    private final int status;

    // Creates an APIException with only a message
    public APIException(String message) {
        super(message);
        this.status = -1;
    }

    // Creates an APIException with both a status code and a message
    public APIException(int status, String message) {
        super(message);
        this.status = status;
    }

    // Returns the HTTP status code associated with this exception
    public int getStatus() { return status; }
}
