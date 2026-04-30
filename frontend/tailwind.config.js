/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#e0fffe',
          100: '#b3fffd',
          200: '#66fffc',
          300: '#00fffc',
          400: '#00d1ff',
          500: '#00b8e6',
          600: '#0094c4',
          700: '#0070a0',
          800: '#005580',
          900: '#003a5c',
        },
        neon: {
          green: '#39FF14',
          blue:  '#00D1FF',
          pink:  '#FF006E',
        },
        dark: {
          900: '#030712',
          800: '#0A0F1E',
          700: '#111827',
          600: '#1a2235',
          500: '#1f2d45',
          400: '#243352',
        },
        ev: {
          charge: '#39FF14',
          idle:   '#FFA500',
          error:  '#FF3B3B',
          offline:'#6B7280',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Outfit', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'ev-gradient': 'linear-gradient(135deg, #0A0F1E 0%, #0d1f3c 50%, #0f2a4a 100%)',
        'card-gradient': 'linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.02) 100%)',
        'neon-glow': 'linear-gradient(90deg, #00D1FF, #39FF14)',
      },
      boxShadow: {
        'neon-blue': '0 0 20px rgba(0, 209, 255, 0.4), 0 0 60px rgba(0, 209, 255, 0.1)',
        'neon-green': '0 0 20px rgba(57, 255, 20, 0.4), 0 0 60px rgba(57, 255, 20, 0.1)',
        'card': '0 4px 24px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255,255,255,0.06)',
        'glass': '0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255,255,255,0.1)',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'glow': 'glow 2s ease-in-out infinite alternate',
        'charge': 'charge 1.5s ease-in-out infinite',
        'slide-up': 'slideUp 0.3s ease-out',
        'fade-in': 'fadeIn 0.4s ease-out',
      },
      keyframes: {
        glow: {
          '0%': { boxShadow: '0 0 10px rgba(57, 255, 20, 0.3)' },
          '100%': { boxShadow: '0 0 30px rgba(57, 255, 20, 0.8), 0 0 60px rgba(57, 255, 20, 0.3)' },
        },
        charge: {
          '0%, 100%': { opacity: '0.6', transform: 'scale(1)' },
          '50%': { opacity: '1', transform: 'scale(1.05)' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}
