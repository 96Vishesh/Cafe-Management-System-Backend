package com.inn.cafe.rest;


import com.inn.cafe.POJO.User;
import com.inn.cafe.wrapper.UserWrapper;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping(path="/user")

public interface UserRest {

    @PostMapping(path = "/signup")
    ResponseEntity<String> signUp(@RequestBody Map<String,String> requestMap);


    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody(required = true) Map<String,String> requestMap);

    @GetMapping(path="/get")
    public ResponseEntity<List<UserWrapper>> getAllUsers();

    @PostMapping(path="/update")
    public ResponseEntity<String> update(@RequestBody(required = true)Map<String,String>RequestMap);

    @GetMapping(path="/checkToken")
    ResponseEntity<String> checkToken();

    @PostMapping(path="/changePassword")
    ResponseEntity<String> changePassword(@RequestBody Map<String,String>RequestMap);

    @PostMapping(path="/forgotPassword")
    ResponseEntity<String> forgotPassword(@RequestBody Map<String,String>RequestMap);

}
