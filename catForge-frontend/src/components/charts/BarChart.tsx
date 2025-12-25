import { BarChart as RechartsBarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Card, CardHeader, CardContent } from '@/components/ui';

interface BarChartData {
  name: string;
  value: number;
}

interface BarChartProps {
  title: string;
  data: BarChartData[];
  color?: string;
  onBarClick?: (name: string) => void;
}

export function BarChart({ title, data, color = '#A8D5E5', onBarClick }: BarChartProps) {
  return (
    <Card>
      <CardHeader>
        <h3 className="text-lg font-semibold text-neutral-800">{title}</h3>
      </CardHeader>
      <CardContent>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <RechartsBarChart data={data} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#E5E5E5" />
              <XAxis type="number" tick={{ fill: '#525252', fontSize: 12 }} />
              <YAxis
                type="category"
                dataKey="name"
                tick={{ fill: '#525252', fontSize: 12 }}
                width={100}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #E5E5E5',
                  borderRadius: '8px',
                }}
              />
              <Bar
                dataKey="value"
                fill={color}
                radius={[0, 4, 4, 0]}
                onClick={(entry) => onBarClick?.(entry.name)}
                style={{ cursor: onBarClick ? 'pointer' : 'default' }}
              />
            </RechartsBarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
