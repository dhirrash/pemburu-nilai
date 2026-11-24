/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.rotibakarasjad.service;

import com.github.rotibakarasjad.entity.User;
import com.github.rotibakarasjad.exception.BusinessException;
import com.github.rotibakarasjad.repository.UserRepository;
import com.github.rotibakarasjad.util.Session;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author HP ELTEBOOKK
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    public User login(String username, String password) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Username tidak ditemukan."));

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException("Password salah.");
        }

        Session.login(user);
        return user;
    }
}
