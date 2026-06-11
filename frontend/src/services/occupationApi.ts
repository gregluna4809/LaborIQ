import type { OccupationDto } from '../types/occupation';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export async function searchOccupations(query: string): Promise<OccupationDto[]> {
  const params = new URLSearchParams({ q: query });
  const response = await fetch(`${API_BASE_URL}/api/occupations/search?${params.toString()}`);

  if (!response.ok) {
    throw new Error(`Occupation search failed with status ${response.status}`);
  }

  return response.json() as Promise<OccupationDto[]>;
}
