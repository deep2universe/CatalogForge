import { PieChart as RechartsPieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { Card, CardHeader, CardContent } from '@/components/ui';

interface PieChartData {
  name: string;
  value: number;
}

interface PieChartProps {
  title: string;
  data: PieChartData[];
  onSegmentClick?: (name: string) => void;
}

const COLORS = [
  '#A8D5E5', // pastel-blue
  '#B5E5CF', // pastel-green
  '#D4B5E5', // pastel-purple
  '#F5E6A3', // pastel-yellow
  '#E5CDB5', // pastel-orange
  '#E5B5B5', // pastel-red
];

export function PieChart({ title, data, onSegmentClick }: PieChartProps) {
  return (
    <Card>
      <CardHeader>
        <h3 className="text-lg font-semibold text-neutral-800">{title}</h3>
      </CardHeader>
      <CardContent>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <RechartsPieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={2}
                dataKey="value"
                onClick={(entry) => onSegmentClick?.(entry.name)}
                style={{ cursor: onSegmentClick ? 'pointer' : 'default' }}
              >
                {data.map((_, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #E5E5E5',
                  borderRadius: '8px',
                }}
              />
              <Legend />
            </RechartsPieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
