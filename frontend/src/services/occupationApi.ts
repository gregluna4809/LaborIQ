import type {
  OccupationDto,
  OccupationWageDto,
  OccupationEmploymentDto,
  OccupationSkillDto,
  OccupationEducationDto,
  RelatedOccupationDto,
} from '../types/occupation';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

async function get<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}

export function searchOccupations(query: string): Promise<OccupationDto[]> {
  return get(`/api/occupations/search?${new URLSearchParams({ q: query })}`);
}

export function getOccupation(socCode: string): Promise<OccupationDto> {
  return get(`/api/occupations/${encodeURIComponent(socCode)}`);
}

export function getLatestWages(socCode: string): Promise<OccupationWageDto> {
  return get(`/api/occupations/${encodeURIComponent(socCode)}/wages/latest`);
}

export function getEmploymentHistory(socCode: string): Promise<OccupationEmploymentDto[]> {
  return get(`/api/occupations/${encodeURIComponent(socCode)}/employment`);
}

export function getSkills(socCode: string): Promise<OccupationSkillDto[]> {
  return get(`/api/occupations/${encodeURIComponent(socCode)}/skills`);
}

export function getEducation(socCode: string): Promise<OccupationEducationDto[]> {
  return get(`/api/occupations/${encodeURIComponent(socCode)}/education`);
}

export function getRelatedOccupations(socCode: string): Promise<RelatedOccupationDto[]> {
  return get(`/api/occupations/${encodeURIComponent(socCode)}/related`);
}
