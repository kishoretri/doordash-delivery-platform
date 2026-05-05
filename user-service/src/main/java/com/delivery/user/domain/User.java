package com.delivery.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name ="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name",nullable = false, length = 100)
    private String fullName;
    @Column(name = "email",nullable = false, unique = true, length = 50)
    private String email;
    @Column(name = "password",nullable = false, length = 255)
    private String password;
    @Column(name="phone", length = 20)
    private String phone;
    @Column(name="enabled" , nullable = false)
    private Boolean enabled;
    @Column(name="created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;


    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;


}
