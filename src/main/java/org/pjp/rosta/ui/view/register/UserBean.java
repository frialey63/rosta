package org.pjp.rosta.ui.view.register;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Main Bean class that we build the form for.
 * <p>
 * Uses Bean Validation (JSR-303) annotations for automatic validation.
 */
public class UserBean {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private boolean employee;

    @Size(min = 8, max = 20, message = "Password must be 8-20 char long")
    private String password;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEmployee() {
        return employee;
    }

    public void setEmployee(boolean allowsMarketing) {
        this.employee = allowsMarketing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBean that = (UserBean) o;
        return email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserBean [firstName=");
        builder.append(firstName);
        builder.append(", lastName=");
        builder.append(lastName);
        builder.append(", email=");
        builder.append(email);
        builder.append(", employee=");
        builder.append(employee);
        builder.append("]");
        return builder.toString();
    }

}