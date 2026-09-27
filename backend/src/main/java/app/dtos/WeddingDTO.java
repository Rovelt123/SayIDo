package app.dtos;

import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WeddingDTO {

    private UUID id;
    private UUID ownerId;
    private String title;
    private String date;
    private String location;
    private float budget;
    private String description;
    private int taskCount;
    private long completedTaskCount;
    private double totalEstimatedHours;
    private double totalSpent;
    private double remainingBudget;
    private double budgetUsedPercent;
    private double budgetOverrun;

    @Builder.Default
    private Set<CategoryDTO> categories = new HashSet<>();
}
