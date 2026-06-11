import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles.css';

function App() {
  return (
    <main className="app-shell">
      <section className="intro">
        <p className="eyebrow">Labor market intelligence</p>
        <h1>LaborIQ</h1>
        <p>
          Explore occupations, wages, employment trends, skills, education requirements,
          and workforce insights using public datasets.
        </p>
      </section>
    </main>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
