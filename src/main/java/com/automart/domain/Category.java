package com.automart.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_no")
    private int no; // 카테고리 고유번호

    @Column(name = "category_name", length = 45, unique = true)
    private String name; // 카테고리 이름

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    /**
     * 카테고리 생성
     */
    public static Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);

        return category;
    }
}
