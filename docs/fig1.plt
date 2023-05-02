clear
set terminal latex
set output "fig1.tex"
set parametric
set trange [0:1]
plot 2*t,2*t
