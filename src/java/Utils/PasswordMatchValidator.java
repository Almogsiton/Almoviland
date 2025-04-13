package Utils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Custom JSF Validator to verify that the password and confirm password fields match.
 * Should be applied on the confirm password input field.
 */
@FacesValidator("passwordMatchValidator")
public class PasswordMatchValidator implements Validator {

    /**
     * Validates whether the confirm password field matches the password field.
     *
     * @param context   The FacesContext instance.
     * @param component The UIComponent associated with the confirm password field.
     * @param value     The value entered in the confirm password field.
     * @throws ValidatorException if passwords do not match.
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIComponent passwordComponent = component.findComponent("password");
        String password = (String) passwordComponent.getAttributes().get("value");
        String confirmPassword = (String) value;

        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match!", null)
            );
        }
    }
}