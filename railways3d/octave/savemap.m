function savemap(M, name) 
	[n m, l] = size(M);
	img = uint8 (zeros(n, m, 3));
	if (l == 1)
		img( : , : , 1 ) = uint8(M * 255);
		img( : , : , 2 ) = uint8(M * 255);
		img( : , : , 3 ) = uint8(M * 255);
	else
		img( : , : , 1 : l ) = uint8(M * 255);
	endif
	imwrite(img, name);
endfunction