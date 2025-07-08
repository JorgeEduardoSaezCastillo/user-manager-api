package com.evaluacion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones personalizadas de validación.
     *
     * @param ex la excepción ValidationException lanzada
     * @return ResponseEntity con un mensaje de error personalizado
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handlerValidationException (ValidationException ex){
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de validación de parámetros anotados con @Valid.
     *
     * @param ex la excepción lanzada al fallar una validación
     * @return ResponseEntity con errores por campo en formato JSON
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(Map.of("mensaje", errores.toString()));
    }

    /**
     * Maneja errores cuando un parámetro UUID es inválido.
     *
     * @param ex excepción lanzada por un tipo de argumento incorrecto
     * @return ResponseEntity con mensaje de error claro
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleUUIDFormatError(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == UUID.class) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El ID no tiene un formato UUID válido"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", "Parámetro inválido"));
    }

    /**
     * Maneja errores por parámetros requeridos faltantes en la solicitud.
     *
     * @param ex excepción de parámetro faltante
     * @return ResponseEntity con mensaje explicativo
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParams(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Falta el parámetro requerido: " + paramName));
    }

    /**
     * Maneja cualquier excepción inesperada.
     *
     * @param ex la excepción no manejada
     * @return ResponseEntity con mensaje genérico de error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno en el servidor"));
    }
}
