import { useState } from "react";
import { formatMoney, formatPercent } from "../../js/format";
import styles from "./BudgetOverviewPage.module.css";

// ________________________________________________________

export default function CategoryBudgetCard({ category, onSave, disabled }) {

  const [budget, setBudget] = useState(String(category.categoryBudget));
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const overBudget = category.budgetOverrun > 0;

// ________________________________________________________

  async function handleSave(event) {
    event.preventDefault();
    if (budget.trim() === "" || !Number.isFinite(Number(budget)) || Number(budget) < 0) {
      setError("Enter a budget of 0 or more.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await onSave(category.id, budget);
    } catch (error) {
      setError(error.message);
    } finally {
      setSaving(false);
    }
  }

// ________________________________________________________

  return (
    <div className={`${styles.categoryCard} ${overBudget ? styles.overBudget : ""}`}>

      <h3>{category.title}</h3>

      <div className={styles.categoryStats}>

        <div>

          <p className={styles.statLabel}>Budget</p>
          <p className={styles.statValue}>{formatMoney(category.categoryBudget)}</p>

        </div>

        <div>

          <p className={styles.statLabel}>Spent</p>
          <p className={styles.statValue}>{formatMoney(category.totalTasksPrice)}</p>

        </div>

        <div className={overBudget ? styles.warningText : ""}>

          <p className={styles.statLabel}>{overBudget ? "Over budget by" : "Remaining"}</p>
          <p className={styles.statValue}>{formatMoney(overBudget ? category.budgetOverrun : category.remainingBudget)}</p>

        </div>

        <div>

          <p className={styles.statLabel}>Category budget used</p>
          <p className={styles.statValue}>
            {category.categoryBudget > 0 ? formatPercent(category.budgetUsedPercent) : "No budget allocated"}
          </p>

        </div>

      </div>

      <div className={`${styles.shareTrack} ${overBudget ? styles.overBudgetTrack : ""}`}>

        <div style={
          { 
            width: `${overBudget ? 100 : Math.min(100, Math.max(0, category.budgetUsedPercent))}%`
          }
        }/>

      </div>

      <form className={styles.budgetForm} onSubmit={handleSave}>

        <label>Category budget</label>

        <div className={styles.inputRow}>

          <input 
            id={`budget-${category.id}`} 
            type="number" 
            min="0" 
            step="1" 
            required
            value={budget} 
            onChange={(event) => setBudget(event.target.value)} 
            disabled={disabled || saving} 
          />

          <button className={styles.button} disabled={disabled || saving} type="submit">
            {saving ? "Saving…" : "Save"}
          </button>

        </div>

      </form>

      {error && <p className={styles.warningText} role="alert">{error}</p>}
      
    </div>
  );
}
