package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUsersDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;


    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {

        log.info("Inside signup {}", requestMap);

        try {
            if (validateSignUp(requestMap)) {

                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {

                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);

                } else {
                    return CafeUtils.getResponseEntity("Email already exists", HttpStatus.BAD_REQUEST);
                }

            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param requestMap
     * @return
     */


    private boolean validateSignUp(Map<String, String> requestMap) {
        return requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password");
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );
            if (auth.isAuthenticated()) {
                if (customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                    return new ResponseEntity<String>("{\"token\":\"" +
                            jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                                    customerUsersDetailsService.getUserDetail().getRole()) + "\"}", HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for Admin Approval." + "\"}", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Wrong Credentials" + "\"}", HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param requestMap
     * @return
     */
    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (optional.isPresent()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User Status Successfully Updated", HttpStatus.OK);
                } else {
                    CafeUtils.getResponseEntity("User id does'nt exist", HttpStatus.OK);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());


        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account approved ", "USER:- " + user + "\n is approved by \n ADMIN:-" + jwtFilter.getCurrentUser(), allAdmin);

        } else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled ", "USER:- " + user + "\n is disabled by \n ADMIN:-" + jwtFilter.getCurrentUser(), allAdmin);


        }
    }

    @Override
    public ResponseEntity<String> checkToken() {

        return CafeUtils.getResponseEntity("true", HttpStatus.OK);



    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try{
            User userObj=userDao.findByEmail(jwtFilter.getCurrentUser());
            if(!userObj.equals(null)){

                if(userObj.getPassword().equals(requestMap.get("oldPassword"))){
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return CafeUtils.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
                }return CafeUtils.getResponseEntity("Incorrect old password",HttpStatus.BAD_REQUEST);
            }return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception ex){
            ex.printStackTrace();
        }return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try{
            User user=userDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && user.getEmail() != null && !user.getEmail().isEmpty()){
                emailUtils.forgotMail(user.getEmail(),"Credentials by Cafe Management System",user.getPassword());
                return CafeUtils.getResponseEntity("Check your mail for Credentials",HttpStatus.OK);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}