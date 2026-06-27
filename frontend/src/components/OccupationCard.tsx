import { Link } from 'react-router-dom';
import type { OccupationSearchResultDto } from '../types/occupation';
import { formatCurrency, formatEmployment } from '../utils/format';

interface Props {
  occupation: OccupationSearchResultDto;
}

type OccGroup = 'DETAILED' | 'BROAD' | 'MAJOR' | string;

function OccGroupBadge({ group }: { group: OccGroup | null }) {
  if (!group) return null;
  const upper = group.toUpperCase();
  const cls =
    upper === 'DETAILED' ? 'badge badge-detailed'
    : upper === 'BROAD' ? 'badge badge-broad'
    : 'badge badge-major';
  return <span className={cls}>{upper}</span>;
}

export function OccupationCard({ occupation }: Props) {
  const occGroup = occupation.occGroup as OccGroup | null;

  return (
    <Link to={`/occupations/${occupation.socCode}`} className="occupation-card">
      <div className="card-head">
        <OccGroupBadge group={occGroup} />
        <h3 className="card-title">{occupation.title}</h3>
        <p className="card-soc">SOC {occupation.socCode}</p>
      </div>
      <div className="card-stats">
        <div className="card-stat">
          <span className="stat-label">Median Wage</span>
          <span className={`stat-value${occupation.latestMedianWage != null ? ' stat-wage' : ''}`}>
            {formatCurrency(occupation.latestMedianWage)}
          </span>
          {occupation.latestWageYear != null && (
            <span className="stat-year">{occupation.latestWageYear}</span>
          )}
        </div>
        <div className="card-stat">
          <span className="stat-label">Employment</span>
          <span className="stat-value">{formatEmployment(occupation.latestEmploymentCount)}</span>
          {occupation.latestEmploymentYear != null && (
            <span className="stat-year">{occupation.latestEmploymentYear}</span>
          )}
        </div>
      </div>
    </Link>
  );
}
