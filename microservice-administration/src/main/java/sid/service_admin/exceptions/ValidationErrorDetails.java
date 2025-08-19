/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.exceptions;

/**
 *
 * @author USER01
 */
import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorDetails extends ErrorDetails {
    private Map<String, String> validationErrors;
    
    public ValidationErrorDetails(LocalDateTime timestamp, String message, String details, 
                             int status, Map<String, String> validationErrors) {
        super(timestamp, message, details, status);
        this.validationErrors = validationErrors;
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }    
}
