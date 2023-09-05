package cl.pruebasermaluc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.xml.bind.ValidationException;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {



    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CustomErrorResponse> handleValidationException(ValidationException ex) {
        // Define el código y el mensaje de error personalizado para excepciones de validación
        int code = Integer.parseInt(ex.getErrorCode());
        String message = ex.getMessage();

        // Crea un objeto JSON con el formato deseado
        CustomErrorResponse errorResponse = new CustomErrorResponse(code, message);

        // Devuelve la respuesta con el objeto JSON en el cuerpo y el código de estado correspondiente
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        // Define el código y el mensaje de error personalizado
        int code = 500; // Puedes establecer el código de error que desees
        String message = "Error interno del servidor: " + ex.getMessage();

        // Crea un objeto JSON con el formato deseado
        CustomErrorResponse errorResponse = new CustomErrorResponse(code, message);

        // Devuelve la respuesta con el objeto JSON en el cuerpo
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
