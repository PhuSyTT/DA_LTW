package com.bookstore.repository;

import com.bookstore.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByBranchId(Long branchId);

    List<Driver> findByBranchIdAndStatus(Long branchId, String status);

    List<Driver> findByStatus(String status);

    @Query("SELECT d FROM Driver d WHERE (:branchId IS NULL OR d.branch.id = :branchId OR d.branch IS NULL) AND d.status = 'AVAILABLE' ORDER BY d.activeOrdersCount ASC, d.rating DESC")
    List<Driver> findAvailableDriversForBranch(@Param("branchId") Long branchId);
}

