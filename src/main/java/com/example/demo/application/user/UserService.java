package com.example.demo.application.user;

import com.example.demo.application.dto.UserInfo;
import com.example.demo.domain.Friend;
import com.example.demo.domain.FriendRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final InvitationCodeGenerator invitationCodeGenerator;

    @Transactional(readOnly = true)
    public UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return new UserInfo(user.getId(), user.getNickname(), user.getLevel(), user.getProfile());
    }

    @Transactional
    public String registerNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        validateIsDuplicateNickname(nickname);
        user.registerNickname(nickname);

        String invitationCode = invitationCodeGenerator.generate(user);
        user.registerInvitationCode(invitationCode);

        return invitationCode;
    }

    private void validateIsDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
    }

    @Transactional
    public void addFriend(Long userId, String invitationCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        User followingUser = userRepository.findByInvitationCode(invitationCode)
            .orElseThrow(() -> new IllegalArgumentException("팔로우하려는 대상이 존재하지 않습니다."));

        if (friendRepository.existsFriend(user.getId(), followingUser.getId())) {
            return;
        }

        Friend friend = new Friend(user, followingUser);
        friendRepository.save(friend);
    }
}
