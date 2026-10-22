import { useEffect, useRef } from 'react';
import styles from '../HomePage.module.css';
import { formatMoney } from '../../../js/format';
import { TASK_STATUSES, TASK_PRIORITIES, taskPrice } from '../../../js/taskView';

export default function TaskDetails({ task, category, onClose }) {

  const dialogRef = useRef(null);
  const price = taskPrice(task);

  useEffect(() => {

    const dialog = dialogRef.current;
    const opener = document.activeElement;
    const previousOverflow = document.body.style.overflow;
    dialog.showModal();
    document.body.style.overflow = 'hidden';

    return () => {
      dialog.close();
      document.body.style.overflow = previousOverflow;
      if (opener instanceof HTMLElement && opener.isConnected) opener.focus();
    };

  }, []);

  return (
    <dialog ref={dialogRef} className={`${styles.editModal} ${styles.taskDetails}`} aria-labelledby="task-details-title" onCancel={(event) => { event.preventDefault(); onClose(); }}>

      <button autoFocus className={styles.logout} onClick={onClose}>Close</button>
      <h2 id="task-details-title">{task.title}</h2>
      <p className={styles.taskDescription}>{task.description || 'No description provided.'}</p>

      <dl className={styles.taskFacts}>

        <dt>Status</dt><dd>{TASK_STATUSES[task.status] ?? task.status}</dd>
        <dt>Priority</dt><dd>{TASK_PRIORITIES[task.priority] ?? 'Not set'}</dd>
        <dt>Category</dt><dd>{category.title}</dd>
        {price != null && <><dt>Price</dt><dd>{formatMoney(price)}</dd></>}
        {task.deadline && <><dt>Deadline</dt><dd>{new Date(task.deadline).toLocaleDateString('en-GB')}</dd></>}
        {task.estimatedHours != null && <><dt>Estimated hours</dt><dd>{task.estimatedHours}</dd></>}
        {task.link && <><dt>Link</dt><dd><a href={task.link} target="_blank" rel="noreferrer">Open link</a></dd></>}
        
      </dl>

    </dialog>
  );
}
