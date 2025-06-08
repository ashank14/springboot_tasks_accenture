package spring_tasks.spring_project.exceptionHandler;


import org.springframework.boot.web.server.ErrorPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import spring_tasks.spring_project.DTO.ErrorResponseDTO;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GloabalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(NoSuchElementException ex){
        ErrorResponseDTO error=new ErrorResponseDTO(ex.getMessage(),404);
        return ResponseEntity.status(404).body(error);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleArgument(MethodArgumentNotValidException ex){
        String errorMsg=ex.getBindingResult().getFieldErrors().stream().map(error->error.getField()+" : " + error.getDefaultMessage()).collect((Collectors.joining()));
        ErrorResponseDTO error=new ErrorResponseDTO(errorMsg,400);
        return ResponseEntity.status(400).body(error);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
        ErrorResponseDTO error=new ErrorResponseDTO("Invalid input format",400);
        return ResponseEntity.status(400).body(error);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex){
        ErrorResponseDTO error=new ErrorResponseDTO("Internal Server Error",500);
        return ResponseEntity.status(500).body(error);
    }
}
