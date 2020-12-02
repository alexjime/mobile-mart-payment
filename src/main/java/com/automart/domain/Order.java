package com.automart.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_no")
    private int no; // 주문 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User user; // 고객 고유번호

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Column(name = "order_date")
    private LocalDateTime orderDate; // 주문 날짜

    private OrderStatus status; // 주문 상태 [ORDER, CANCLE], 얘도 DB에서 빼는게 좋지 않을까?

    private int totalPrice; // 얘는 DB에서 빼는게 좋을듯

    /* To-do list
        주문생성
        주문취소
        전체가격조회 */
}