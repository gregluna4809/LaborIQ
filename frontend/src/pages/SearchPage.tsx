import { FormEvent, useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { OccupationResults } from '../components/OccupationResults';
import { SkeletonCard } from '../components/SkeletonCard';
import { searchOccupations } from '../services/occupationApi';
import type { OccupationSearchResultDto } from '../types/occupation';

type Status = 'idle' | 'loading' | 'success' | 'error';

const SUGGESTIONS = ['Software Developer', 'Registered Nurse', 'Financial Analyst', 'Electrician'];

export function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQ = searchParams.get('q') ?? '';
  const [query, setQuery] = useState(initialQ);
  const [status, setStatus] = useState<Status>('idle');
  const [occupations, setOccupations] = useState<OccupationSearchResultDto[]>([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [lastQuery, setLastQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const didInit = useRef(false);

  const runSearch = useCallback(async (q: string) => {
    const term = q.trim();
    if (!term) {
      setStatus('idle');
      setOccupations([]);
      return;
    }
    setLastQuery(term);
    setStatus('loading');
    setOccupations([]);
    setErrorMessage('');
    try {
      const results = await searchOccupations(term);
      setOccupations(results);
      setStatus('success');
    } catch (err) {
      setStatus('error');
      setErrorMessage(err instanceof Error ? err.message : 'Search failed');
    }
  }, []);

  useEffect(() => {
    if (didInit.current) return;
    didInit.current = true;
    if (initialQ) void runSearch(initialQ);
  }, [initialQ, runSearch]);

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const term = query.trim();
    if (term) {
      setSearchParams({ q: term });
    } else {
      setSearchParams({});
    }
    void runSearch(term);
  }

  function handleSuggestion(s: string) {
    setQuery(s);
    setSearchParams({ q: s });
    void runSearch(s);
  }

  return (
    <main className="search-page">
      <section className="search-hero">
        <p className="eyebrow">Labor market intelligence</p>
        <h1 className="hero-title">Explore Occupations</h1>
        <p className="hero-tagline">
          Wages, employment counts, and workforce data from the Bureau of Labor Statistics.
        </p>

        <form className="search-form" onSubmit={handleSubmit}>
          <label htmlFor="occupation-search" className="search-label">
            Search by title or SOC code
          </label>
          <div className="search-row">
            <input
              ref={inputRef}
              id="occupation-search"
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Try: software developer, nurse, 15-1252…"
              autoComplete="off"
            />
            <button type="submit" disabled={status === 'loading'} className="search-btn">
              {status === 'loading' ? 'Searching…' : 'Search'}
            </button>
          </div>
        </form>
      </section>

      <section className="results-area" aria-live="polite" aria-label="Search results">
        {status === 'idle' && (
          <div className="empty-state">
            <div className="empty-icon" aria-hidden="true">🔍</div>
            <h2>Find any occupation</h2>
            <p>Search by job title, field, or SOC code to explore labor market data.</p>
            <div className="suggestion-chips">
              {SUGGESTIONS.map((s) => (
                <button key={s} className="suggestion-chip" onClick={() => handleSuggestion(s)}>
                  {s}
                </button>
              ))}
            </div>
          </div>
        )}

        {status === 'loading' && (
          <div className="results-section">
            <div className="results-header">
              <div className="sk sk-results-heading" />
            </div>
            <div className="results-grid">
              {Array.from({ length: 6 }).map((_, i) => (
                <SkeletonCard key={i} />
              ))}
            </div>
          </div>
        )}

        {status === 'error' && (
          <div className="error-state" role="alert">
            <div className="empty-icon" aria-hidden="true">⚠️</div>
            <h2>Search failed</h2>
            <p>{errorMessage}</p>
            <button className="retry-btn" onClick={() => void runSearch(lastQuery)}>
              Try again
            </button>
          </div>
        )}

        {status === 'success' && occupations.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon" aria-hidden="true">🔎</div>
            <h2>No results found</h2>
            <p>
              No occupations matched &ldquo;{lastQuery}&rdquo;. Try a broader term or a different
              SOC code.
            </p>
          </div>
        )}

        {status === 'success' && occupations.length > 0 && (
          <OccupationResults occupations={occupations} query={lastQuery} />
        )}
      </section>
    </main>
  );
}
