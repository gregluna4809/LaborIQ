import { Link, NavLink } from 'react-router-dom';

export function Header() {
  return (
    <header className="site-header">
      <div className="site-header-inner">
        <Link to="/" className="site-logo">
          <span className="logo-mark">LQ</span>
          <span className="logo-name">LaborIQ</span>
        </Link>
        <nav className="site-nav" aria-label="Main navigation">
          <NavLink
            to="/"
            className={({ isActive }) => `nav-link${isActive ? ' nav-link-active' : ''}`}
          >
            Search
          </NavLink>
        </nav>
      </div>
    </header>
  );
}
