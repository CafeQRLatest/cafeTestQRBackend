package com.restaurant.pos.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSearchCriteria {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private UUID categoryId;
    private String paymentMethod;
    private String searchTerm;
    private UUID branchId;
}
