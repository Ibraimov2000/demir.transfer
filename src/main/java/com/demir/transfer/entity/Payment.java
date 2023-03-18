package com.demir.transfer.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Data
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    private User user;
}
