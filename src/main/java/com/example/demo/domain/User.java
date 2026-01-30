package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "nickname", unique = true))
    private Nickname nickname;

    @Column(nullable = false)
    private String email;

    @Column(length = 6, unique = true)
    private String invitationCode;

    @Column(length = 2048, nullable = false)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    private Integer level = 0;

    public User(String email, Nickname nickname, String invitationCode, String profile, Provider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.invitationCode = invitationCode;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void changeNickname(String nickname) {
        this.nickname = new Nickname(nickname);
    }

    public String getNickname() {
        return nickname.value();
    }
}
