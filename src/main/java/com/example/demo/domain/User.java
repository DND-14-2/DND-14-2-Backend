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
    @AttributeOverride(name = "value", column = @Column(name = "nickname"))
    private Nickname nickname;

    private String email;

    @Column(length = 2048)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    private Provider provider;

    private String providerId;

    private Integer level = 0;

    public User(String email, String profile, Provider provider, String providerId) {
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void changeNickname(String nickname) {
        this.nickname = new Nickname(nickname);
    }
}
