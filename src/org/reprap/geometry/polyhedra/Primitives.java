package org.reprap.geometry.polyhedra;

public class Primitives 
{
	static CSG3D cube(double x, double y, double z, boolean centre)
	{
		CSG3D result;
		if(centre)
		{
			x = 0.5*x;
			y = 0.5*y;
			z = 0.5*z;
			result = new CSG3D(new HalfSpace(new Point3D(1, 0, 0), new Point3D(x, 0, 0)));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, 1, 0), new Point3D(0, y, 0))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, 0, 1), new Point3D(0, 0, z))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(-1, 0, 0), new Point3D(-x, 0, 0))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, -1, 0), new Point3D(0, -y, 0))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, 0, -1), new Point3D(0, 0, -z))));
		} else
		{
			result = new CSG3D(new HalfSpace(new Point3D(1, 0, 0), new Point3D(x, 0, 0)));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, 1, 0), new Point3D(0, y, 0))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, 0, 1), new Point3D(0, 0, z))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(-1, 0, 0), new Point3D(0, 0, 0))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, -1, 0), new Point3D(0, 0, 0))));
			result = CSG3D.intersection(result, new CSG3D(new HalfSpace(new Point3D(0, 0, -1), new Point3D(0, 0, 0))));			
		}
		return result;
	}
	

}
