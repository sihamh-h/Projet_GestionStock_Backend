package fr.doranco.GestionStock.Exceptions;


import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.relation.RoleNotFoundException;
import java.net.URI;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Validation des annotations @Valid sur User
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .toList();
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Error");
        pd.setDetail("One or more input fields are invalid.");
        pd.setProperty("errors", errors);
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.badRequest().body(pd);
    }

    //JSON invalide ou enum incorrect
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleJsonError(HttpMessageNotReadableException ex, HttpServletRequest request){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Requête JSON invalide");
        problemDetail.setDetail("Invalid JSON request or wrong data format");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(problemDetail);
    }

    //role non trouvé
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problemDetail.setTitle("Role Not Found");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    // champ existe deja
   @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Bad Request");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", ex.getMessage());

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(AdminAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleAdminAlreadyExists(AdminAlreadyExistsException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problemDetail.setTitle("Conflict");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problemDetail.setTitle("User Not Found");
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problemDetail.setTitle("Authentication Failed");
        problemDetail.setDetail("Invalid email or password");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", "incorrect email or password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ProblemDetail> handleDisabledException(DisabledException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problemDetail.setTitle("Authentication Failed");
        problemDetail.setDetail("User account is disabled");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("error", "User account is disabled!");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<?> handleMonErreurDeType(IllegalAccessException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "statut", "Security Exception",
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleSignature(SignatureException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid token signature: " + e.getMessage()));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> handleMalformedJwt(MalformedJwtException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Malformed JWT",
                        "message", "The provided JWT is not well-formed: " + e.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }




}
