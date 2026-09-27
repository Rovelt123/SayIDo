import { formatMoney, formatPercent } from "../../js/format";
import styles from "./BudgetOverviewPage.module.css";

// ________________________________________________________

const colors = [
  "#96704d", 
  "#509687", 
  "#954d96", 
  "#81964d", 
  "#4d7296", 
  "#b56b78", 
  "#c39445", 
  "#687c72", 
];

// ________________________________________________________

export default function BudgetChart({ wedding }) {
  const overBudget = wedding.budgetOverrun > 0;
  const scale = Math.max(wedding.budget, wedding.totalSpent, 0);
  const categories = [];
  let start = 0;

// ________________________________________________________

  for (const [index, category] of wedding.categories.entries()) {
    const spent = category.totalTasksPrice;
    const size = scale > 0 ? Math.max(0, spent) / scale * 100 : 0;
    const budgetPercent = wedding.budget > 0 ? spent / wedding.budget * 100 : null;

    categories.push({
      ...category,
      color: colors[index % colors.length],
      start,
      size,
      budgetPercent,
    });
    start += size;
  }

// ________________________________________________________
// SVG-Diagram udviklet med hjælp fra AI... 
  return (
    <div>
      <div className={styles.chart}>
        
        <svg viewBox="0 0 320 180" role="img">
          <path d="M 24 156 A 136 136 0 0 1 296 156" className={styles.chartTrack} />
          {categories.filter((category) => category.size > 0).map((category) => (
            <path key={category.id} d="M 24 156 A 136 136 0 0 1 296 156" pathLength="100"
              stroke={category.color} strokeDasharray={`${category.size} 100`}
              strokeDashoffset={-category.start}>
              <title>{category.title}: {formatMoney(category.totalTasksPrice)}</title>
            </path>
          ))}
        </svg>

        <div className={styles.chartLabel}>
          <span>Total budget</span>
          <strong>{formatMoney(wedding.budget)}</strong>
        </div>

      </div>

      <p className={styles.shareLabel}>Category spending:</p>

      <ul className={styles.chartLegend}>
        
        {categories.map((category) => (
          <li key={category.id}>
            <span className={styles.legendName}>
              <i style={{ background: category.color }} />{category.title}

              {category.budgetOverrun > 0 && (
                <span className={styles.legendWarning}>
                  <i className="fa-solid fa-triangle-exclamation"/>{" "}
                  {formatMoney(category.budgetOverrun)} over budget
                </span>
              )}

            </span>

            <span>{formatMoney(category.totalTasksPrice)}</span>
            <strong>{category.budgetPercent === null ? "—" : formatPercent(category.budgetPercent)}</strong>

          </li>
        ))}

        <li>
          <span className={styles.legendName}><i className={styles.remainingDot} />Remaining</span>
          <span>{formatMoney(Math.max(0, wedding.remainingBudget))}</span>
          <strong>{wedding.budget > 0
            ? formatPercent(Math.max(0, 100 - wedding.budgetUsedPercent)) : "—"}</strong>
        </li>

      </ul>
    </div>
  );
}
