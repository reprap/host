/*

  OpenSCAD file for generating STL models of RepRap build bases for display in the 
  RepRap Java Host software.

  Adrian Bowyer 27 November 2011

  Licence: GPL

*/

// Uncomment the next line for Mendel
//reprap_base(x=200, y=200, z=120);

//Uncomment the next line for Huxley
reprap_base(x=140, y=140, z=110);

module logo()
{
	linear_extrude(file = "reprap-logo.dxf", layer = "Layer_1", origin = [7,267.5],  scale = 0.3, height = 10, 
		center = true, convexity = 50, twist = 0 );
}

module x()
{
	union()
	{
		rotate([0,0,-30])
			cube([2,15,2], center=true);
		rotate([0,0,30])
			cube([2,15,2], center=true);
	}
}

module y()
{
	union()
	{
		rotate([0,0,-30])
			cube([2,15,2], center=true);
		rotate([0,0,30])
			translate([0,3.75,0])
				cube([2,7.5,2], center=true);
	}
}

module z()
{
	union()
	{
		rotate([0,0,-30])
			cube([2,15,2], center=true);
		translate([0,7,0])
			cube([7.5,2,2], center=true);
		translate([0,-7,0])
			cube([7.5,2,2], center=true);
	}
}


module reprap_base(x=140, y=140, z=110)
{
	union()
	{
		translate([-8,y-10,0])
			y();
	
		translate([x-10,-15,0])
			x();
	
		translate([-2,-2,z+10])
			rotate([90,0,0])
				z();
		
		difference()
		{
			translate([0,0,-2])
				cube([x,y,2]);
			
			translate([8,6.5,0])
				logo();
			
			for ( i = [0 : 1+floor(max(x/20, y/20))] )
			{
				if(i < 3)
				{
					translate([i*20-0.5,20,-10])
						cube([1,400,20]);
				} else
				{
					translate([i*20-0.5,-10,-10])
						cube([1,400,20]);
				}
				translate([-10,i*20-0.5,-10])
					cube([400,1,20]);
			}
		}
		
		translate([-2,-2,0])
		cube([2,2,z]);
	}
}


