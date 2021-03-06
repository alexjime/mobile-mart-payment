package com.automart.category.repository;

import com.automart.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // 카테고리 고유 코드로 카테고리 조회하기(단건)
    public Optional<Category> findByNo(int no);

    // 이름으로 카테고리 조회하기
    public Optional<Category> findByName(String name);

    // 고유 코드로 카테고리 삭제하기
    public void deleteByNo(int no);
}
