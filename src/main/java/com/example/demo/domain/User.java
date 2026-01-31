package com.example.demo.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @AttributeOverride(name = "value", column = @Column(name = "nickname", unique = true, length = 5))
    private Nickname nickname;

    @Column(nullable = false)
    private String email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "invitation_code", unique = true, length = 6))
    private InvitationCode invitationCode;

    @Column(length = 2048, nullable = false)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    private Integer level = 0;

    public User(String email, Nickname nickname, InvitationCode invitationCode, String profile, Provider provider, String providerId) {
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
