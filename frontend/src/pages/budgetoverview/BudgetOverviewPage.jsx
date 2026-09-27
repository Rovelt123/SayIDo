import { useEffect, useState } from "react";
import { Link } from "react-router";
import Navbar from "../../components/Navbar/Navbar";
import BudgetChart from "./BudgetChart";
import CategoryBudgetCard from "./CategoryBudgetCard";
import { budgetRequest } from "../../js/budgetApi";
import { formatMoney, formatPercent } from "../../js/format";
import styles from "./BudgetOverviewPage.module.css";

// ________________________________________________________

export default function BudgetOverviewPage() {

  const [wedding, setWedding] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState("");
  const [refresh, setRefresh] = useState(0);

// ________________________________________________________

  useEffect(() => {
    let active = true;

    budgetRequest("/weddings")
      .then((weddings) => {
        if (active) setWedding(weddings[0] ?? null);
      })
      .catch((error) => {
        if (active) setError(error.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [refresh]);

// ________________________________________________________

  function reload() {
    setLoading(true);
    setError("");
    setNotice("");
    setRefresh((value) => value + 1);
  }

// ________________________________________________________

  async function saveBudget(categoryId, categoryBudget) {
    setSaving(true);
    setNotice("");
    try {
      await budgetRequest(`/weddings/${wedding.id}/categories/${categoryId}/budget`, {
        method: "PATCH",
        body: JSON.stringify({ categoryBudget }),
      });
      try {
        const updated = await budgetRequest(`/weddings/${wedding.id}`);
        setWedding(updated);
        setNotice("Category budget saved.");
      } catch {
        setError("Your budget was saved, but the overview could not be refreshed. Please reload.");
      }
    } finally {
      setSaving(false);
    }
  }

// ________________________________________________________

  return (
    <div>
      <Navbar hideButtons />

      <main className={styles.page}>

        <Link className={styles.backLink} to="/homepage">← Back to wedding overview</Link>

        <div className={styles.pageHeader}>

          <div>

            <p className={styles.eyebrow}>{wedding?.title || "Your wedding"}</p>
            <h1>Budget Overview</h1>

          </div>

          <button className={styles.button} onClick={reload} disabled={loading || saving}>Refresh</button>

        </div>


        {loading && <p role="status">Loading your budget…</p>}

        {error && <p className={styles.warning} role="alert">{error}</p>}

        {notice && <p role="status">{notice}</p>}

        {!loading && !error && !wedding && (
          <p>No wedding yet. <Link to="/create">Create a wedding</Link> to view your budget.</p>
        )}

        {!loading && !error && wedding && (
          <div>

            <div className={styles.summary}>

              <BudgetChart wedding={wedding} />

              <div>

                <h2>Your budget at a glance</h2>

                <div className={styles.summaryStats}>

                  <div>
                    <p className={styles.statLabel}><span className={styles.spentDot} />Spent</p>
                    <p className={styles.statValue}>{formatMoney(wedding.totalSpent)}</p>
                  </div>

                  <div>
                    <p className={styles.statLabel}><span className={styles.remainingDot} />Remaining</p>
                    <p className={styles.statValue}>{formatMoney(wedding.remainingBudget)}</p>
                  </div>

                  <div>
                    <p className={styles.statLabel}>Budget used</p>
                    <p className={styles.statValue}>{formatPercent(wedding.budgetUsedPercent)}</p>
                  </div>

                </div>

                {wedding.budget <= 0 && <p className={styles.muted}>No budget allocated. Set your total budget in the wedding overview.</p>}
                
                {wedding.budgetOverrun > 0 && (
                  <p className={styles.warning}>
                    <i className="fa-solid fa-triangle-exclamation"/>{" "}
                    Over budget by <strong>{formatMoney(wedding.budgetOverrun)}</strong>
                  </p>
                )}
              
              </div>

            </div>

            <div className={styles.sectionHeader}>

              <h2>Category budgets</h2>
              
            </div>
            
            <div className={styles.categories}>
              {wedding.categories.map((category) => (
                <CategoryBudgetCard 
                  key={`${category.id}-${category.categoryBudget}`} 
                  category={category}
                  onSave={saveBudget} disabled={saving} 
                />
              ))}
            </div>

            {wedding.categories.length === 0 && <p>No categories yet. Add categories from your wedding overview.</p>}

          </div>
        )}

      </main>

    </div>
  );
}
