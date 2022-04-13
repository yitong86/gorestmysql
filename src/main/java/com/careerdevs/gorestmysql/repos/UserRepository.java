package com.careerdevs.gorestmysql.repos;




import com.careerdevs.gorestmysql.models.User;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    //primary key id
}