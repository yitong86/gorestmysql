package com.careerdevs.gorestmysql.controllers;


import com.careerdevs.gorestmysql.repos.UserRepository;
import com.careerdevs.gorestmysql.models.User;
import com.careerdevs.gorestmysql.utils.ApiErrorHandling;
import com.careerdevs.gorestmysql.validation.UserValidation;
import com.careerdevs.gorestmysql.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
//    Required Routes for GoRestSQL MVP:
//    GET route that returns one user by ID from the SQL database
//    GET route that returns all users stored in the SQL database
//    DELETE route that deletes one user by ID from SQL database (returns the deleted SQL user data)
//    DELETE route that deletes all users from SQL database (returns how many users were deleted)
//    POST route that queries one user by ID from GoREST and saves their data to your local database (returns the SQL user data)
//    POST route that uploads all users from the GoREST API into the SQL database (returns how many users were uploaded)
//    POST route that create a user on JUST the SQL database (returns the newly created SQL user data)
//    PUT route that updates a user on JUST the SQL database (returns the updated SQL user data)

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") String id) {
        try {

            if (ApiErrorHandling.isStrNaN(id))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid ID");


            int uID = Integer.parseInt(id);

            Optional<User> foundUser = userRepository.findById(uID);

            if (foundUser.isEmpty())
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User Not Found With ID:" + id);

            return new ResponseEntity<>(foundUser, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") String id) {
        try {

            if (ApiErrorHandling.isStrNaN(id)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, id + " is not a valid ID");
            }


            int uID = Integer.parseInt(id);

            Optional<User> foundUser = userRepository.findById(uID);

            if (foundUser.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User Not Found with ID:" + id);
            }
            userRepository.deleteById(uID);

            return new ResponseEntity<>(foundUser, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }


    @DeleteMapping("/deleteall")
    public ResponseEntity<?> deleteAllUsers() {
        try {
            long totalUsers = userRepository.count();
            userRepository.deleteAll();

            return new ResponseEntity<>("Users Deleted:" + totalUsers, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }


    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadUserById(
            @PathVariable("id") String userId,
            RestTemplate restTemplate
    ) {

        try {

            if (ApiErrorHandling.isStrNaN(userId)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, userId + " is not a valid ID");
            }


            int uID = Integer.parseInt(userId);

            //check the range
            String url = "https://gorest.co.in/public/v2/users/" + uID;

            User foundUser = restTemplate.getForObject(url, User.class);//404 STOP CODE HERE


            //System.out.println("Found USER");
            System.out.println(foundUser);

            //assert foundUser != null
            if (foundUser == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User data was null");
            }
            User savedUser = userRepository.save(foundUser);

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }

    @PostMapping("/uploadall")
    public ResponseEntity<?> uploadAll(RestTemplate restTemplate) {
        try {

            String url = "https://gorest.co.in/public/v2/users/";

            ResponseEntity<User[]> response = restTemplate.getForEntity(url, User[].class);

            User[] firstPageUsers = response.getBody();

            assert firstPageUsers != null;

            if (firstPageUsers == null) {
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to Get first page " + "users from GoRest");
            }
            ArrayList<User> allUsers = new ArrayList<>(Arrays.asList(firstPageUsers));

            HttpHeaders responseHeaders = response.getHeaders();

            String totalPages = Objects.requireNonNull(responseHeaders.get("X-Pagination-pages")).get(0);

            int totalPgNum = Integer.parseInt(totalPages);
//page 2
            for (int i = 2; i <= totalPgNum; i++) {
                String pageUrl = url + "?page=" + i;
                User[] pageUsers = restTemplate.getForObject(pageUrl, User[].class);

                if (pageUsers == null) {
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to Get page " + i + "of users from GoRest");
                }
                allUsers.addAll(Arrays.asList(firstPageUsers));
            }
//           upload all users to SQL
            userRepository.saveAll(allUsers);
            return new ResponseEntity<>("Users Created:" + allUsers.size(), HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }


    @PostMapping("/")
    public ResponseEntity<?> createNewUser(@RequestBody User newUser) {
        try {

            ValidationError newUserErrors = UserValidation.validateUser(newUser, userRepository, false);

            if (newUserErrors.hasError()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, newUserErrors.toString());
            }

            User savedUser = userRepository.save(newUser);

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUser() {
        try {
            Iterable<User> allUsers = userRepository.findAll();
            return new ResponseEntity<>(allUsers, HttpStatus.OK);

        } catch (Exception e) {
            return ApiErrorHandling.genericApiError(e);

        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updateUser(@RequestBody User updateUser) {
        try {
            ValidationError newUserErrors = UserValidation.validateUser(updateUser, userRepository, true);
            if (newUserErrors.hasError()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, newUserErrors.toString());
            }
            User saveUser = userRepository.save(updateUser);
            return new ResponseEntity<>(saveUser, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            //System.out.println("HTTP");
            return ApiErrorHandling.customApiError(e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            //System.out.println("GENERIC");
            return ApiErrorHandling.genericApiError(e);

        }
    }
}







