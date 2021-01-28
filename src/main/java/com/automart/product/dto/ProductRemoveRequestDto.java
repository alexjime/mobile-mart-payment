package com.automart.product.dto;

import com.automart.product.domain.Product;
import lombok.Builder;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Data
public class ProductRemoveRequestDto {

    private int productNo; // 제품 고유 번호

    @Builder
    public ProductRemoveRequestDto(int productNo) {
        this.productNo = productNo;
    }

}
