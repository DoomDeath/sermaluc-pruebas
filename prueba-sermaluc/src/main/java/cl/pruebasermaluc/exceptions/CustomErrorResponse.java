package cl.pruebasermaluc.exceptions;


import lombok.Data;

@Data
public class CustomErrorResponse {


    private int code;
    private String message;

    public CustomErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }



}
