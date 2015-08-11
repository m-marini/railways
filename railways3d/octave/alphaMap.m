function M = alphaMap(Z, l)
	[n m] = size(Z);
	M = zeros(n, m, l);
	s = 1 / (2 * l - 1);
	for i = 0 : l - 1
		M( : , : , i + 1) = range(Z, (2 * i - 1) * s,  s) - range(Z, (2 * i + 1) * s, s);
	endfor
endfunction