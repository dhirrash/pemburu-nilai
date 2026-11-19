/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.rotibakarasjad.service;

import com.github.rotibakarasjad.entity.enums.Role;
import com.github.rotibakarasjad.entity.User;
import com.github.rotibakarasjad.util.Session;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author HP ELTEBOOKK
 */
public class AuthService {
    public User login(String username, String password) throws Exception {
        User user = null;
        
        if (username.equals("admin")) {
            user = new User();
            
            user.setId(1);
            user.setUsername("admin");
            user.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            user.setFullName("Admin Asjad");
            user.setRole(Role.ADMIN);
        } else if (username.equals("kasir1")) {
            user = new User();
            user.setId(2);
            user.setUsername("kasir1");
            user.setPasswordHash(BCrypt.hashpw("kasir123", BCrypt.gensalt()));
            user.setFullName("Kasir Roti Bakar");
            user.setRole(Role.KASIR);
        }

        if (user == null) {
            throw new Exception("Username tidak ditemukan.");
        }
        
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new Exception("Password salah.");
        }

        Session.login(user);
        return user;
    }
}
