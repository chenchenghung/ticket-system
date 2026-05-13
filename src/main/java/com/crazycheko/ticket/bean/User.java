package com.crazycheko.ticket.bean;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "age", nullable = false, unique = true)
    private Integer age;

}

