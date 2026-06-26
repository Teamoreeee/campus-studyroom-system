package com.campus.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_recommendation")
public class AiRecommendation {

    @TableId(type = IdType.AUTO)
    private Long recommendationId;

    private Long userId;

    private Long roomId;

    private Long seatId;

    private BigDecimal score;

    private String reason;

    private String strategy;

    private Boolean isAccepted;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
