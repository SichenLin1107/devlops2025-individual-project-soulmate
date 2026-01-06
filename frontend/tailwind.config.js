/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html","./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: { 
    extend: {
      colors: {
        'warm-rose': '#F8BBD9',
        'warm-peach': '#FFCCCB',
        'warm-lavender': '#E1D5E7',
        'warm-mint': '#B8E6B8',
        'warm-cream': '#FFF8E7',
        'warm-blush': '#FFE4E1',
      }
    } 
  },
  plugins: [],
  // 启用 preflight 但通过 CSS 层级保护 Element Plus
  corePlugins: { preflight: true }
}
