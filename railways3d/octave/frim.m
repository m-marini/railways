img = imread("git/railways/railways3d//src/main/resources/Textures/station-terrain-height.png" );

axis = linspace(-256, 255, 512);

[xx, yy] = meshgrid(axis, axis);

zz = double( img( : , : , 1) ) ;

#zz = zeros(512, 512);

mesh( xx, yy ,zz )