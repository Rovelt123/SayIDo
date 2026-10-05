export const TASK_STATUSES = { TODO: 'To-do', IN_PROGRESS: 'In progress', DONE: 'Done' };
export const TASK_PRIORITIES = { LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High' };

export const isTaskDone = (task) => task.status === 'DONE';

export function taskPrice(task) {

  if (task.price == null || String(task.price).trim() === '') return null;

  const price = Number(task.price);
  return Number.isFinite(price) ? price : null;

}

export function visibleTasks(tasks, statuses, sort) {

  const statusRank = { TODO: 0, IN_PROGRESS: 1, DONE: 2 };
  const priorityRank = { LOW: 0, MEDIUM: 1, HIGH: 2 };

  const value = (task) => {

    if (sort === 'status') return statusRank[task.status] ?? null;

    if (sort.startsWith('price')) return taskPrice(task);

    if (sort.startsWith('priority')) return priorityRank[task.priority] ?? null;

    return task.position ?? 0;

  };

  return tasks.filter((task) => statuses.includes(task.status)).sort((a, b) => {

    const left = value(a);
    const right = value(b);

    if (left == null && right != null) return 1;

    if (right == null && left != null) return -1;

    const difference = (left - right) * (sort.endsWith('-desc') ? -1 : 1);

    return difference || (a.position ?? 0) - (b.position ?? 0);
    
  });
}
