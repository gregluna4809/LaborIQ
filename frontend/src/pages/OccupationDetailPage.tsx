import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { WageDistributionChart } from '../components/WageDistributionChart';
import {
  getEducation,
  getEmploymentHistory,
  getLatestWages,
  getOccupation,
  getRelatedOccupations,
  getSkills,
} from '../services/occupationApi';
import type {
  OccupationDto,
  OccupationEducationDto,
  OccupationEmploymentDto,
  OccupationSkillDto,
  OccupationWageDto,
  RelatedOccupationDto,
} from '../types/occupation';
import { formatCurrency, formatEmployment } from '../utils/format';

function DetailSkeleton() {
  return (
    <div className="detail-page" aria-busy="true" aria-label="Loading occupation data">
      <div className="sk sk-back" />
      <div className="sk sk-detail-badge" style={{ marginTop: 24 }} />
      <div className="sk sk-detail-title" />
      <div className="sk sk-detail-meta" />
      <div className="stats-hero">
        {[0, 1, 2].map((i) => (
          <div key={i} className="stat-card skeleton-stat-card">
            <div className="sk sk-stat-label" />
            <div className="sk sk-stat-value" />
          </div>
        ))}
      </div>
      <div className="sk sk-chart-block" />
    </div>
  );
}

interface StatCardProps {
  label: string;
  value: string;
  accent?: boolean;
  sub?: string;
}

function StatCard({ label, value, accent, sub }: StatCardProps) {
  return (
    <div className={`stat-card${accent ? ' stat-card-accent' : ''}`}>
      <span className="stat-card-label">{label}</span>
      <span className="stat-card-value">{value}</span>
      {sub && <span className="stat-card-sub">{sub}</span>}
    </div>
  );
}

interface SectionProps {
  title: string;
  children: React.ReactNode;
}

function Section({ title, children }: SectionProps) {
  return (
    <div className="detail-section">
      <h2 className="section-title">{title}</h2>
      {children}
    </div>
  );
}

function EducationRow({ item }: { item: OccupationEducationDto }) {
  const pct = item.percentage ?? 0;
  return (
    <div className="edu-row">
      <span className="edu-level">{item.educationLevel}</span>
      <div className="edu-bar-track">
        <div className="edu-bar-fill" style={{ width: `${Math.min(pct, 100)}%` }} />
      </div>
      <span className="edu-pct">{pct.toFixed(0)}%</span>
    </div>
  );
}

