package com.example.demo.domain;

import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import jakarta.persistence.*;
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

    public LedgerEntry(UpsertLedgerCommand command, User user) {
        validateAmount(command.amount());
        this.amount = command.amount();

        this.type = command.type();
        this.category = command.category();
        this.description = normalizeDescription(command.description());
        this.occurredOn = command.occurredOn();
        this.paymentMethod = command.paymentMethod();

        this.memo = command.memo();
        this.user = user;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void update(long amount, LedgerType type, LedgerCategory category, String description, PaymentMethod paymentMethod, String memo) {
        validateAmount(amount);
        this.amount = amount;

        this.type = type;
        this.category = category;
        this.description = normalizeDescription(description);
        this.paymentMethod = paymentMethod;

        this.memo = memo;
    }

    private static void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액(amount)은 0보다 커야 합니다.");
        }
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("설명(description)은 필수입니다.");
        }
        String trimmed = description.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("설명(description)은 빈 문자열일 수 없습니다.");
        }
        if (trimmed.length() > 15) {
            throw new IllegalArgumentException("설명(description)은 1자 이상 15자 이내여야 합니다.");
        }
        return trimmed;
    }
}
