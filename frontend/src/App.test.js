import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders platform title", () => {
  render(<App />);
  expect(screen.getByText(/Tech Salary Transparency Platform/i)).toBeInTheDocument();
});
