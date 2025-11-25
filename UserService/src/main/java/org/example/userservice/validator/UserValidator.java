package org.example.userservice.validator;

import org.example.userservice.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    public String validate(UserEntity user){
        StringBuilder errors = new StringBuilder();
        errors
                .append(validateEmail(user))
                .append(validatePhoneNumber(user));
        if(!errors.isEmpty()){
            return errors.toString();
        }
        return "";
    }


    private String validateEmail(UserEntity user){
        if(user.getEmail()==null || user.getEmail().isEmpty()){
            return "Email is required! ";
        }
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-zA-z]+\\.[a-zA-Z.]+");
        Matcher matcher = pattern.matcher(user.getEmail());
        if(!matcher.matches()){
            return "Invalid email!";
        }
        return "";
    }

    private String validatePhoneNumber(UserEntity user) {
        if (user.getTelephone() == null || user.getTelephone().isEmpty())
            return "Phone number is required\n";
        Pattern pattern = Pattern.compile("[0-9]{10}");
        Matcher matcher = pattern.matcher(user.getTelephone());
        if (!matcher.matches()) {
            return "Invalid phone number!";
        }
        return "";
    }
}
