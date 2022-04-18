package com.careerdevs.gorestmysql.validation;

import com.careerdevs.gorestmysql.models.User;
import com.careerdevs.gorestmysql.repos.UserRepository;
import com.careerdevs.gorestmysql.utils.ApiErrorHandling;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class UserValidation {
    @Autowired
    private  UserRepository userRepository;

    public static ValidationError validateUser(User user,UserRepository userRepo, boolean isUpdate){
        ValidationError errors = new ValidationError();

        if(isUpdate){

            if(user.getId() == null){
                errors.addError("id","Id can not be left blank");
            }else {
                Optional<User> foundUser = userRepo.findById(user.getId());

                if(foundUser.isEmpty()){
                    errors.addError("id","No user found with the ID:" + user.getId());
                }else{
                    System.out.println(foundUser.get());
                }
            }

        }

        String userName = user.getName();
        String userEmail= user.getEmail();
        String userGender = user.getGender();
        String userStatus = user.getStatus();
        if(userName == null || userName.trim().equals("")){
            errors.addError("name","Name can not be left blank");
        }
        if(userEmail == null || userEmail.trim().equals("")){
            errors.addError("name","Email can not be left blank");
        }
        if(userGender == null || userGender.trim().equals("")){
            errors.addError("name","Gender can not be left blank");
        }else if(!(userGender.equals("male") || userGender.equals("female") ||userGender.equals("other"))){
            errors.addError("gender","Gender must be: male.female,or other");
        }
        if(userStatus == null || userStatus.trim().equals("")){
            errors.addError("name","Status can not be left blank");
        }else if(!(userStatus.equals("active") || userStatus.equals("inactive"))){
            errors.addError("status","Status must be: active or inactive");
        }

    return errors;
    }

}
