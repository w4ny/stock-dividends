package com.example.stock.persist.entity;

import com.example.stock.model.Dividend;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @Entity(name = "DIVIDEND")
/*
    중복 데이터 저장 방지 제약
    단일 컬럼 뿐만 아니라 복합 컬럼도 지정 가능
*/
    @Table(uniqueConstraints =
            {
                    @UniqueConstraint(columnNames = {"companyId", "date"})
            }
    )
    public class DividendEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Long companyId;

        private LocalDateTime date;

        private String dividend;

        public DividendEntity(Long companyId, Dividend dividend) {
            this.companyId = companyId;
            this.date = dividend.getDate();
            this.dividend = dividend.getDivide();
        }
    }
