import type { OccupationSearchResultDto } from '../types/occupation';
import { OccupationCard } from './OccupationCard';

interface Props {
  occupations: OccupationSearchResultDto[];
  query: string;
}

export function OccupationResults({ occupations, query }: Props) {
  return (
    <section className="results-section" aria-label="Occupation search results">
      <div className="results-header">
        <h2 className="results-title">
          Results for <em>&ldquo;{query}&rdquo;</em>
        </h2>
        <span className="results-count">{occupations.length} found</span>
      </div>
      <div className="results-grid">
        {occupations.map((occ) => (
          <OccupationCard key={occ.socCode} occupation={occ} />
        ))}
      </div>
    </section>
  );
}
