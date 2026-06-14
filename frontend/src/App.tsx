import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Header } from './components/Header';
import { OccupationDetailPage } from './pages/OccupationDetailPage';
import { SearchPage } from './pages/SearchPage';

export default function App() {
  return (
    <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<SearchPage />} />
        <Route path="/occupations/:socCode" element={<OccupationDetailPage />} />
      </Routes>
    </BrowserRouter>
  );
}
