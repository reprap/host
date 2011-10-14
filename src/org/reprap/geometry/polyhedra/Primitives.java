package org.reprap.geometry.polyhedra;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

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
	
	
	/**
	 * See http://en.wikibooks.org/wiki/OpenSCAD_User_Manual/Other_Language_Features
	 */
	private static int getFragmentsFromR(double r, int fn, double fs, double fa)
    {
            if (fn > 0)
                    return fn;
            return (int)Math.ceil(Math.max(Math.min(360.0 / fa, r*Math.PI / fs), 5));
    }
	
	//cylinder($fn=20,$fa=12,$fs=1,h=3,r1=2,r2=2,center=false)
	//fa is the minimum angle for a fragment.
	//fs is the minimum size of a fragment.
	//fn is usually 0. When this variable has a value greater than zero, the other two variables are ignored 
	//and full circle is rendered using this number of fragments.
	static CSG3D cylinder(int fn, double fa, double fs, double h, double r1, double r2, boolean centre)
	{
		fn = getFragmentsFromR(Math.min(r1,r2), fn, fs, fa);
		Point3D p1 = new Point3D(r1,0,0);
		Point3D p2 = new Point3D(r2,0,h);
		Point3D p3 = new Point3D(r1,10,0);
		CSG3D s = new CSG3D(new HalfSpace(p1,p2,p3));
		double a = 2.0*Math.PI/(double)fn;
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.set(new AxisAngle4d(0, 0, 1, a));
		
		CSG3D result = CSG3D.universe();
		for(int i = 0; i < fn; i++)
		{
			result = CSG3D.intersection(result, s);
			s = s.transform(m);
		}
		p3 = new Point3D(0,0,h);
		result = CSG3D.intersection(result, new CSG3D(new HalfSpace(p3,p3)));
		p2 = new Point3D(0,0,0);
		result = CSG3D.intersection(result, new CSG3D(new HalfSpace(p3.neg(),p2)));
		if(centre)
		{
			m.setIdentity();
			m.setTranslation(new Vector3d(0, 0, -h/2));
			result = result.transform(m);
		}
		return result;
	}
}
