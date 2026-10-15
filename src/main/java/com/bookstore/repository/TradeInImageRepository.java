package com.bookstore.repository;

import com.bookstore.entity.TradeInImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeInImageRepository extends JpaRepository<TradeInImage, Long> {

    List<TradeInImage> findByTradeInRequestId(Long tradeInRequestId);
}
