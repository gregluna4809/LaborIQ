export interface OccupationDto {
  id: number;
  socCode: string;
  title: string;
  description: string | null;
  sourceMetadata: Record<string, unknown>;
}

export interface OccupationSearchResultDto {
  socCode: string;
  title: string;
  occGroup: string | null;
  latestMedianWage: number | null;
  latestWageYear: number | null;
  latestEmploymentCount: number | null;
  latestEmploymentYear: number | null;
}

export interface OccupationWageDto {
  id: number;
  socCode: string;
  year: number;
  medianWage: number | null;
  meanWage: number | null;
  p10Wage: number | null;
  p25Wage: number | null;
  p75Wage: number | null;
  p90Wage: number | null;
}

export interface OccupationEmploymentDto {
  id: number;
  socCode: string;
  year: number;
  employmentCount: number | null;
}

export interface OccupationSkillDto {
  id: number;
  socCode: string;
  skillName: string;
  importanceScore: number | null;
}

export interface OccupationEducationDto {
  id: number;
  socCode: string;
  educationLevel: string;
  percentage: number | null;
}

export interface RelatedOccupationDto {
  id: number;
  sourceSocCode: string;
  targetSocCode: string;
  relationshipType: string;
}
