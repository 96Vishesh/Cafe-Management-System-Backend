package com.inn.cafe.dao;


import com.inn.cafe.POJO.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@EnableJpaRepositories
@Repository
public interface CategoryDao extends JpaRepository<Category, Integer> {

    List<Category> getAllCategory();
}
