/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        pastel: {
          blue: {
            DEFAULT: '#A8D5E5',
            dark: '#7BC0D4',
            light: '#D4EAF2',
          },
          green: {
            DEFAULT: '#B5E5CF',
            dark: '#8AD4B5',
            light: '#DAF2E7',
          },
          purple: {
            DEFAULT: '#D4B5E5',
            dark: '#B98AD4',
            light: '#E9DAF2',
          },
          yellow: {
            DEFAULT: '#F5E6A3',
            dark: '#E8D47A',
            light: '#FAF3D1',
          },
          red: {
            DEFAULT: '#E5B5B5',
            dark: '#D48A8A',
            light: '#F2DADA',
          },
          orange: {
            DEFAULT: '#E5CDB5',
            dark: '#D4B08A',
            light: '#F2E6DA',
          },
        },
        sidebar: '#F0F7FA',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px 0 rgb(0 0 0 / 0.05), 0 1px 2px -1px rgb(0 0 0 / 0.05)',
        'card-hover': '0 4px 6px -1px rgb(0 0 0 / 0.07), 0 2px 4px -2px rgb(0 0 0 / 0.07)',
      },
    },
  },
  plugins: [],
};
