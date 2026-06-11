export interface OccupationDto {
  id: number;
  socCode: string;
  title: string;
  description: string | null;
  sourceMetadata: Record<string, unknown>;
}
