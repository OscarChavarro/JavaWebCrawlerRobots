//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2005 - Oscar Chavarro: Original base version                 =
//= - March 14 2006 - Oscar Chavarro: Get/set interface                     =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================

package vsdk.toolkit.environment.geometry;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.Ray;

public class Sphere extends Solid {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    private double _radius;
    private double _radius_squared;
    private Vector3D _static_delta;
    private double [] _static_minmax;

    private PolyhedralBoundedSolid brepCache;

    public Sphere(double r) {
        _radius = r;
        _radius_squared = _radius*_radius;
        _static_delta = new Vector3D();
        _static_minmax = new double[6];
        brepCache = null;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doIntersection.
    @param inout_rayo
    @return true if given ray intersects current Sphere
    */
    @Override
    public boolean
    doIntersection(Ray inout_rayo) {
        /* OJO: Como en Java, a diferencia de C no hay sino objetos por
                referencia, no se puede hacer una declaraci&oacute;n 
                est&aacute;tica de un objeto, y poder hacerla es importante 
                porque la constructora Vector3D::Vector3D se ejecuta muchas veces, 
                y no se debe gastar tiempo creando objetos (i.e. haciento 
                Vector3D delta; delta = new Vector3D(); ...).  El c&oacute;digo
                original de MIT resolvi&oacute; &eacute;sto usando unos 
                flotantes double dx, dy, dz; e implementando una versi&oacute;n
                adicional de Vector3D::dotProduct() que recibe 3 doubles.  
                Se considera que esa soluci&oacute;n es "fea" y que al ofrecer
                el nuevo m&eacute;todo `Vector3D::dotProduct` se le 
                desorganiza la mente al usuario/programador.  Por eso, se 
                resolvi&oacute; implementar otra soluci&oacute;n (tal vez 
                igual de fea): a&ntilde;adir un nuevo atributo de clase en
                Sphere, y utilizarlo como su fuese una variable est&aacute;tica
                de tipo Vector3D dentro del m&eacute;todo.  Esto no es bueno 
                porque gasta memoria, pero ... que m&aacute;s podr&aacute; 
                hacerse? Al menos el tiempo de ejecuci&oacute;n se mantiene 
                igual respecto al c&oacute;digo original de MIT.
                NOTA: Comparar este m&eacute;todo modificado con la 
                      versi&oacute;n original en la etapa 1, con la 
                      ayuda de un profiler. ... */
        _static_delta.x = -inout_rayo.origin.x;
        _static_delta.y = -inout_rayo.origin.y;
        _static_delta.z = -inout_rayo.origin.z;
        double v = inout_rayo.direction.dotProduct(_static_delta);

        // Test if the inout_rayo actually intersects the sphere
        double t = _radius_squared + v*v 
                  - _static_delta.x*_static_delta.x 
                  - _static_delta.y*_static_delta.y 
                  - _static_delta.z*_static_delta.z;
        if ( t < 0 ) {
            return false;
        }

        // Test if the intersection is in the positive
        // inout_rayo direction
        t = v - Math.sqrt(t);
        if ( t < 0 ) {
            return false;
        }

        inout_rayo.t = t;
        return true;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doExtraInformation.
    @param inT
    */
    @Override
    public void
    doExtraInformation(Ray inRay, double inT, 
                                  GeometryIntersectionInformation outData) {
        //-----------------------------------------------------------------
        outData.p.x = inRay.origin.x + inT*inRay.direction.x;
        outData.p.y = inRay.origin.y + inT*inRay.direction.y;
        outData.p.z = inRay.origin.z + inT*inRay.direction.z;

        outData.n.x = outData.p.x;
        outData.n.y = outData.p.y;
        outData.n.z = outData.p.z;
        outData.n.normalize();

        //-----------------------------------------------------------------
        double theta;
        double phi;

        phi = Math.acos(outData.n.z);
        if ( outData.n.x > VSDK.EPSILON ) {
            theta = Math.atan(outData.n.y / outData.n.x) + 3*Math.PI/2;
          }
          else if ( outData.n.x < VSDK.EPSILON ) {
            // OJO: Habra una manera mas eficiente de lograr este intervalo?
            theta = Math.atan(outData.n.y / outData.n.x) + 3*Math.PI/2;
            theta += Math.PI;
            if ( theta > 2*Math.PI ) theta -= 2*Math.PI;
          }
          else {
            theta = 0.0;
        }
        // Suponiendo que theta esta en [0, 2*PI] y phi en [0, PI]...
        outData.u = ((theta+Math.PI/2)/(2*Math.PI));
        outData.v = 1 - (phi / Math.PI);

        //-----------------------------------------------------------------
        outData.t.x = Math.sin(theta-Math.PI/2);
        outData.t.y = -Math.cos(theta-Math.PI/2);
        outData.t.z = 0;

        //-----------------------------------------------------------------
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doContainmentTest.
    @return INSIDE, OUTSIDE or LIMIT constant value
    */
    @Override
    public int doContainmentTest(Vector3D p, double distanceTolerance)
    {
        double l = p.length();
        if ( l < _radius - distanceTolerance ) {
            return INSIDE;
        }
        else if ( l > _radius + distanceTolerance ) {
            return OUTSIDE;
        }
        return LIMIT;
    }

    /**
    @return a new 6 valued double array containing the coordinates of a min-max
    bounding box for current geometry.
    */
    @Override
    public double[] getMinMax()
    {
        for ( int i = 0; i < 3; i++ ) {
            _static_minmax[i] = -_radius;
        }
        for ( int i = 3; i < 6; i++ ) {
            _static_minmax[i] = _radius;
        }
        return _static_minmax;
    }

    public double getRadius()
    {
        return _radius;
    }

    public void setRadius(double r)
    {
        _radius = r;
        _radius_squared = r*r;
    }

    private static void
    spherePosition(Vector3D p, double theta, double t, double r)
    {
        double phi = (t-0.5)*Math.PI;
        p.x = Math.cos(phi) * Math.cos(theta) * r;
        p.y = Math.cos(phi) * Math.sin(theta) * r;
        p.z = Math.sin(phi) * r;
    }

    @Override
    public PolyhedralBoundedSolid exportToPolyhedralBoundedSolid()
    {
        if ( brepCache == null ) {
            brepCache = buildPolyhedralBoundedSolid();
        }
        return brepCache;
    }

    /**
    Given current sphere, this method generates a "polyhedral ball"
    aproximation.
    Note that this method follows a similar strategy to the one proposed on
    function "ball", from program [MANT1988].12.6, but it is expressed entirely
    on "low level" operators, and doesn't rely on the previous availability of
    generalized rotational sweep operations.
    */
    private PolyhedralBoundedSolid buildPolyhedralBoundedSolid()
    {
        double theta;
        double phi;
        int nparalels = 8;
        int nmeridians = 16;
        double dtheta = 2*Math.PI / ((double)nmeridians);
        double dphi = 1.0 / ((double)nparalels);
        int i, base2, base1;
        Vector3D pos;

        PolyhedralBoundedSolid solid;

        //- Build triangles for lower cap ---------------------------------
        solid = new PolyhedralBoundedSolid();
        pos = new Vector3D(0, 0, -_radius);
        solid.mvfs(pos, 1, 1);

        pos = new Vector3D();
        spherePosition(pos, dtheta, dphi, _radius);
        solid.smev(1, 1, 3, pos);
        pos = new Vector3D();
        spherePosition(pos, 0, dphi, _radius);
        solid.smev(1, 3, 2, pos);

        solid.mef(1, 1, 1, 3, 2, 3, 2);

        for ( i = 2; i < nmeridians; i++ ) {
            theta = dtheta * ((double)i);
            pos = new Vector3D();
            spherePosition(pos, theta, dphi, _radius);
            solid.smev(1, 1, (i+1)+1, pos);
            // Next face is <(1), (i+1), (i+0)>
            solid.mef(1,        /* seed face, always face 1 */
                      1,        /* seed face, always face 1 */
                      (i+0)+1,  /* start of half edge 1 */
                      (1),      /* end of half edge 1 */
                      (i+1)+1,  /* start of half edge 2 */
                      (1),      /* end of half edge 2 */
                      i+1       /* new face id */);
        }
        // Next face is <(1), (2), (i+1)>
        solid.mef(1,        /* seed face, always face 1 */
                  1,        /* seed face, always face 1 */
                  (i+1),    /* start of half edge 1 */
                  (1),      /* end of half edge 1 */
                  (2),      /* start of half edge 2 */
                  (3),      /* end of half edge 2 */
                  i+1       /* new face id */);
        base2 = i+2;
        base1 = 2;

        //- Build side quads for sphere body ------------------------------
        int p;
        for ( p = 0; p < nparalels-2; p++ ) {
            phi = ((double)(p+2)) / ((double)nparalels);
            for ( i = 0; i < nmeridians; i++ ) {
                theta = dtheta * ((double)i);
                pos = new Vector3D();
                spherePosition(pos, theta, phi, _radius);
                solid.smev(1, (i)+base1, (i)+base2, pos);
                if ( i > 0 ) {
                    // Next face is <(i), (i+base2), (i-1+base2), (i-1)>
                    solid.mef(1,           /* seed face, always face 1 */
                              1,           /* seed face, always face 1 */
                              (i-1)+base2, /* start of half edge 1 */
                              (i-1)+base1, /* end of half edge 1 */
                              (i)+base2,   /* start of half edge 2 */
                              (i)+base1,   /* end of half edge 2 */
                              base2+i+1    /* new face id */);
                }
            }
            solid.mef(1,           /* seed face, always face 1 */
                      1,           /* seed face, always face 1 */
                      (i+base2-1), /* start of half edge 1 */
                      (base1+i-1), /* end of half edge 1 */
                      (base2),     /* start of half edge 2 */
                      (base2+1),   /* end of half edge 2 */
                      base2+i+1    /* new face id */);
            base1 = base2;
            base2 += nmeridians;
        }

        //- Build triangles for upper cap --------------------------------
        pos = new Vector3D(0, 0, _radius);
        solid.smev(1, base1, base2, pos);

        for ( i = 0; i < nmeridians-2; i++ ) {
            solid.mef(1,           /* seed face, always face 1 */
                      1,           /* seed face, always face 1 */
                      base2,       /* start of half edge 1 */
                      base1+i,     /* end of half edge 1 */
                      base1+i+1,   /* start of half edge 2 */
                      base1+i+2,   /* end of half edge 2 */
                      base2+i+1    /* new face id */);
        }

        solid.mef(1,           /* seed face, always face 1 */
                  1,           /* seed face, always face 1 */
                  base2,       /* start of half edge 1 */
                  base1+i,     /* end of half edge 1 */
                  base1+i+1,   /* start of half edge 2 */
                  base1,   /* end of half edge 2 */
                  base2+i+1    /* new face id */);

        //-----------------------------------------------------------------
        return solid;
    }

    /**
    Given a (thetha, phi) spherical coordinate in the surface of current
    Sphere, this method writes on to `p` Vector3D the (x, y, z) coordinates
    of the corresponding point on Sphere's surface.
    \todo  check this method for efficiency improvement
    @param p
    @param theta
    @param phi
    */
    public void
    spherePosition(Vector3D p, double theta, double phi)
    {
        p.x = Math.cos(phi) * Math.cos(theta) * _radius;
        p.y = -Math.cos(phi) * Math.sin(theta) * _radius;
        p.z = Math.sin(phi) * _radius;
    }

    /**
    Given a (thetha, phi) spherical coordinate in the surface of current
    Sphere, this method writes on to `n` Vector3D the (nx, ny, nz) coordinates
    of the surface normal at corresponding point on Sphere's surface.
    \todo  check this method for efficiency improvement
    @param n
    @param theta
    @param phi
    */
    public void
    sphereNormal(Vector3D n, double theta, double phi)
    {
        n.x = Math.cos(phi) * Math.cos(theta);
        n.y = -Math.cos(phi) * Math.sin(theta);
        n.z = Math.sin(phi);
    }

    /**
    Given a (thetha, phi) spherical coordinate in the surface of current
    Sphere, this method writes on to `n` Vector3D the (tx, ty, tz) coordinates
    of the surface tangent at corresponding point on Sphere's surface. Tangents
    are aligned with respect to Sphere's equator.
    \todo  check this method for efficiency improvement
    \todo  check this method for efficiency improvement
    @param t
    @param theta
    @param phi
    */
    public void
    sphereTangent(Vector3D t, double theta, double phi)
    {
        t.x = Math.sin(theta);
        t.y = Math.cos(theta);
        t.z = 0;
    }

    /**
    Given a (thetha, phi) spherical coordinate in the surface of current
    Sphere, this method writes on to `n` Vector3D the (bx, by, bz) coordinates
    of the surface tangent binormal at corresponding point on Sphere's surface. 
    Tangents binormals are perpendicular to both normal and tangent.
    \todo  check this method for efficiency improvement
    @param b
    @param theta
    @param phi
    */
    public void
    sphereBinormal(Vector3D b, double theta, double phi)
    {
        b.x = -Math.sin(phi)*Math.cos(theta);
        b.y = Math.sin(phi)*Math.sin(theta);
        b.z = Math.cos(phi)*Math.cos(theta)*Math.cos(theta) + 
              Math.cos(phi)*Math.sin(theta)*Math.sin(theta);
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
