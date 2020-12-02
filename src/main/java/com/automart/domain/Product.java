package com.automart.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id @GeneratedValue
    @Column(name = "product_no")
    private int no; // 제품 고유번호

    @OneToMany(fetch = FetchType.LAZY)
    private List<Cart> carts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_no")
    private Category category; // 카테고리 고유번호

    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column(name = "product_name")
    private String name; // 제품 이름

    @Column(name = "product_price")
    private int price; // 제품 판매가

    @Column(name = "product_cost")
    private int cost; // 제품 원가

    @Column(name = "product_stock")
    private int stock; // 제품 재고

    @Column(name = "product_code")
    private int code; // 제품 바코드 번호

    @Column(name = "product_img_url")
    private String imgUrl; // 제품 이미지 저장 주소

    @Column(name = "product_location")
    private String location; // 제품 진열 위치

}