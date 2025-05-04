package com.example.tfgdanielmario.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User authenticate(String username, String password);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);
}
