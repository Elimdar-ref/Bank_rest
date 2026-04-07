package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    //Найти ВСЕ карты пользователя
    List<Card> findByUser(User user);

    Page<Card> findByUser(User user, Pageable pageable);

    //Найти карту по зашифрованному номеру
    Optional<Card> findByCardNumberEncrypted(String encryptedNumber);

    //Проверить, существует ли карта с таким номером
    boolean existsByCardNumberEncrypted(String encryptedNumber);

    //Найти все карты с определённым статусом
    List<Card> findByStatus(Card.CardStatus status);

    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Card c WHERE c.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);
}