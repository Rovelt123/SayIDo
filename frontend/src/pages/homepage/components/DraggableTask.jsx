import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import styles from './CategoryColumn.module.css'
import { TASK_STATUSES, TASK_PRIORITIES, taskPrice } from '../../../js/taskView'
import { formatMoney } from '../../../js/format'

function DraggableTask({ task, onEditTask, onDeleteTask, onChangeStatus, onViewTask }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
  } = useSortable({  
    id: task.id, 
  })
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <li ref={setNodeRef} style={style} className={styles.task} onClick={(event) => {

      if (!event.target.closest('button, a, input, select, textarea, label')) {

        event.currentTarget.querySelector('[data-task-details]').focus()
        onViewTask(task)

      }

    }}>
      <span className={styles.taskHeader}>

        <button ref={setActivatorNodeRef} className={styles.moveHandle} aria-label={`Move ${task.title}`} {...listeners} {...attributes}>
          <i className="fa-solid fa-grip-vertical" aria-hidden="true"></i>
        </button>

        <button data-task-details className={styles.taskTitle} onClick={() => onViewTask(task)} aria-label={`View details for ${task.title}`}>
          {task.title}
        </button>

        <select className={`${styles.statusSelect} ${styles[task.status]}`} aria-label={`Status for ${task.title}`} name="status" value={task.status} onPointerDown={(e) => e.stopPropagation()} onChange={(e) => onChangeStatus(task, e.target.value)}>
          {Object.entries(TASK_STATUSES).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>

        <span className={styles.actionsContainer}>
          <button
            className={styles.iconButton}
            onPointerDown={(e) => e.stopPropagation()}
            onClick={() => onEditTask(task)}
            aria-label={`Edit ${task.title}`}
          >
            <i className="fa-solid fa-pen"></i>
          </button>

          <button
            className={styles.iconButton}
            onPointerDown={(e) => e.stopPropagation()}
            onClick={() => onDeleteTask(task)}
            aria-label={`Delete ${task.title}`}
          >
            <i className="fa-solid fa-trash"></i>
          </button>
        </span>
      </span>

      <span className={styles.taskMeta}>
        <span className={styles.metaRow}>
          <span className={styles.metaLabel}>Priority:</span>
          <span className={styles.metaValue}>{TASK_PRIORITIES[task.priority] ?? 'Not set'}</span>
        </span>

        {taskPrice(task) != null && (
          <span className={styles.metaRow}>
            <span className={styles.metaLabel}>Price:</span>
            <span className={styles.metaValue}>{formatMoney(taskPrice(task))}</span>
          </span>
        )}

        {task.link && (
          <span className={styles.metaRow}>
            <span className={styles.metaLabel}>Link:</span>
            <a
              className={styles.taskLink}
              href={task.link}
              target="_blank"
              rel="noreferrer"
              onPointerDown={(e) => e.stopPropagation()}
            >
              Open
            </a>
          </span>
        )}
      </span>
    </li>
  )
}
export default DraggableTask
