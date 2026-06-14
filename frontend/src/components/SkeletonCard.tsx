export function SkeletonCard() {
  return (
    <div className="skeleton-card" aria-hidden="true">
      <div className="sk sk-badge" />
      <div className="sk sk-title" />
      <div className="sk sk-soc" />
      <div className="skeleton-divider" />
      <div className="skeleton-stats">
        <div className="skeleton-stat-col">
          <div className="sk sk-label" />
          <div className="sk sk-value" />
        </div>
        <div className="skeleton-stat-col">
          <div className="sk sk-label" />
          <div className="sk sk-value" />
        </div>
      </div>
    </div>
  );
}
