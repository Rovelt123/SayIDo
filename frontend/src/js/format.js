export function formatMoney(value) {
  return `${value.toLocaleString("en-US", { maximumFractionDigits: 2 })} kr.`;
}

export function formatPercent(value) {
  return `${value.toLocaleString("en-US", { maximumFractionDigits: 1 })}%`;
}
