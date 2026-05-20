package com.petal.repository;

import com.petal.entity.SavedDate;
import com.petal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedDateRepository extends JpaRepository<SavedDate, Long> {
    List<SavedDate> findByUserOrderByEventDateAscIdAsc(User user);
}
