package cl.pruebasermaluc.model;


import lombok.Data;

@Data
public class ArchivoResponse {

    private int code;
    private String message;

    public ArchivoResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
