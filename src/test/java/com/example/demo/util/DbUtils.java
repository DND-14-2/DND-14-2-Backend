package com.example.demo.util;

import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;

public class DbUtils {
    public static User givenSavedUser(UserRepository userRepository) {
        User user = new User(
            "test@example.com",
            "https://profile.com/image.png",
            Provider.KAKAO,
            "kakao-test-1"
        );
        return userRepository.save(user);
    }
}
