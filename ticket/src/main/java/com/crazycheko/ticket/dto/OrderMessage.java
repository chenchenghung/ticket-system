package com.crazycheko.ticket.dto;


import com.crazycheko.ticket.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;
    private String eventId;
    private String userId;
    private Integer quantity;
    private Order.OrderStatus status;

}
