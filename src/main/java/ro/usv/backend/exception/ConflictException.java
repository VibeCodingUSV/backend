java
package ro.usv.backend.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}