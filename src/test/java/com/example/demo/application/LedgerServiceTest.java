package com.example.demo.application;

import com.example.demo.application.dto.DailyLedgerDetail;
import com.example.demo.application.dto.DailySummary;
import com.example.demo.application.dto.LedgerResult;
import com.example.demo.application.dto.UpsertLedgerCommand;
import com.example.demo.domain.LedgerEntry;
import com.example.demo.domain.LedgerEntryRepository;
import com.example.demo.domain.User;
import com.example.demo.domain.UserRepository;
import com.example.demo.domain.enums.LedgerCategory;
import com.example.demo.domain.enums.LedgerType;
import com.example.demo.domain.enums.PaymentMethod;
import com.example.demo.util.AbstractIntegrationTest;
import com.example.demo.util.DbUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class LedgerServiceTest extends AbstractIntegrationTest {
    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    void 가계부_항목을_생성할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            savedUser.getId(),
            12000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "점심",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "메모"
        );

        // when
        Long ledgerEntryId = ledgerService.createLedgerEntry(command);

        // then
        assertThat(ledgerEntryId).isNotNull();

        LedgerEntry saved = ledgerEntryRepository.findById(ledgerEntryId)
            .orElseThrow(() -> new AssertionError("저장된 가계부 항목을 찾을 수 없습니다. id=" + ledgerEntryId));

        assertThat(saved.getId()).isEqualTo(ledgerEntryId);
        assertThat(saved.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(saved.getAmount()).isEqualTo(12000L);
        assertThat(saved.getType()).isEqualTo(LedgerType.EXPENSE);
        assertThat(saved.getCategory()).isEqualTo(LedgerCategory.FOOD);
        assertThat(saved.getDescription()).isEqualTo("점심");
        assertThat(saved.getOccurredOn()).isEqualTo(LocalDate.of(2026, 1, 24));
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(saved.getMemo()).isEqualTo("메모");
    }

    @Test
    void 존재하지_않는_사용자면_생성_시_예외를_던진다() {
        // given
        long nonExistentUserId = 999999L;
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            nonExistentUserId,
            1000L,
            LedgerType.EXPENSE,
            LedgerCategory.OTHER,
            "테스트",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            null
        );

        // when & then
        assertThatThrownBy(() -> ledgerService.createLedgerEntry(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    void 사용자와_가계부ID로_가계부_항목을_조회할_수_있다() {
        // given
        User user = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            user.getId(),
            5000L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "버스",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.DEBIT_CARD,
            null
        );
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(command, user));

        // when
        LedgerResult result = ledgerService.getLedgerEntry(user.getId(), entry.getId());

        // then
        assertThat(result.ledgerId()).isEqualTo(entry.getId());
        assertThat(result.amount()).isEqualTo(5000L);
        assertThat(result.type()).isEqualTo(LedgerType.EXPENSE);
        assertThat(result.category()).isEqualTo(LedgerCategory.TRANSPORT);
        assertThat(result.description()).isEqualTo("버스");
        assertThat(result.occurredOn()).isEqualTo(LocalDate.of(2026, 1, 24));
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
        assertThat(result.memo()).isNull();
    }

    @Test
    void 다른_사용자의_가계부를_조회하면_예외를_던진다() {
        // given
        User savedUser1 = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            savedUser1.getId(),
            5000L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "버스",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.DEBIT_CARD,
            null
        );
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(command, savedUser1));

        User savedUser2 = DbUtils.givenSavedUser(userRepository);

        // when & then
        assertThatThrownBy(() -> ledgerService.getLedgerEntry(savedUser2.getId(), entry.getId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당되는 가계부 항목이 존재하지 않습니다.");
    }

    @Test
    void 메모를_수정할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            savedUser.getId(),
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            null
        );
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(command, savedUser));

        // when
        ledgerService.updateLedgerMemo(savedUser.getId(), entry.getId(), "아메리카노");

        // then
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getMemo()).isEqualTo("아메리카노");
    }

    @Test
    void 가계부_항목을_수정할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand oldCommand = new UpsertLedgerCommand(
            savedUser.getId(),
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            "old"
        );
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(oldCommand, savedUser));

        // when
        UpsertLedgerCommand newCommand = new UpsertLedgerCommand(
            savedUser.getId(),
            15000L,
            LedgerType.EXPENSE,
            LedgerCategory.SHOPPING,
            "옷",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            "new"
        );
        LedgerResult result = ledgerService.updateLedgerEntry(entry.getId(), newCommand);

        // then
        assertThat(result.amount()).isEqualTo(15000L);
        LedgerEntry updated = ledgerEntryRepository.findById(entry.getId()).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo("옷");
        assertThat(updated.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(updated.getMemo()).isEqualTo("new");
    }

    @Test
    void 가계부_항목을_삭제할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand command = new UpsertLedgerCommand(
            savedUser.getId(),
            7000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "커피",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CREDIT_CARD,
            null
        );
        LedgerEntry entry = ledgerEntryRepository.save(new LedgerEntry(command, savedUser));

        // when
        ledgerService.deleteLedgerEntry(savedUser.getId(), entry.getId());

        // then
        assertThat(ledgerEntryRepository.findById(entry.getId())).isEmpty();
    }

    @Test
    void 날짜_범위로_일별_요약을_조회할_수_있고_데이터가_없는_날짜는_0으로_채운다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        LocalDate start = LocalDate.of(2026, 1, 24);
        LocalDate end = LocalDate.of(2026, 1, 26);

        UpsertLedgerCommand command1 = new UpsertLedgerCommand(
            savedUser.getId(),
            1000L,
            LedgerType.INCOME,
            LedgerCategory.SAVINGS_FINANCE,
            "용돈",
            start,
            PaymentMethod.BANK_TRANSFER,
            null
        );
        ledgerEntryRepository.save(new LedgerEntry(command1, savedUser));

        UpsertLedgerCommand command2 = new UpsertLedgerCommand(
            savedUser.getId(),
            300L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "간식",
            start,
            PaymentMethod.CASH,
            null
        );
        ledgerEntryRepository.save(new LedgerEntry(command2, savedUser));

        UpsertLedgerCommand command3 = new UpsertLedgerCommand(
            savedUser.getId(),
            200L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "지하철",
            end,
            PaymentMethod.DEBIT_CARD,
            null
        );
        ledgerEntryRepository.save(new LedgerEntry(command3, savedUser));


        // when
        List<DailySummary> result = ledgerService.getSummary(savedUser.getId(), start, end);

        // then
        assertThat(result).hasSize(3);

        DailySummary d1 = result.get(0);
        assertThat(d1.date()).isEqualTo(start);
        assertThat(d1.incomeTotal()).isEqualTo(1000L);
        assertThat(d1.expenseTotal()).isEqualTo(300L);

        DailySummary d2 = result.get(1);
        assertThat(d2.date()).isEqualTo(start.plusDays(1));
        assertThat(d2.incomeTotal()).isEqualTo(0L);
        assertThat(d2.expenseTotal()).isEqualTo(0L);

        DailySummary d3 = result.get(2);
        assertThat(d3.date()).isEqualTo(end);
        assertThat(d3.incomeTotal()).isEqualTo(0L);
        assertThat(d3.expenseTotal()).isEqualTo(200L);
    }

    @Test
    void 특정_날짜의_가계부_항목과_수입지출_합계를_조회할_수_있다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        LocalDate date = LocalDate.of(2026, 1, 24);

        UpsertLedgerCommand command1 = new UpsertLedgerCommand(
            savedUser.getId(),
            2000L,
            LedgerType.INCOME,
            LedgerCategory.SAVINGS_FINANCE,
            "입금",
            date,
            PaymentMethod.BANK_TRANSFER,
            null
        );
        ledgerEntryRepository.save(new LedgerEntry(command1, savedUser));

        UpsertLedgerCommand command2 = new UpsertLedgerCommand(
            savedUser.getId(),
            500L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "점심",
            date,
            PaymentMethod.CREDIT_CARD,
            null
        );
        ledgerEntryRepository.save(new LedgerEntry(command2, savedUser));

        UpsertLedgerCommand command3 = new UpsertLedgerCommand(
            savedUser.getId(),
            700L,
            LedgerType.EXPENSE,
            LedgerCategory.TRANSPORT,
            "버스",
            date,
            PaymentMethod.DEBIT_CARD,
            null
        );
        ledgerEntryRepository.save(new LedgerEntry(command3, savedUser));


        // when
        DailyLedgerDetail detail = ledgerService.getLedgerEntriesByDate(savedUser.getId(), date);

        // then
        assertThat(detail.date()).isEqualTo(date);
        assertThat(detail.incomeTotal()).isEqualTo(2000L);
        assertThat(detail.expenseTotal()).isEqualTo(1200L);
        assertThat(detail.results()).hasSize(3);
    }


    @Test
    void LedgerEntry_생성시_amount는_0보다_커야한다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand badAmount = new UpsertLedgerCommand(
            savedUser.getId(),
            0L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "점심",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            null
        );

        // when & then
        assertThatThrownBy(() -> new LedgerEntry(badAmount, savedUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("금액(amount)은 0보다 커야 합니다.");
    }

    @Test
    void LedgerEntry_생성시_description은_1자이상_15자이하여야한다() {
        // given
        User savedUser = DbUtils.givenSavedUser(userRepository);
        UpsertLedgerCommand blankDesc = new UpsertLedgerCommand(
            savedUser.getId(),
            1000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "   ",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            null
        );

        UpsertLedgerCommand longDesc = new UpsertLedgerCommand(
            savedUser.getId(),
            1000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "1234567890123456",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            null
        );

        UpsertLedgerCommand ok = new UpsertLedgerCommand(
            savedUser.getId(),
            1000L,
            LedgerType.EXPENSE,
            LedgerCategory.FOOD,
            "  점심  ",
            LocalDate.of(2026, 1, 24),
            PaymentMethod.CASH,
            null
        );

        // when & then
        assertThatThrownBy(() -> new LedgerEntry(blankDesc, savedUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("설명(description)은 빈 문자열일 수 없습니다.");

        assertThatThrownBy(() -> new LedgerEntry(longDesc, savedUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("설명(description)은 1자 이상 15자 이내여야 합니다.");

        LedgerEntry entry = new LedgerEntry(ok, savedUser);
        assertThat(entry.getDescription()).isEqualTo("점심");
    }
}