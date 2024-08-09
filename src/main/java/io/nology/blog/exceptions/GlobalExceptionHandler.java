package io.nology.blog.exceptions;

import java.nio.channels.Pipe.SourceChannel;
import java.util.HashMap;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.nology.blog.common.ConstraintMetadataService;

@ControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private ConstraintMetadataService constraintMetadataService;

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), NotFoundException.getStatuscode());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), BadRequestException.getStatuscode());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ValidationErrors> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ValidationErrors validationErrors = new ValidationErrors();

        String message = ex.getMostSpecificCause().getMessage();
        logger.info("DataIntegrityViolationException message: " + message);

        HashMap<String, String> dbErrorsMap = constraintMetadataService
                .getDatabaseErrorsForUniqueConstraintViolation(message);
        if (!dbErrorsMap.isEmpty()) {
            validationErrors.addError(dbErrorsMap.get("column"), String.format(
                    "%s must be unique. '%s' has already been used", dbErrorsMap.get("column"),
                    dbErrorsMap.get("value")));
        }

        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

}
