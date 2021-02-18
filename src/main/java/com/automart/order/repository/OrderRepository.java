package com.automart.order.repository;

import com.automart.order.domain.Order;
import com.automart.order.dto.TotalDailyMarginResponseDto;
import com.automart.order.dto.TotalDailySalesResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 주문 고유번호로 주문 조회하기(단건)
    public Optional<Order> findByNo(Integer no);

    /**
     * 일별 매출 (전체)
     *
     * od : order_no 별 전체 주문 가격
     * date : 주문이 존재하는 날짜
     * price(SUM(od.price)) : 해당 날짜의 총 판매액
     */
    @Query(value="SELECT DATE(o.order_date) AS date, SUM(od.price) AS price" +
                " FROM automart.order o" +
                    " LEFT OUTER JOIN (" +
                        " SELECT order_no, SUM(order_detail_price) AS price" +
                        " FROM automart.order_detail" +
                        " WHERE order_detail_status = 1" +
                        " GROUP BY order_no" +
                    " ) od" +
                    " ON o.order_no = od.order_no" +
                " WHERE o.order_state = 1" +
                " GROUP BY o.order_date" +
                " ORDER BY o.order_date;", nativeQuery = true)
    public List<TotalDailySalesResponseDto> findTotalDailySales();

    /**
     * 일별 매출 (카테고리)
     *
     * od : order_no 별 전체 주문 가격
     * date : 주문이 존재하는 날짜
     * price(SUM(od.price)) : 해당 날짜에 특정 카테고리 상품들의 총 판매액
     */
    @Query(value="SELECT DATE(o.order_date) AS date, SUM(od.price) AS price" +
                " FROM automart.order o" +
                    " INNER JOIN (" +
                        " SELECT d.order_no, SUM(d.order_detail_price) AS price" +
                        " FROM automart.order_detail d, automart.product p" +
                        " WHERE d.product_no = p.product_no" +
                            " AND d.order_detail_status = 1" +
                            " AND p.category_no = ?1" +
                        " GROUP BY order_no" +
                    " ) od" +
                " ON o.order_no = od.order_no" +
                " WHERE o.order_state = 1" +
                " GROUP BY o.order_date" +
                " ORDER BY o.order_date;", nativeQuery = true)
    public List<TotalDailySalesResponseDto> findTotalDailySalesByCategory(int categoryNo);
//
//    /**
//     * 일별 매출 (상품)
//     *
//     * od : order_no 별 전체 주문 가격
//     * date : 주문이 존재하는 날짜
//     * price(SUM(od.price)) : 해당 날짜에 특정 상품의 총 판매액
//     */
//    @Query(value="SELECT DATE(o.order_date) AS date, SUM(od.price) AS price" +
//            " FROM automart.order o" +
//            " INNER JOIN (" +
//            " SELECT d.order_no, SUM(d.order_detail_price) AS price" +
//            " FROM automart.order_detail d, automart.product p" +
//            " WHERE d.product_no = p.product_no" +
//            " AND d.order_detail_status = 1" +
//            " AND p.product_no = ?1" +
//            " GROUP BY order_no" +
//            " ) od" +
//            " ON o.order_no = od.order_no" +
//            " WHERE o.order_state = 1" +
//            " GROUP BY o.order_date" +
//            " ORDER BY o.order_date;", nativeQuery = true)
//    public List<TotalDailySalesResponseDto> findTotalDailySalesByProduct(int productNo);

    /**
     * 일별 순수익 (전체)
     *
     * od : order_no 별 순수익
     * date : 주문이 존재하는 날짜
     * margin(SUM(od.margin)) : 해당 날짜의 총 순수익
     */
    @Query(value="SELECT DATE(o.order_date) AS date, SUM(od.margin) AS margin" +
                " FROM automart.order o" +
                    " LEFT OUTER JOIN (" +
                        " SELECT order_no," +
                            " SUM(d.order_detail_price - p.product_cost * d.order_detail_count) AS margin" +
                        " FROM automart.order_detail d, automart.product p" +
                        " WHERE d.product_no = p.product_no" +
                            " AND d.order_detail_status = 1" +
                        " GROUP BY d.order_no" +
                    " ) od" +
                    " ON o.order_no = od.order_no" +
                " WHERE o.order_state = 1" +
                " GROUP BY o.order_date" +
                " ORDER BY o.order_date;", nativeQuery = true)
    public List<TotalDailyMarginResponseDto> findTotalDailyMargin();

    /**
     * 일별 순수익 (카테고리별)
     *
     * od : order_no 별 순수익
     * date : 주문이 존재하는 날짜
     * margin(SUM(od.margin)) : 해당 날짜에 특정 카테고리 상품들의 총 순수익
     */
    @Query(value="SELECT DATE(o.order_date) AS date, SUM(od.margin) AS margin" +
            " FROM automart.order o" +
                " INNER JOIN (" +
                    " SELECT order_no," +
                        " SUM(d.order_detail_price - p.product_cost * d.order_detail_count) AS margin" +
                    " FROM automart.order_detail d, automart.product p" +
                    " WHERE d.product_no = p.product_no" +
                        " AND d.order_detail_status = 1" +
                        " AND p.category_no = ?1" +
                    " GROUP BY d.order_no" +
                " ) od" +
                " ON o.order_no = od.order_no" +
            " WHERE o.order_state = 1" +
            " GROUP BY o.order_date" +
            " ORDER BY o.order_date;", nativeQuery = true)
    public List<TotalDailyMarginResponseDto> findTotalDailyMarginByCategory(int categoryNo);

    /*
    SELECT p.product_name AS name,
    FROM ( SELECT order_no
           FROM automart.order
           WHERE DATE_FORMAT(order_date, "%Y-%m-%d") = CURDATE()
               AND o.order_state = 1 ) o,
         automart.order_detail d, automart.product p
    WHERE o.order_no = d.order_no
        AND p.product_no = d.product_no
        AND d.order_detail_status = 1
        AND p.category_code = ?1
    GROUP BY p.product_no
    ORDER BY  desc limit 5
    */
}
