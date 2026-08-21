package com.dynamicstay.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "guest_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestReview {

    @Id
    private String id;

    private Long bookingId;

    private Long roomId;

    private String guestName;

    private Integer rating;

    private String comment;

    private LocalDate stayDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private List<String> tags;
}
