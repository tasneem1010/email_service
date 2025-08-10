package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data // Includes @ToString, @EqualsAndHashCode, @Getter, @Setter and @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String email;
    private String password;
    // Contains timezone info (Z)
    @Column(updatable = false, insertable = false, nullable = false)
    private Instant createdDate;
    @Column(updatable = false, insertable = false, nullable = false)
    private Instant updatedDate;
    private boolean deleted;

}

