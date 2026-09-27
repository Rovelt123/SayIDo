package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false, updatable = false)
    private Wedding wedding;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private double categoryBudget;

    @Builder.Default
    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.ALL
    )
    private Set<Task> tasks = new HashSet<>();



    // ________________________________________________________

    public void addTask(Task task) {
        tasks.add(task);
        task.setCategory(this);
    }

    // ________________________________________________________

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setCategory(null);
    }
}
