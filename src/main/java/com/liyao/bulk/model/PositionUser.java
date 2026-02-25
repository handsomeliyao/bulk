package com.liyao.bulk.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PositionUser {
    private Long id;
    private Long postId;
    private Long operCode;
    private LocalDateTime createdAt;
}
