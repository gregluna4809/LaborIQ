import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import type { OccupationWageDto } from '../types/occupation';
import { formatCompactDollars } from '../utils/format';

interface Props {
  wage: OccupationWageDto;
}

const BARS = [
  { key: 'p10Wage' as const, label: 'P10', color: '#99e6d8' },
  { key: 'p25Wage' as const, label: 'P25', color: '#2dd4bf' },
  { key: 'medianWage' as const, label: 'Median', color: '#0f766e' },
  { key: 'p75Wage' as const, label: 'P75', color: '#2dd4bf' },
  { key: 'p90Wage' as const, label: 'P90', color: '#99e6d8' },
];

interface ChartPoint {
  label: string;
  value: number;
  color: string;
}

interface CustomLabelProps {
  x?: number;
  y?: number;
  width?: number;
  value?: number;
}

function CustomLabel({ x = 0, y = 0, width = 0, value }: CustomLabelProps) {
  if (value == null) return null;
  return (
    <text
      x={x + width / 2}
      y={y - 8}
      textAnchor="middle"
      fill="#334e68"
      fontSize={12}
      fontWeight={700}
    >
      {formatCompactDollars(value)}
    </text>
  );
}

export function WageDistributionChart({ wage }: Props) {
  const data: ChartPoint[] = BARS.flatMap(({ key, label, color }) => {
    const value = wage[key];
    return value != null ? [{ label, value, color }] : [];
  });

  if (data.length === 0) return null;

  return (
    <div className="chart-wrapper">
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data} margin={{ top: 32, right: 8, left: 8, bottom: 4 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
          <XAxis
            dataKey="label"
            axisLine={false}
            tickLine={false}
            tick={{ fill: '#627d98', fontSize: 13, fontWeight: 600 }}
          />
          <YAxis
            tickFormatter={(v: number) => `$${(v / 1_000).toFixed(0)}K`}
            axisLine={false}
            tickLine={false}
            tick={{ fill: '#829ab1', fontSize: 12 }}
            width={52}
          />
          <Tooltip
            formatter={(value: number | string | readonly (string | number)[] | undefined) => {
              const n = value == null ? 0 : typeof value === 'number' ? value : Number(value);
              return [`$${n.toLocaleString('en-US')}`, 'Annual wage'];
            }}
            contentStyle={{
              borderRadius: 8,
              border: '1px solid #d9e2ec',
              boxShadow: '0 4px 16px rgba(0,0,0,.08)',
              fontSize: 13,
            }}
            cursor={{ fill: 'rgba(15,118,110,.06)' }}
          />
          <Bar dataKey="value" radius={[6, 6, 0, 0]} label={<CustomLabel />}>
            {data.map((point, i) => (
              <Cell key={i} fill={point.color} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
