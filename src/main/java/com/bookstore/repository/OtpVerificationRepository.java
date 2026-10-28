package com.bookstore.repository;

import com.bookstore.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    // Tìm mã OTP mới nhất chưa sử dụng của email
    Optional<OtpVerification> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);

    // Tìm mã OTP mới nhất chưa sử dụng theo email và loại OTP (REGISTER, RESET_PASSWORD, UPDATE_PROFILE)
    Optional<OtpVerification> findTopByEmailAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(String email, String otpType);

    // Tìm bản ghi OTP mới nhất để kiểm tra Cooldown (chống spam < 60s)
    Optional<OtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    // Tìm bản ghi OTP mới nhất theo loại OTP để kiểm tra Cooldown
    Optional<OtpVerification> findTopByEmailAndOtpTypeOrderByCreatedAtDesc(String email, String otpType);

    // Đếm số lần yêu cầu OTP trong khoảng thời gian (Rate limiting: max 5 lần / 15 phút)
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime afterTime);

    // Đếm số lần yêu cầu OTP theo loại trong khoảng thời gian
    long countByEmailAndOtpTypeAndCreatedAtAfter(String email, String otpType, LocalDateTime afterTime);
}

