package com.example.demo.domain;

import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;            // 9,223,372,036,854,775,807원까지 가능

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerCategory category;

    @Column(nullable = false, length = 15)
    private String description;

    @Column(nullable = false)
    private LocalDate occurredOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public LedgerEntry(long amount, LedgerType type, LedgerCategory category, String description, LocalDate occurredOn, PaymentMethod paymentMethod, String memo, User user) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.occurredOn = occurredOn;
        this.paymentMethod = paymentMethod;
        this.memo = memo;
        this.user = user;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void update(long amount, LedgerType type, LedgerCategory category, String description, PaymentMethod paymentMethod, String memo) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.memo = memo;
    }
}
