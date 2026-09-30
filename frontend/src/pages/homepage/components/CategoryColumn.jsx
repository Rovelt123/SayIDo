import styles from "./CategoryColumn.module.css";
import DraggableTask from "./DraggableTask";
import { useDroppable } from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { useState, useEffect, useRef } from "react";

function CategoryColumn({
  category,
  onEdit,
  onDelete,
  onAddTask,
  onEditTask,
  onDeleteTask,
  onChangeStatus,
  dragHandle,
}) {
  const isUncategorized = category.title === "Uncategorized";

  const tasks = [...(category.tasks ?? [])].sort(
    (a, b) => a.position - b.position,
  );

  const totalHours = tasks.reduce(
    (sum, task) => sum + (task.estimatedHours ?? 0),
    0,
  );

  const [menuOpen, setMenuOpen] = useState(false);

  const totalTasksPrice = tasks.reduce(
    (sum, task) => sum + (task.price ?? 0),
    0,
  );

  const categoryBudget = category.categoryBudget ?? 0;

  const aboveBudget = totalTasksPrice > categoryBudget;

  const { setNodeRef, isOver } = useDroppable({ id: category.id });

  const menuRef = useRef(null);

  useEffect(() => {
  function handleClickOutside(event) {
    if (
      menuRef.current &&
      !menuRef.current.contains(event.target)
    ) {
      setMenuOpen(false);
    }
  }

  document.addEventListener("mousedown", handleClickOutside);

  return () => {
    document.removeEventListener("mousedown", handleClickOutside);
  };
}, []);

  return (
    <div
      className={`${styles.container} ${isOver ? styles.dragOver : ""}`}
      ref={setNodeRef}
    >
      
      <div className={styles.header}>
        <div className={styles.actionsContainer}>
          {dragHandle}
        <h3 className={styles.smallTitle}>{category.title}</h3>
        </div>
        

        <div className={styles.actionsContainer}>

          {tasks.length > 0 && (
              <p className={styles.taskNumber}>
                {tasks.length} {tasks.length === 1 ? "task" : "tasks"}
               
              </p>
          )}
          {totalHours > 0 && (
              <p className={styles.taskNumber}>
                {totalHours}h
              </p>
          )}

          

          {!isUncategorized && (
            <div className={styles.menuWrapper} ref={menuRef}>
              <button className={styles.menuButton} onClick={() => setMenuOpen(!menuOpen)} aria-label={`Options for ${category.title}`}>
              ⋯
              </button>
              {menuOpen && (
                <div className={styles.categoryMenu}>
                  <button onClick={() => {onEdit(category), setMenuOpen(false)}}>
                    <i className="fa-solid fa-pen"></i>
                    Edit category
                  </button>
                  <button className={styles.deleteMenuItem} onClick={() =>{onDelete(category), setMenuOpen(false)}}>
                    <i className="fa-solid fa-trash"></i>
                    Delete category
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
        
      </div>

    {aboveBudget && (

        <span className={styles.aboveBudget}>
          <i className="fa-solid fa-triangle-exclamation"/>{" "}
          over budget
        </span>

      )}

      {tasks.length === 0 && <p className={styles.empty}>No tasks yet</p>}

      <SortableContext
        items={tasks.map((task) => task.id)}
        strategy={verticalListSortingStrategy}
      >
        <ul className={styles.taskList}>
          {tasks.map((task) => (
            <DraggableTask
              key={task.id}
              task={task}
              onEditTask={onEditTask}
              onDeleteTask={onDeleteTask}
              onChangeStatus={onChangeStatus}
            />
          ))}
        </ul>
      </SortableContext>
      <button className={styles.addTaskButton} onClick={() => onAddTask(category)} aria-label={`Add task to ${category.title}`}>
            + Add task
      </button>
    </div>
  );
}

export default CategoryColumn;
