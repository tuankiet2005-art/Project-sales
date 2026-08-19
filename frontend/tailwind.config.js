/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#161410",
        paper: "#f4efe6",
        copper: "#b45309",
        forest: "#1f3d2b",
        mist: "#e7e0d4",
      },
      fontFamily: {
        display: ["Fraunces", "Georgia", "serif"],
        sans: ["Manrope", "system-ui", "sans-serif"],
      },
      maxWidth: {
        page: "86.4rem",
        quote: "76.8rem",
      },
      boxShadow: {
        card: "0 18px 40px -24px rgba(22, 20, 16, 0.35)",
      },
    },
  },
  plugins: [],
};