export function OccupationDetailPage() {
  const { socCode } = useParams<{ socCode: string }>();
  const navigate = useNavigate();

  const [occupation, setOccupation] = useState<OccupationDto | null>(null);
  const [wage, setWage] = useState<OccupationWageDto | null>(null);
  const [employment, setEmployment] = useState<OccupationEmploymentDto | null>(null);
  const [skills, setSkills] = useState<OccupationSkillDto[]>([]);
  const [education, setEducation] = useState<OccupationEducationDto[]>([]);
  const [related, setRelated] = useState<RelatedOccupationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!socCode) return;

    setLoading(true);
    setError('');

    Promise.allSettled([
      getOccupation(socCode),
      getLatestWages(socCode),
      getEmploymentHistory(socCode),
      getSkills(socCode),
      getEducation(socCode),
      getRelatedOccupations(socCode),
    ]).then(([occR, wageR, empR, skillR, eduR, relR]) => {
      if (occR.status === 'rejected') {
        setError(`Occupation "${socCode}" not found.`);
        setLoading(false);
        return;
      }

      setOccupation(occR.value);
      setWage(wageR.status === 'fulfilled' ? wageR.value : null);
      setEmployment(
        empR.status === 'fulfilled' ? (empR.value[0] ?? null) : null,
      );
      setSkills(skillR.status === 'fulfilled' ? skillR.value : []);
      setEducation(eduR.status === 'fulfilled' ? eduR.value : []);
      setRelated(relR.status === 'fulfilled' ? relR.value : []);
      setLoading(false);
    });
  }, [socCode]);

  if (loading) return <DetailSkeleton />;

  if (error) {
    return (
      <main className="detail-page">
        <div className="error-state" role="alert">
          <div className="empty-icon" aria-hidden="true">⚠️</div>
          <h2>Not Found</h2>
          <p>{error}</p>
          <button className="retry-btn" onClick={() => navigate('/')}>
            Back to Search
          </button>
        </div>
      </main>
    );
  }

  if (!occupation) return null;

  const occGroup = occupation.sourceMetadata?.occGroup as string | undefined;
  const dataSource = occupation.sourceMetadata?.source as string | undefined;

  return (
    <main className="detail-page">
      <Link to="/" className="back-link">
        ← Back to search
      </Link>

      <header className="detail-header">
        {occGroup && (
          <span
            className={`badge ${
              occGroup.toUpperCase() === 'DETAILED'
                ? 'badge-detailed'
                : occGroup.toUpperCase() === 'BROAD'
                  ? 'badge-broad'
                  : 'badge-major'
            }`}
          >
            {occGroup.toUpperCase()}
          </span>
        )}
        <h1 className="detail-title">{occupation.title}</h1>
        <p className="detail-meta">
          SOC {occupation.socCode}
          {dataSource && ` · ${dataSource}`}
          {wage?.year && ` · ${wage.year} data`}
        </p>
      </header>

      <div className="stats-hero">
        <StatCard
          label="Median Annual Wage"
          value={formatCurrency(wage?.medianWage)}
          accent={wage?.medianWage != null}
        />
        <StatCard
          label="Mean Annual Wage"
          value={formatCurrency(wage?.meanWage)}
        />
        <StatCard
          label="Total Employed"
          value={formatEmployment(employment?.employmentCount)}
          sub={employment?.year != null ? `as of ${employment.year}` : undefined}
        />
      </div>

      {wage && (
        <Section title="Wage Distribution">
          <p className="section-sub">Annual wages by percentile · {wage.year} BLS OEWS data</p>
          <WageDistributionChart wage={wage} />
          <div className="wage-footnote">
            <span>P10: {formatCurrency(wage.p10Wage)}</span>
            <span>P25: {formatCurrency(wage.p25Wage)}</span>
            <span>Median: {formatCurrency(wage.medianWage)}</span>
            <span>P75: {formatCurrency(wage.p75Wage)}</span>
            <span>P90: {formatCurrency(wage.p90Wage)}</span>
          </div>
        </Section>
      )}

      <div className="detail-two-col">
        {education.length > 0 && (
          <Section title="Education Profile">
            <p className="section-sub">Typical education level of workers in this occupation</p>
            <div className="edu-list">
              {education.map((item) => (
                <EducationRow key={item.id} item={item} />
              ))}
            </div>
          </Section>
        )}

        {related.length > 0 && (
          <Section title="Related Occupations">
            <ul className="related-list">
              {related.map((r) => (
                <li key={r.id}>
                  <Link to={`/occupations/${r.targetSocCode}`} className="related-link">
                    <span className="related-soc">SOC {r.targetSocCode}</span>
                    <span className="related-type">{r.relationshipType}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </Section>
        )}
      </div>

      {skills.length > 0 && (
        <Section title="Top Skills">
          <p className="section-sub">Skills ranked by importance score</p>
          <div className="skills-list">
            {skills.slice(0, 10).map((skill) => (
              <span key={skill.id} className="skill-pill" title={`Importance: ${skill.importanceScore ?? '—'}`}>
                {skill.skillName}
              </span>
            ))}
          </div>
        </Section>
      )}

      {wage == null && employment == null && education.length === 0 && skills.length === 0 && (
        <div className="empty-state" style={{ marginTop: 32 }}>
          <div className="empty-icon" aria-hidden="true">📊</div>
          <h2>No data available yet</h2>
          <p>
            Detailed labor market data for this occupation will be populated when OEWS ingestion
            completes.
          </p>
        </div>
      )}
    </main>
  );
}
