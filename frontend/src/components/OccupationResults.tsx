import type { OccupationDto } from '../types/occupation';

interface OccupationResultsProps {
  occupations: OccupationDto[];
}

export function OccupationResults({ occupations }: OccupationResultsProps) {
  return (
    <section className="results-section" aria-label="Occupation search results">
      <div className="results-header">
        <h2>Search Results</h2>
        <span>{occupations.length} found</span>
      </div>

      <div className="results-grid">
        {occupations.map((occupation) => (
          <article className="occupation-card" key={occupation.socCode}>
            <div>
              <h3>{occupation.title}</h3>
              <p className="soc-code">SOC {occupation.socCode}</p>
            </div>
            <SourceMetadata metadata={occupation.sourceMetadata} />
          </article>
        ))}
      </div>
    </section>
  );
}

function SourceMetadata({ metadata }: { metadata: Record<string, unknown> | null | undefined }) {
  if (!metadata || Object.keys(metadata).length === 0) {
    return <p className="metadata-empty">No source metadata available</p>;
  }

  return (
    <dl className="metadata-list">
      {Object.entries(metadata).map(([key, value]) => (
        <div key={key}>
          <dt>{formatMetadataKey(key)}</dt>
          <dd>{formatMetadataValue(value)}</dd>
        </div>
      ))}
    </dl>
  );
}

function formatMetadataKey(key: string) {
  return key
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/[_-]/g, ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatMetadataValue(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return 'Not provided';
  }

  if (typeof value === 'object') {
    return JSON.stringify(value);
  }

  return String(value);
}
