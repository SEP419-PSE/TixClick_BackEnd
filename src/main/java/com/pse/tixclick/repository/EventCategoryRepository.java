package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory,Integer> {
    Optional<EventCategory> findEventCategoriesByCategoryName(String categoryName);
}
