package com.example.demo.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.application.user.UserService;
import com.example.demo.domain.Provider;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class UserServiceTest {

    @Autowired
    UserService sut;

    @Autowired
    UserRepository userRepository;

    @Test
    void 닉네임이_중복되면_안된다() {
        // given
        User user1 = new User("email1", "profile1", Provider.GOOGLE, "provider-id-1");
        User user2 = new User("email2", "profile2", Provider.GOOGLE, "provider-id-2");
        Long user1Id = userRepository.save(user1).getId();
        Long user2Id = userRepository.save(user2).getId();
        String nickname = "test";
        sut.registerNickname(user1Id, nickname);

        // when & then
        assertThatThrownBy(() -> sut.registerNickname(user2Id, nickname))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 닉네임입니다.");
    }
}
