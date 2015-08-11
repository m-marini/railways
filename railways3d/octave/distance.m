function M = distance(P, n = 513, m = 513)
	[ X , Y ] = meshgrid(0 : n - 1, 0 : m - 1);
	X = reshape(X, n * m, 1);
	Y = reshape(Y, n * m, 1);
	O = [ X  Y ];
	k = size(P, 1);
	D = zeros(n * m , 1);
	FF = zeros(n * m , 1);
	D = segDist(O, P);
	M = reshape(D, n, m);
endfunction