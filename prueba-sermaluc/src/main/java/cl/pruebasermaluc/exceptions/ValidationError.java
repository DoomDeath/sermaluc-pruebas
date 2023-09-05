package cl.pruebasermaluc.exceptions;


import lombok.Data;

@Data
public class ValidationError {

    private int code;
    private String message;

    public ValidationError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
