import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getLatestWages, getEmploymentHistory } from '../services/occupationApi';
import type { OccupationDto, OccupationEmploymentDto, OccupationWageDto } from '../types/occupation';
import { formatCurrency, formatEmployment } from '../utils/format';

interface Props {
  occupation: OccupationDto;
}

type OccGroup = 'DETAILED' | 'BROAD' | 'MAJOR' | string;

function OccGroupBadge({ group }: { group: OccGroup | undefined }) {
  if (!group) return null;
  const upper = group.toUpperCase();
  const cls =
    upper === 'DETAILED' ? 'badge badge-detailed'
    : upper === 'BROAD' ? 'badge badge-broad'
    : 'badge badge-major';
  return <span className={cls}>{upper}</span>;
}

export function OccupationCard({ occupation }: Props) {
  const [wage, setWage] = useState<OccupationWageDto | null>(null);
  const [employment, setEmployment] = useState<OccupationEmploymentDto | null>(null);

  useEffect(() => {
    // Responses ordered DESC by year; first item is most recent.
    getLatestWages(occupation.socCode).then(setWage).catch(() => setWage(null));
    getEmploymentHistory(occupation.socCode)
      .then((list) => setEmployment(list[0] ?? null))
      .catch(() => setEmployment(null));
  }, [occupation.socCode]);

  const occGroup = occupation.sourceMetadata?.occGroup as OccGroup | undefined;

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
          <span className={`stat-value${wage?.medianWage != null ? ' stat-wage' : ''}`}>
            {formatCurrency(wage?.medianWage)}
          </span>
          {wage?.year != null && <span className="stat-year">{wage.year}</span>}
        </div>
        <div className="card-stat">
          <span className="stat-label">Employment</span>
          <span className="stat-value">{formatEmployment(employment?.employmentCount)}</span>
          {employment?.year != null && <span className="stat-year">{employment.year}</span>}
        </div>
      </div>
    </Link>
  );
}
