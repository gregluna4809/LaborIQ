import { FormEvent, useState } from 'react';
import { OccupationResults } from './components/OccupationResults';
import { searchOccupations } from './services/occupationApi';
import type { OccupationDto } from './types/occupation';

type SearchStatus = 'idle' | 'loading' | 'success' | 'error';

export default function App() {
  const [query, setQuery] = useState('');
  const [submittedQuery, setSubmittedQuery] = useState('');
  const [occupations, setOccupations] = useState<OccupationDto[]>([]);
  const [status, setStatus] = useState<SearchStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const normalizedQuery = query.trim();
    setSubmittedQuery(normalizedQuery);
    setErrorMessage('');
    setOccupations([]);

    if (!normalizedQuery) {
      setStatus('success');
      return;
    }

    setStatus('loading');

    try {
      const results = await searchOccupations(normalizedQuery);
      setOccupations(results);
      setStatus('success');
    } catch (error) {
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'Unable to search occupations');
    }
  }

  return (
    <main className="app-shell">
      <section className="hero-section">
        <p className="eyebrow">Labor market intelligence</p>
        <h1>LaborIQ</h1>
        <p className="tagline">Labor market intelligence powered by public workforce data.</p>

        <form className="search-form" onSubmit={handleSubmit}>
          <label htmlFor="occupation-search">Search by occupation title or SOC code</label>
          <div className="search-row">
            <input
              id="occupation-search"
              name="q"
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Try software, nurse, 13-0000"
              autoComplete="off"
            />
            <button type="submit" disabled={status === 'loading'}>
              {status === 'loading' ? 'Searching...' : 'Search'}
            </button>
          </div>
        </form>
      </section>

      <section className="content-section" aria-live="polite">
        {status === 'idle' && (
          <div className="state-panel">
            <h2>Start with a title or SOC code</h2>
            <p>Search will return matching occupations from the LaborIQ backend.</p>
          </div>
        )}

        {status === 'loading' && (
          <div className="state-panel">
            <h2>Searching occupations</h2>
            <p>Fetching matching workforce records.</p>
          </div>
        )}

        {status === 'error' && (
          <div className="state-panel state-error" role="alert">
            <h2>Search failed</h2>
            <p>{errorMessage}</p>
          </div>
        )}

        {status === 'success' && occupations.length === 0 && (
          <div className="state-panel">
            <h2>No occupations found</h2>
            <p>
              {submittedQuery
                ? `No results matched "${submittedQuery}".`
                : 'Enter a search term to find occupations.'}
            </p>
          </div>
        )}

        {status === 'success' && occupations.length > 0 && (
          <OccupationResults occupations={occupations} />
        )}
      </section>
    </main>
  );
}
