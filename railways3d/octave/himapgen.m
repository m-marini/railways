function Z = himapgen(st = 9, land = 1) 
	n = 2 ^ st + 1;

	Z = zeros(n, n);

	f = @(s,o) s * rand() + o;

#	Z(1, 1) = f(2, -1);
#	Z(1, n) = f(2, -1);
#	Z(n ,1) = f(2, -1);
#	Z(n, n) = f(2, -1);

	st = n - 1;
	scale = 1.0;
	offset = -0.5;

	while (st >= 2)
		for i = 1 : st : n - 1
			for j = 1 : st : n - 1
				Z = fractQuad(Z, i, j, st, (@() scale * rand() + offset) );
			endfor
		endfor
		scale = scale / 2;
		offset = offset / 2;
		st = st / 2;
	endwhile

	if (land < 1)
		th = quantile(reshape(Z, n * n, 1), 1 - land);
		Z = (Z - th);
		Z = Z .* (Z > 0);
	endif
	
	offset = min(min(Z));
	maxvalue = max(max(Z));
	Z = (Z - offset) / (maxvalue - offset);

endfunction