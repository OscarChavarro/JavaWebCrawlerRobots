//===========================================================================
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [WAGN2004] Wagner, Max. "Ray/Torus Intersection". CS400 course homework =
//= report for CS400 class                                                  =
//===========================================================================

package vsdk.toolkit.environment.geometry;

// VSDK classes
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.processing.SolverPolynomialQuarticBairstow;

/**
Current implementation is based on [WAGN2004].
*/
public class Torus extends Solid 
{
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20131024L;
    
    private double majorRadius;
    private double minorRadius;
    
    private final GeometryIntersectionInformation lastInfo;

    /**
    @param inMajorRadius
    @param inMinorRadius 
    */
    public Torus(final double inMajorRadius, final double inMinorRadius)
    {
        majorRadius = inMajorRadius;
        minorRadius = inMinorRadius;
        
        lastInfo = new GeometryIntersectionInformation();
    }

    /**
    @return the majorRadius
    */
    public double getMajorRadius() 
    {
        return majorRadius;
    }

    /**
     * @param rMajor the majorRadius to set
     */
    public void setMajorRadius(double rMajor) 
    {
        this.majorRadius = rMajor;
    }

    /**
    @return the minorRadius
    */
    public double getMinorRadius() 
    {
        return minorRadius;
    }

    /**
    @param rMinor the minorRadius to set
    */
    public void setMinorRadius(double rMinor) 
    {
        this.minorRadius = rMinor;
    }

    @Override
    public boolean doIntersection(Ray inOut_ray) 
    {
        Vector3D p = inOut_ray.origin;

        inOut_ray.direction.normalize();
        Vector3D d = inOut_ray.direction;

        double alpha, beta, gama;

        alpha = d.dotProduct(d);
        beta = 2 * p.dotProduct(d);
        gama = p.dotProduct(p) - (minorRadius * minorRadius) - (majorRadius * majorRadius);

        double a4, a3, a2, a1, a0;

        a4 = alpha * alpha;
        a3 = 2 * alpha * beta;
        a2 = (beta * beta) + 2 * alpha * gama + 4 * (majorRadius * majorRadius) * (d.z * d.z);
        a1 = 2 * beta * gama + 8 * (majorRadius * majorRadius) * p.z * d.z;
        a0 = (gama * gama) + 4 * (majorRadius * majorRadius) * (p.z * p.z) - (4 * (majorRadius * majorRadius) * (minorRadius * minorRadius));

        //System.out.println(inOut_ray);
        //System.out.println("minorRadius: "+minorRadius+" majorRadius: "+majorRadius);
        //System.out.println("a4: "+a4+" a3: "+a3+" a2: "+a2+" a1: "+a1+" a0: "+a0);
        SolverPolynomialQuarticBairstow q;

        q = new SolverPolynomialQuarticBairstow(a4, a3, a2, a1, a0);

        double root[] = q.getReal();
        double rootImg[] = q.getImg();
        double mRoot = 0;
        int count = 0;

        for (int i = 0; i < 4; i++) {
            if (rootImg[i] == 0 && root[i] > 0) {
                if (count == 0) {
                    mRoot = root[i];
                    count++;
                } else if (root[i] < mRoot) {
                    mRoot = root[i];
                }
            }
        }

        if (count == 0) {
            return false;
        } 
        else {
            lastInfo.p.x = inOut_ray.origin.x + (mRoot * inOut_ray.direction.x);
            lastInfo.p.y = inOut_ray.origin.y + (mRoot * inOut_ray.direction.y);
            lastInfo.p.z = inOut_ray.origin.z + (mRoot * inOut_ray.direction.z);
            inOut_ray.t = mRoot;
            return true; //calculateRoot(a4, a3, a2,  a1, a0, inOut_ray);
        }
    }

    /*
    Unused and not working yet method. An old try to find roots from analytical
    cubic equation solution.
    @param a4
    @param a3
    @param a2
    @param a1
    @param a0
    @param ray
    @return true if current quartic equation has at least one real root
    */    
    /*
    private boolean
    calculateRoot(double a4, double a3,double a2, double a1, double a0, Ray ray)
    {
        int iRoot[];
        iRoot = new int[4];
        double Root[];
        Root = new double[4];
        
        //Root 1
        //--------------------------------------------------------------------
        double x;
        double x1;
        double x2;
        double x3;
        
        //----------------------------------------------------
        //1. Part 1 
        double p1=-a3/(4*a4);
        System.out.println("Parte 1: "+p1);
       
        
        //--------------------------------------------------------- 
        //2. Part 2
        
        //System.out.println("\nParte 2: p2_1 * Math.sqrt( p2_2  - p2_3 + p2_4 + p2_5)"); 
        
        // 2.1
        double p2_1=1/2d;
        System.out.println("p2_1: "+p2_1);
        
        // 2.2
        double p2_2=Math.pow(a3, 2)/(4 *  Math.pow(a4, 2));
        System.out.println("p2_2: "+p2_2);
        
        // 2.3
        double p2_3=(2 * a2)/(3 * a4);
        System.out.println("p2_3: "+p2_3);
       
        // 2.4
        System.out.println("p2_4= p2_4_n/p2_4_d ");
       
        // 2.4 nominador
        double p2_4_n;
        p2_4_n = Math.pow(2d, (1/3d))*(Math.pow(a2, 2) - 3 * a3  * a1 + 12 * a4 * a0);
        //System.out.println("p2_4_n: "+p2_4_n);
        
        
        // 2.4 denominador
        //System.out.println("p2_4_d=3 * a * Math.pow((  p2_4_d_1 - p2_4_d_2 + p2_4_d_3 + p2_4_d_4 - p2_4_d_5  + Math.sqrt(p2_4_d_6)),(1/3d)); ");
        
        // 2.4.1
        double p2_4_d_1= 2 * Math.pow(a2, 3);
        System.out.println("p2_4_d_1: "+p2_4_d_1);
        // 2.4.2
        double p2_4_d_2= 9 * a3 * a2 * a1;
        System.out.println("p2_4_d_2: "+p2_4_d_2);
        // 2.4.3
        double p2_4_d_3= 27 * a4 * Math.pow(a1, 2) ;
        System.out.println("p2_4_d_3: "+p2_4_d_3);
        // 2.4.4
        double p2_4_d_4= 27 * Math.pow(a3, 2) * a0;
        System.out.println("p2_4_d_4: "+p2_4_d_4);
        // 2.4.5
        double p2_4_d_5= 72 * a4 * a2 * a0;
        System.out.println("p2_4_d_5: "+p2_4_d_5);
        // 2.4.6        
        System.out.println("p2_4_d_6=p2_4_d_6_1 + p2_4_d_6_2");
        
        double p2_4_d_6_1=4 * Math.pow(
                                        (Math.pow(a2, 2) 
                                        - 3 * a3 * a1 
                                        + 12 * a4 * a0),3
                                       );
        double p2_4_d_6_2=Math.pow(
                                      (2 * Math.pow(a2, 3) 
                                       - 9 * a3 * a2 * a1 
                                       + 27 * a4 * Math.pow(a1, 2) 
                                       + 27 * Math.pow(a3, 2) * a0 
                                       - 72 * a4 * a2 * a0),2
                                     );
        
        double p2_4_d_6 = - p2_4_d_6_1 + p2_4_d_6_2;
        
        System.out.println("p2_4_d_6: "+p2_4_d_6);
        
        double p2_4_d=3 * a4 * Math.pow(
                                        (  p2_4_d_1 
                                         - p2_4_d_2 
                                         + p2_4_d_3 
                                         + p2_4_d_4 
                                         - p2_4_d_5 
                                         + Math.sqrt(p2_4_d_6)
                                        )
                                       ,(1/3d));
        
        System.out.println("p2_4_d: "+p2_4_d);
        
        double p2_4= p2_4_n/p2_4_d;
        
        // 2.5
        
        double p2_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                               -4 * Math.pow(
                                                                                               (Math.pow(a2, 2)
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                )
                                                                                                ,3
                                                                                             ) 
                                                                               + Math.pow(
                                                                                           (2 * Math.pow(a2, 3) 
                                                                                            - 9 * a3 * a2 * a1 
                                                                                            + 27 * a4 * Math.pow(a1, 2) 
                                                                                            + 27 * Math.pow(a3, 2) * a0 
                                                                                            - 72 * a4 * a2 * a0
                                                                                            ),2
                                                                                           )
                                                                               )
                                                                  )
                                                                 ,(1/3d)
                                                                 );
        
        double p2 = p2_1 * Math.sqrt( p2_2  - p2_3 + p2_4 + p2_5);
        
        System.out.println("p2: "+p2);
       
        //--------------------------------------------------------------------------------------------------------------
        //3. Part 3
        
        // 3.1
        double p3_1= 1/2d;
       
        //3.2
        double p3_2=Math.pow(a3, 2)/(2 * Math.pow(a4, 2));
        
        // 3.3
        double p3_3=(4 * a2)/(3 * a4);
        
        // 3.4
        double p3_4=( Math.pow(2, (1/3d))*(Math.pow(a2, 2) 
                                            - 3 * a3 * a1 
                                            + 12 * a4 * a0
                                           )
                    )/
                    (3 * a4 * Math.pow(
                                       (2 * Math.pow(a2, 3) 
                                        - 9 * a3 * a2 * a1 
                                        + 27 * a4 * Math.pow(a1, 2) 
                                        + 27 * Math.pow(a3, 2) * a0 
                                        - 72 * a4 * a2 * a0 
                                        + Math.sqrt(
                                                    -4 * Math.pow(
                                                                   (Math.pow(a2, 2) 
                                                                    - 3 * a3 * a1 
                                                                    + 12 * a4 * a0
                                                                    ),3
                                                                   ) 
                                                    + Math.pow(
                                                                 (2 * Math.pow(a2, 3) 
                                                                  - 9 * a3 * a2 * a1 
                                                                  + 27 * a4 * Math.pow(a1, 2) 
                                                                  + 27 * Math.pow(a3, 2) * a0 
                                                                  - 72 * a4 * a2 * a0
                                                                  ),2
                                                                 )
                                                      )
                                        ),(1/3d)
                                      )
                     );
        
        // 3.5
        double p3_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                                -4 * Math.pow(
                                                                                               (Math.pow(a2, 2) 
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                ),3
                                                                                              ) 
                                                                                + Math.pow(
                                                                                            (2 * Math.pow(a2, 3)
                                                                                             - 9 * a3 * a2 * a1 
                                                                                             + 27 * a4 * Math.pow(a1, 2) 
                                                                                             + 27 * Math.pow(a3, 2) * a0 
                                                                                             - 72 * a4 * a2 * a0
                                                                                             ),2
                                                                                            )
                                                                               )
                                                                   ),(1/3)
                                                                 );
        
        //3.6
        double p3_6=(-(Math.pow(a3, 3)/Math.pow(a4, 3)) 
                     + (4 * a3 * a2)/Math.pow(a4, 2) 
                     - (8 * a1)/a4
                     )/
                     (  4 * Math.sqrt(
                                        Math.pow(a3, 2)/(4 * Math.pow(a4, 2)) 
                                        - (2 * a2)/(3 * a4) 
                                        + (Math.pow(2, (1/3d))* (Math.pow(a2, 2) - 3 * a3 * a1 + 12 * a4 * a0))
                                           /(3 * a4 * Math.pow(
                                                               (2 * Math.pow(a2, 3) 
                                                                - 9 * a3 * a2 * a1
                                                                + 27 * a4 * Math.pow(a1, 2) 
                                                                + 27 * Math.pow(a3, 2) * a0 
                                                                - 72 * a4 * a2 * a0 
                                                                + Math.sqrt(
                                                                            -4 * Math.pow(
                                                                                           (Math.pow(a2, 2) 
                                                                                            - 3 * a3 * a1 
                                                                                            + 12 * a4 * a0
                                                                                            ),3
                                                                                           ) 
                                                                             + Math.pow(
                                                                                          (2 * Math.pow(a2, 3) 
                                                                                           - 9 * a3 * a2 * a1 
                                                                                           + 27 * a4 * Math.pow(a1, 2) 
                                                                                           + 27 * Math.pow(a3, 2) * a0 
                                                                                           - 72 * a4 * a2 * a0
                                                                                           ),2
                                                                                          )
                                                                              )
                                                               ),(1/3d)
                                                              )
                                            ) 
                                        + 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow( 
                                                                                        (2 * Math.pow(a2, 3)
                                                                                         - 9 * a3 * a2 * a1 
                                                                                         + 27 * a4 * Math.pow(a1, 2) 
                                                                                         + 27 * Math.pow(a3, 2) * a0 
                                                                                         - 72 * a4 * a2 * a0 
                                                                                         + Math.sqrt(
                                                                                                      -4 * Math.pow(
                                                                                                                     (Math.pow(a2, 2) 
                                                                                                                      - 3 * a3 * a1 
                                                                                                                      + 12 * a4 * a0
                                                                                                                      ),3
                                                                                                                     ) 
                                                                                                       + Math.pow(
                                                                                                                     (2 * Math.pow(a2, 3) 
                                                                                                                      - 9 * a3 * a2 * a1 
                                                                                                                      + 27 * a4 * Math.pow(a1, 2) 
                                                                                                                      + 27 * Math.pow(a3, 2) * a0 
                                                                                                                      - 72 * a4 * a2 * a0
                                                                                                                      ),2
                                                                                                                   )
                                                                                                      )
                                                                                         ),(1/3d)
                                                                                     )
                                      )
                      );
        
        double p3= p3_1 * Math.sqrt(p3_2 - p3_3 - p3_4 - p3_5 - p3_6);
        
   //     System.out.println("p3_1: "+p3_1+"\np3_2: "+p3_2+" \np3_3: "+p3_3+" \np3_4: "+p3_4+" \np3_5: "+p3_5+" \np3_6: "+p3_6);
       
        
          x=p1-p2-p3;
   //     System.out.println("P1: "+p1+" P2: "+p2+" P3: "+p3+" \nX:"+x);
          System.out.println("\nX:"+x);
          
          if(Double.isNaN(x))
          {
              iRoot[0]=0;
              Root[0]=0;
          }
          else
          {
              iRoot[0]=1;
              Root[0]=x;
          }
        
        //Root 2
        //------------------------------------------------------------------------
        //----------------------------------------------------
        //----------------------------------------------------
        //1. Part 1 
        p1=-a3/(4*a4);
        //System.out.println("Parte 1: "+p1);

        //--------------------------------------------------------- 
        //2. Part 2        
        //System.out.println("\nParte 2: p2_1 * Math.sqrt( p2_2  - p2_3 + p2_4 + p2_5)"); 
        
        // 2.1        
        p2_1=1/2d;
        //System.out.println("p2_1: "+p2_1);
        
        // 2.2
        
        p2_2=Math.pow(a3, 2)/(4 *  Math.pow(a4, 2));
        //System.out.println("p2_2: "+p2_2);
        
        // 2.3
        
        p2_3=(2 * a2)/(3 * a4);
        //System.out.println("p2_3: "+p2_3);
       
        // 2.4
        //System.out.println("p2_4= p2_4_n/p2_4_d ");
       
        // 2.4 nominador
        p2_4_n=Math.pow(2d, (1/3d))*(Math.pow(a2, 2) - 3 * a3  * a1 + 12 * a4 * a0);
        //System.out.println("p2_4_n: "+p2_4_n);
        
        // 2.4 denominador
        //System.out.println("p2_4_d=3 * a * Math.pow((  p2_4_d_1 - p2_4_d_2 + p2_4_d_3 + p2_4_d_4 - p2_4_d_5  + Math.sqrt(p2_4_d_6)),(1/3d)); ");
        
        // 2.4.1
        p2_4_d_1= 2 * Math.pow(a2, 3);
        //System.out.println("p2_4_d_1: "+p2_4_d_1);
        // 2.4.2
        p2_4_d_2= 9 * a3 * a2 * a1;
        //System.out.println("p2_4_d_2: "+p2_4_d_2);
        // 2.4.3
        p2_4_d_3= 27 * a4 * Math.pow(a1, 2) ;
        //System.out.println("p2_4_d_3: "+p2_4_d_3);
        // 2.4.4
        p2_4_d_4= 27 * Math.pow(a3, 2) * a0;
        //System.out.println("p2_4_d_4: "+p2_4_d_4);
        // 2.4.5
        p2_4_d_5= 72 * a4 * a2 * a0;
        //System.out.println("p2_4_d_5: "+p2_4_d_5);
        // 2.4.6        
        //System.out.println("p2_4_d_6=p2_4_d_6_1 + p2_4_d_6_2");
        
        p2_4_d_6_1=4 * Math.pow(
                                        (Math.pow(a2, 2) 
                                        - 3 * a3 * a1 
                                        + 12 * a4 * a0),3
                                       );
        p2_4_d_6_2=Math.pow(
                                      (2 * Math.pow(a2, 3) 
                                       - 9 * a3 * a2 * a1 
                                       + 27 * a4 * Math.pow(a1, 2) 
                                       + 27 * Math.pow(a3, 2) * a0 
                                       - 72 * a4 * a2 * a0),2
                                     );
        
        p2_4_d_6 = - p2_4_d_6_1 + p2_4_d_6_2;
        
        //System.out.println("p2_4_d_6: "+p2_4_d_6);
        
        p2_4_d=3 * a4 * Math.pow(
                                        (  p2_4_d_1 
                                         - p2_4_d_2 
                                         + p2_4_d_3 
                                         + p2_4_d_4 
                                         - p2_4_d_5 
                                         + Math.sqrt(p2_4_d_6)
                                        )
                                       ,(1/3d));
        
        //System.out.println(p2_4_d);
        
        p2_4= p2_4_n/p2_4_d;
        
        // 2.5
        
        p2_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                               -4 * Math.pow(
                                                                                               (Math.pow(a2, 2)
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                )
                                                                                                ,3
                                                                                             ) 
                                                                               + Math.pow(
                                                                                           (2 * Math.pow(a2, 3) 
                                                                                            - 9 * a3 * a2 * a1 
                                                                                            + 27 * a4 * Math.pow(a1, 2) 
                                                                                            + 27 * Math.pow(a3, 2) * a0 
                                                                                            - 72 * a4 * a2 * a0
                                                                                            ),2
                                                                                           )
                                                                               )
                                                                  )
                                                                 ,(1/3d)
                                                                 );
        
        p2 = p2_1 * Math.sqrt( p2_2  - p2_3 + p2_4 + p2_5);
        
        
       
        //--------------------------------------------------------------------------------------------------------------
        //3. Part 3
        
        // 3.1
        p3_1= 1/2d;
       
        //3.2
        p3_2=Math.pow(a3, 2)/(2 * Math.pow(a4, 2));
        
        // 3.3
        p3_3=(4 * a2)/(3 * a4);
        
        // 3.4
        p3_4=( Math.pow(2, (1/3d))*(Math.pow(a2, 2) 
                                            - 3 * a3 * a1 
                                            + 12 * a4 * a0
                                           )
                    )/
                    (3 * a4 * Math.pow(
                                       (2 * Math.pow(a2, 3) 
                                        - 9 * a3 * a2 * a1 
                                        + 27 * a4 * Math.pow(a1, 2) 
                                        + 27 * Math.pow(a3, 2) * a0 
                                        - 72 * a4 * a2 * a0 
                                        + Math.sqrt(
                                                    -4 * Math.pow(
                                                                   (Math.pow(a2, 2) 
                                                                    - 3 * a3 * a1 
                                                                    + 12 * a4 * a0
                                                                    ),3
                                                                   ) 
                                                    + Math.pow(
                                                                 (2 * Math.pow(a2, 3) 
                                                                  - 9 * a3 * a2 * a1 
                                                                  + 27 * a4 * Math.pow(a1, 2) 
                                                                  + 27 * Math.pow(a3, 2) * a0 
                                                                  - 72 * a4 * a2 * a0
                                                                  ),2
                                                                 )
                                                      )
                                        ),(1/3d)
                                      )
                     );
        
        // 3.5
        p3_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                                -4 * Math.pow(
                                                                                               (Math.pow(a2, 2) 
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                ),3
                                                                                              ) 
                                                                                + Math.pow(
                                                                                            (2 * Math.pow(a2, 3)
                                                                                             - 9 * a3 * a2 * a1 
                                                                                             + 27 * a4 * Math.pow(a1, 2) 
                                                                                             + 27 * Math.pow(a3, 2) * a0 
                                                                                             - 72 * a4 * a2 * a0
                                                                                             ),2
                                                                                            )
                                                                               )
                                                                   ),(1/3)
                                                                 );
        
        //3.6
        p3_6=(-(Math.pow(a3, 3)/Math.pow(a4, 3)) 
                     + (4 * a3 * a2)/Math.pow(a4, 2) 
                     - (8 * a1)/a4
                     )/
                     (  4 * Math.sqrt(
                                        Math.pow(a3, 2)/(4 * Math.pow(a4, 2)) 
                                        - (2 * a2)/(3 * a4) 
                                        + (Math.pow(2, (1/3d))* (Math.pow(a2, 2) - 3 * a3 * a1 + 12 * a4 * a0))
                                           /(3 * a4 * Math.pow(
                                                               (2 * Math.pow(a2, 3) 
                                                                - 9 * a3 * a2 * a1
                                                                + 27 * a4 * Math.pow(a1, 2) 
                                                                + 27 * Math.pow(a3, 2) * a0 
                                                                - 72 * a4 * a2 * a0 
                                                                + Math.sqrt(
                                                                            -4 * Math.pow(
                                                                                           (Math.pow(a2, 2) 
                                                                                            - 3 * a3 * a1 
                                                                                            + 12 * a4 * a0
                                                                                            ),3
                                                                                           ) 
                                                                             + Math.pow(
                                                                                          (2 * Math.pow(a2, 3) 
                                                                                           - 9 * a3 * a2 * a1 
                                                                                           + 27 * a4 * Math.pow(a1, 2) 
                                                                                           + 27 * Math.pow(a3, 2) * a0 
                                                                                           - 72 * a4 * a2 * a0
                                                                                           ),2
                                                                                          )
                                                                              )
                                                               ),(1/3d)
                                                              )
                                            ) 
                                        + 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow( 
                                                                                        (2 * Math.pow(a2, 3)
                                                                                         - 9 * a3 * a2 * a1 
                                                                                         + 27 * a4 * Math.pow(a1, 2) 
                                                                                         + 27 * Math.pow(a3, 2) * a0 
                                                                                         - 72 * a4 * a2 * a0 
                                                                                         + Math.sqrt(
                                                                                                      -4 * Math.pow(
                                                                                                                     (Math.pow(a2, 2) 
                                                                                                                      - 3 * a3 * a1 
                                                                                                                      + 12 * a4 * a0
                                                                                                                      ),3
                                                                                                                     ) 
                                                                                                       + Math.pow(
                                                                                                                     (2 * Math.pow(a2, 3) 
                                                                                                                      - 9 * a3 * a2 * a1 
                                                                                                                      + 27 * a4 * Math.pow(a1, 2) 
                                                                                                                      + 27 * Math.pow(a3, 2) * a0 
                                                                                                                      - 72 * a4 * a2 * a0
                                                                                                                      ),2
                                                                                                                   )
                                                                                                      )
                                                                                         ),(1/3d)
                                                                                     )
                                      )
                      );
        
        p3= p3_1 * Math.sqrt(p3_2 - p3_3 - p3_4 - p3_5 - p3_6);
        
        //System.out.println("p3_1: "+p3_1+"\np3_2: "+p3_2+" \np3_3: "+p3_3+" \np3_4: "+p3_4+" \np3_5: "+p3_5+" \np3_6: "+p3_6);
        
        x1=p1+p2-p3;
        //System.out.println("P1: "+p1+" P2: "+p2+" P3: "+p3+" \nX1:"+x1);
        
        //System.out.println("X1: "+x1);
       
        if( Double.isNaN(x1) ) {
            iRoot[1] = 0;
            Root[1] = 0;
        } 
        else {
            iRoot[1] = 1;
            Root[1] = x1;
        }
        
        // Root 3
        //----------------------------------------------------------------------
        
        //1. Part 1 
        p1=-a3/(4*a4);
        //        System.out.println("Parte 1: "+p1);
       
        
        //--------------------------------------------------------- 
       //2. Part 2
        
//       System.out.println("\nParte 2: p2_1 * Math.sqrt( p2_2  - p2_3 + p2_4 + p2_5)"); 
        
        // 2.1
        
        p2_1=1/2d;
 //       System.out.println("p2_1: "+p2_1);
        
        // 2.2
        
        p2_2=Math.pow(a3, 2)/(4 *  Math.pow(a4, 2));
//        System.out.println("p2_2: "+p2_2);
        
        // 2.3
        
        p2_3=(2 * a2)/(3 * a4);
//        System.out.println("p2_3: "+p2_3);
       
        // 2.4
 //       System.out.println("p2_4= p2_4_n/p2_4_d ");
       
        // 2.4 nominador
        p2_4_n=Math.pow(2d, (1/3d))*(Math.pow(a2, 2) - 3 * a3  * a1 + 12 * a4 * a0);
 //       System.out.println("p2_4_n: "+p2_4_n);
        
        
        // 2.4 denominador
 //       System.out.println("p2_4_d=3 * a * Math.pow((  p2_4_d_1 - p2_4_d_2 + p2_4_d_3 + p2_4_d_4 - p2_4_d_5  + Math.sqrt(p2_4_d_6)),(1/3d)); ");
        
        // 2.4.1
        p2_4_d_1= 2 * Math.pow(a2, 3);
 //       System.out.println("p2_4_d_1: "+p2_4_d_1);
        // 2.4.2
        p2_4_d_2= 9 * a3 * a2 * a1;
 //       System.out.println("p2_4_d_2: "+p2_4_d_2);
        // 2.4.3
        p2_4_d_3= 27 * a4 * Math.pow(a1, 2) ;
 //       System.out.println("p2_4_d_3: "+p2_4_d_3);
        // 2.4.4
        p2_4_d_4= 27 * Math.pow(a3, 2) * a0;
 //       System.out.println("p2_4_d_4: "+p2_4_d_4);
        // 2.4.5
        p2_4_d_5= 72 * a4 * a2 * a0;
 //       System.out.println("p2_4_d_5: "+p2_4_d_5);
        // 2.4.6        
 //       System.out.println("p2_4_d_6=p2_4_d_6_1 + p2_4_d_6_2");
        
        p2_4_d_6_1=4 * Math.pow(
                                        (Math.pow(a2, 2) 
                                        - 3 * a3 * a1 
                                        + 12 * a4 * a0),3
                                       );
         p2_4_d_6_2=Math.pow(
                                      (2 * Math.pow(a2, 3) 
                                       - 9 * a3 * a2 * a1 
                                       + 27 * a4 * Math.pow(a1, 2) 
                                       + 27 * Math.pow(a3, 2) * a0 
                                       - 72 * a4 * a2 * a0),2
                                     );
        
        p2_4_d_6 = - p2_4_d_6_1 + p2_4_d_6_2;
        
//        System.out.println("p2_4_d_6: "+p2_4_d_6);
        
        p2_4_d=3 * a4 * Math.pow(
                                        (  p2_4_d_1 
                                         - p2_4_d_2 
                                         + p2_4_d_3 
                                         + p2_4_d_4 
                                         - p2_4_d_5 
                                         + Math.sqrt(p2_4_d_6)
                                        )
                                       ,(1/3d));
        
 //       System.out.println(p2_4_d);
        
        p2_4= p2_4_n/p2_4_d;
        
        // 2.5
        
        p2_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                               -4 * Math.pow(
                                                                                               (Math.pow(a2, 2)
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                )
                                                                                                ,3
                                                                                             ) 
                                                                               + Math.pow(
                                                                                           (2 * Math.pow(a2, 3) 
                                                                                            - 9 * a3 * a2 * a1 
                                                                                            + 27 * a4 * Math.pow(a1, 2) 
                                                                                            + 27 * Math.pow(a3, 2) * a0 
                                                                                            - 72 * a4 * a2 * a0
                                                                                            ),2
                                                                                           )
                                                                               )
                                                                  )
                                                                 ,(1/3d)
                                                                 );
        
        p2 = p2_1 * Math.sqrt( p2_2  + p2_3 + p2_4 + p2_5);
        
        
       
        //--------------------------------------------------------------------------------------------------------------
        //3. Part 3
        
        // 3.1
        p3_1= 1/2d;
       
        //3.2
        p3_2=Math.pow(a3, 2)/(2 * Math.pow(a4, 2));
        
        // 3.3
        p3_3=(4 * a2)/(3 * a4);
        
        // 3.4
        p3_4=( Math.pow(2, (1/3d))*(Math.pow(a2, 2) 
                                            - 3 * a3 * a1 
                                            + 12 * a4 * a0
                                           )
                    )/
                    (3 * a4 * Math.pow(
                                       (2 * Math.pow(a2, 3) 
                                        - 9 * a3 * a2 * a1 
                                        + 27 * a4 * Math.pow(a1, 2) 
                                        + 27 * Math.pow(a3, 2) * a0 
                                        - 72 * a4 * a2 * a0 
                                        + Math.sqrt(
                                                    -4 * Math.pow(
                                                                   (Math.pow(a2, 2) 
                                                                    - 3 * a3 * a1 
                                                                    + 12 * a4 * a0
                                                                    ),3
                                                                   ) 
                                                    + Math.pow(
                                                                 (2 * Math.pow(a2, 3) 
                                                                  - 9 * a3 * a2 * a1 
                                                                  + 27 * a4 * Math.pow(a1, 2) 
                                                                  + 27 * Math.pow(a3, 2) * a0 
                                                                  - 72 * a4 * a2 * a0
                                                                  ),2
                                                                 )
                                                      )
                                        ),(1/3d)
                                      )
                     );
        
        // 3.5
        p3_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                                -4 * Math.pow(
                                                                                               (Math.pow(a2, 2) 
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                ),3
                                                                                              ) 
                                                                                + Math.pow(
                                                                                            (2 * Math.pow(a2, 3)
                                                                                             - 9 * a3 * a2 * a1 
                                                                                             + 27 * a4 * Math.pow(a1, 2) 
                                                                                             + 27 * Math.pow(a3, 2) * a0 
                                                                                             - 72 * a4 * a2 * a0
                                                                                             ),2
                                                                                            )
                                                                               )
                                                                   ),(1/3)
                                                                 );
        
        //3.6
        p3_6=(-(Math.pow(a3, 3)/Math.pow(a4, 3)) 
                     + (4 * a3 * a2)/Math.pow(a4, 2) 
                     - (8 * a1)/a4
                     )/
                     (  4 * Math.sqrt(
                                        Math.pow(a3, 2)/(4 * Math.pow(a4, 2)) 
                                        - (2 * a2)/(3 * a4) 
                                        + (Math.pow(2, (1/3d))* (Math.pow(a2, 2) - 3 * a3 * a1 + 12 * a4 * a0))
                                           /(3 * a4 * Math.pow(
                                                               (2 * Math.pow(a2, 3) 
                                                                - 9 * a3 * a2 * a1
                                                                + 27 * a4 * Math.pow(a1, 2) 
                                                                + 27 * Math.pow(a3, 2) * a0 
                                                                - 72 * a4 * a2 * a0 
                                                                + Math.sqrt(
                                                                            -4 * Math.pow(
                                                                                           (Math.pow(a2, 2) 
                                                                                            - 3 * a3 * a1 
                                                                                            + 12 * a4 * a0
                                                                                            ),3
                                                                                           ) 
                                                                             + Math.pow(
                                                                                          (2 * Math.pow(a2, 3) 
                                                                                           - 9 * a3 * a2 * a1 
                                                                                           + 27 * a4 * Math.pow(a1, 2) 
                                                                                           + 27 * Math.pow(a3, 2) * a0 
                                                                                           - 72 * a4 * a2 * a0
                                                                                           ),2
                                                                                          )
                                                                              )
                                                               ),(1/3d)
                                                              )
                                            ) 
                                        + 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow( 
                                                                                        (2 * Math.pow(a2, 3)
                                                                                         - 9 * a3 * a2 * a1 
                                                                                         + 27 * a4 * Math.pow(a1, 2) 
                                                                                         + 27 * Math.pow(a3, 2) * a0 
                                                                                         - 72 * a4 * a2 * a0 
                                                                                         + Math.sqrt(
                                                                                                      -4 * Math.pow(
                                                                                                                     (Math.pow(a2, 2) 
                                                                                                                      - 3 * a3 * a1 
                                                                                                                      + 12 * a4 * a0
                                                                                                                      ),3
                                                                                                                     ) 
                                                                                                       + Math.pow(
                                                                                                                     (2 * Math.pow(a2, 3) 
                                                                                                                      - 9 * a3 * a2 * a1 
                                                                                                                      + 27 * a4 * Math.pow(a1, 2) 
                                                                                                                      + 27 * Math.pow(a3, 2) * a0 
                                                                                                                      - 72 * a4 * a2 * a0
                                                                                                                      ),2
                                                                                                                   )
                                                                                                      )
                                                                                         ),(1/3d)
                                                                                     )
                                      )
                      );
        
        p3= p3_1 * Math.sqrt(p3_2 - p3_3 - p3_4 - p3_5 + p3_6);
        
//        System.out.println("p3_1: "+p3_1+"\np3_2: "+p3_2+" \np3_3: "+p3_3+" \np3_4: "+p3_4+" \np3_5: "+p3_5+" \np3_6: "+p3_6);
        
        x2 = p1 + p2 - p3;
     //   System.out.println("P1: "+p1+" P2: "+p2+" P3: "+p3+" \nX2:"+x2);
        System.out.println("X2: "+x2);
        
        if(Double.isNaN(x2))
          {
              iRoot[2]=0;
              Root[2]=0;
          }
          else
          {
              iRoot[2]=1;
              Root[2]=x2;
          }
              
        // Root 4
        //----------------------------------------------------------------------
        
       //1. Part 1 
        p1=-a3/(4*a4);
 //       System.out.println("Parte 1: "+p1);
       
        
        //--------------------------------------------------------- 
       //2. Part 2
        
  //     System.out.println("\nParte 2: p2_1 * Math.sqrt( p2_2  - p2_3 + p2_4 + p2_5)"); 
        
        // 2.1
        
        p2_1=1/2d;
 //       System.out.println("p2_1: "+p2_1);
        
        // 2.2
        
        p2_2=Math.pow(a3, 2)/(4 *  Math.pow(a4, 2));
 //       System.out.println("p2_2: "+p2_2);
        
        // 2.3
        
        p2_3=(2 * a2)/(3 * a4);
 //       System.out.println("p2_3: "+p2_3);
       
        // 2.4
//        System.out.println("p2_4= p2_4_n/p2_4_d ");
       
        // 2.4 nominador
        p2_4_n=Math.pow(2d, (1/3d))*(Math.pow(a2, 2) - 3 * a3  * a1 + 12 * a4 * a0);
//        System.out.println("p2_4_n: "+p2_4_n);
        
        
        // 2.4 denominador
 //       System.out.println("p2_4_d=3 * a * Math.pow((  p2_4_d_1 - p2_4_d_2 + p2_4_d_3 + p2_4_d_4 - p2_4_d_5  + Math.sqrt(p2_4_d_6)),(1/3d)); ");
        
        // 2.4.1
        p2_4_d_1= 2 * Math.pow(a2, 3);
 //       System.out.println("p2_4_d_1: "+p2_4_d_1);
        // 2.4.2
        p2_4_d_2= 9 * a3 * a2 * a1;
 //       System.out.println("p2_4_d_2: "+p2_4_d_2);
        // 2.4.3
        p2_4_d_3= 27 * a4 * Math.pow(a1, 2) ;
 //       System.out.println("p2_4_d_3: "+p2_4_d_3);
        // 2.4.4
        p2_4_d_4= 27 * Math.pow(a3, 2) * a0;
 //       System.out.println("p2_4_d_4: "+p2_4_d_4);
        // 2.4.5
        p2_4_d_5= 72 * a4 * a2 * a0;
 //       System.out.println("p2_4_d_5: "+p2_4_d_5);
        // 2.4.6        
 //       System.out.println("p2_4_d_6=p2_4_d_6_1 + p2_4_d_6_2");
        
        p2_4_d_6_1=4 * Math.pow(
                                        (Math.pow(a2, 2) 
                                        - 3 * a3 * a1 
                                        + 12 * a4 * a0),3
                                       );
         p2_4_d_6_2=Math.pow(
                                      (2 * Math.pow(a2, 3) 
                                       - 9 * a3 * a2 * a1 
                                       + 27 * a4 * Math.pow(a1, 2) 
                                       + 27 * Math.pow(a3, 2) * a0 
                                       - 72 * a4 * a2 * a0),2
                                     );
        
        p2_4_d_6 = - p2_4_d_6_1 + p2_4_d_6_2;
        
//        System.out.println("p2_4_d_6: "+p2_4_d_6);
        
        p2_4_d=3 * a4 * Math.pow(
                                        (  p2_4_d_1 
                                         - p2_4_d_2 
                                         + p2_4_d_3 
                                         + p2_4_d_4 
                                         - p2_4_d_5 
                                         + Math.sqrt(p2_4_d_6)
                                        )
                                       ,(1/3d));
        
 //       System.out.println(p2_4_d);
        
        p2_4= p2_4_n/p2_4_d;
        
        // 2.5
        
        p2_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                               -4 * Math.pow(
                                                                                               (Math.pow(a2, 2)
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                )
                                                                                                ,3
                                                                                             ) 
                                                                               + Math.pow(
                                                                                           (2 * Math.pow(a2, 3) 
                                                                                            - 9 * a3 * a2 * a1 
                                                                                            + 27 * a4 * Math.pow(a1, 2) 
                                                                                            + 27 * Math.pow(a3, 2) * a0 
                                                                                            - 72 * a4 * a2 * a0
                                                                                            ),2
                                                                                           )
                                                                               )
                                                                  )
                                                                 ,(1/3d)
                                                                 );
        
        p2 = p2_1 * Math.sqrt( p2_2  + p2_3 + p2_4 + p2_5);
        
        
       
        //--------------------------------------------------------------------------------------------------------------
        //3. Part 3
        
        // 3.1
        p3_1= 1/2d;
       
        //3.2
        p3_2=Math.pow(a3, 2)/(2 * Math.pow(a4, 2));
        
        // 3.3
        p3_3=(4 * a2)/(3 * a4);
        
        // 3.4
        p3_4=( Math.pow(2, (1/3d))*(Math.pow(a2, 2) 
                                            - 3 * a3 * a1 
                                            + 12 * a4 * a0
                                           )
                    )/
                    (3 * a4 * Math.pow(
                                       (2 * Math.pow(a2, 3) 
                                        - 9 * a3 * a2 * a1 
                                        + 27 * a4 * Math.pow(a1, 2) 
                                        + 27 * Math.pow(a3, 2) * a0 
                                        - 72 * a4 * a2 * a0 
                                        + Math.sqrt(
                                                    -4 * Math.pow(
                                                                   (Math.pow(a2, 2) 
                                                                    - 3 * a3 * a1 
                                                                    + 12 * a4 * a0
                                                                    ),3
                                                                   ) 
                                                    + Math.pow(
                                                                 (2 * Math.pow(a2, 3) 
                                                                  - 9 * a3 * a2 * a1 
                                                                  + 27 * a4 * Math.pow(a1, 2) 
                                                                  + 27 * Math.pow(a3, 2) * a0 
                                                                  - 72 * a4 * a2 * a0
                                                                  ),2
                                                                 )
                                                      )
                                        ),(1/3d)
                                      )
                     );
        
        // 3.5
        p3_5= 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow(
                                                                  (2 * Math.pow(a2, 3) 
                                                                   - 9 * a3 * a2 * a1 
                                                                   + 27 * a4 * Math.pow(a1, 2) 
                                                                   + 27 * Math.pow(a3, 2) * a0 
                                                                   - 72 * a4 * a2 * a0 
                                                                   + Math.sqrt(
                                                                                -4 * Math.pow(
                                                                                               (Math.pow(a2, 2) 
                                                                                                - 3 * a3 * a1 
                                                                                                + 12 * a4 * a0
                                                                                                ),3
                                                                                              ) 
                                                                                + Math.pow(
                                                                                            (2 * Math.pow(a2, 3)
                                                                                             - 9 * a3 * a2 * a1 
                                                                                             + 27 * a4 * Math.pow(a1, 2) 
                                                                                             + 27 * Math.pow(a3, 2) * a0 
                                                                                             - 72 * a4 * a2 * a0
                                                                                             ),2
                                                                                            )
                                                                               )
                                                                   ),(1/3)
                                                                 );
        
        //3.6
        p3_6=(-(Math.pow(a3, 3)/Math.pow(a4, 3)) 
                     + (4 * a3 * a2)/Math.pow(a4, 2) 
                     - (8 * a1)/a4
                     )/
                     (  4 * Math.sqrt(
                                        Math.pow(a3, 2)/(4 * Math.pow(a4, 2)) 
                                        - (2 * a2)/(3 * a4) 
                                        + (Math.pow(2, (1/3d))* (Math.pow(a2, 2) - 3 * a3 * a1 + 12 * a4 * a0))
                                           /(3 * a4 * Math.pow(
                                                               (2 * Math.pow(a2, 3) 
                                                                - 9 * a3 * a2 * a1
                                                                + 27 * a4 * Math.pow(a1, 2) 
                                                                + 27 * Math.pow(a3, 2) * a0 
                                                                - 72 * a4 * a2 * a0 
                                                                + Math.sqrt(
                                                                            -4 * Math.pow(
                                                                                           (Math.pow(a2, 2) 
                                                                                            - 3 * a3 * a1 
                                                                                            + 12 * a4 * a0
                                                                                            ),3
                                                                                           ) 
                                                                             + Math.pow(
                                                                                          (2 * Math.pow(a2, 3) 
                                                                                           - 9 * a3 * a2 * a1 
                                                                                           + 27 * a4 * Math.pow(a1, 2) 
                                                                                           + 27 * Math.pow(a3, 2) * a0 
                                                                                           - 72 * a4 * a2 * a0
                                                                                           ),2
                                                                                          )
                                                                              )
                                                               ),(1/3d)
                                                              )
                                            ) 
                                        + 1/(3 * Math.pow(2, (1/3d)) * a4) * Math.pow( 
                                                                                        (2 * Math.pow(a2, 3)
                                                                                         - 9 * a3 * a2 * a1 
                                                                                         + 27 * a4 * Math.pow(a1, 2) 
                                                                                         + 27 * Math.pow(a3, 2) * a0 
                                                                                         - 72 * a4 * a2 * a0 
                                                                                         + Math.sqrt(
                                                                                                      -4 * Math.pow(
                                                                                                                     (Math.pow(a2, 2) 
                                                                                                                      - 3 * a3 * a1 
                                                                                                                      + 12 * a4 * a0
                                                                                                                      ),3
                                                                                                                     ) 
                                                                                                       + Math.pow(
                                                                                                                     (2 * Math.pow(a2, 3) 
                                                                                                                      - 9 * a3 * a2 * a1 
                                                                                                                      + 27 * a4 * Math.pow(a1, 2) 
                                                                                                                      + 27 * Math.pow(a3, 2) * a0 
                                                                                                                      - 72 * a4 * a2 * a0
                                                                                                                      ),2
                                                                                                                   )
                                                                                                      )
                                                                                         ),(1/3d)
                                                                                     )
                                      )
                      );
        
        p3= p3_1 * Math.sqrt(p3_2 - p3_3 - p3_4 - p3_5 + p3_6);
        
 //       System.out.println("p3_1: "+p3_1+"\np3_2: "+p3_2+" \np3_3: "+p3_3+" \np3_4: "+p3_4+" \np3_5: "+p3_5+" \np3_6: "+p3_6);
        
        x3 = p1 + p2 + p3;
        //System.out.println("P1: "+p1+" P2: "+p2+" P3: "+p3+" \nX3:"+x3);
        System.out.println("X3: "+x3);
        
        if ( Double.isNaN(x3) ) {
              iRoot[3]=0;
              Root[3]=0;
          }
          else {
              iRoot[3]=1;
              Root[3]=x3;
        }
        
        // Revisa de las raices encontradas cual está más cerca al punto del rayo
        
        double root = 0;
        int cont = 0;
        
        for ( int i = 0; i < 4; i++ ) {
            if ( iRoot[i] == 0 ) {
            }
            else {
                if ( cont == 0 ) {
                    root = Root[i];
                    cont++;
                }
                else {
                    if ( root < Root[i] ) {
                    }
                    else {
                        root = Root[i];
		    }
                }
                
                
            }
        }

        if ( cont == 0 ) {
            return false;
        }
        else {
                lastInfo.p.x = ray.origin.x + (root * ray.direction.x);
                lastInfo.p.y = ray.origin.y + (root * ray.direction.y);
                lastInfo.p.z = ray.origin.z + (root * ray.direction.z);
                
                ray.t=root;
         //       System.out.println("Raiz: "+root);
                return true;
        }
    }
    */
    
    @Override
    public void doExtraInformation(
        Ray inRay, double intT, GeometryIntersectionInformation outData) 
    {
        outData.p = lastInfo.p;
        double r2=minorRadius*minorRadius;
        double R2=majorRadius*majorRadius;
     
        outData.n.x = (4*lastInfo.p.x*(Math.pow(lastInfo.p.x, 2) + Math.pow(lastInfo.p.y, 2) + Math.pow(lastInfo.p.z, 2) - r2 - R2) );
        outData.n.y = (4*lastInfo.p.y*(Math.pow(lastInfo.p.x, 2) + Math.pow(lastInfo.p.y, 2) + Math.pow(lastInfo.p.z, 2) - r2 - R2) );
        outData.n.z = (4*lastInfo.p.z*(Math.pow(lastInfo.p.x, 2) + Math.pow(lastInfo.p.y, 2) + Math.pow(lastInfo.p.z, 2) - r2 - R2) + 8*R2*lastInfo.p.z);

        outData.n.normalize();
    }

    /**
    @return a new 6 valued double array containing the coordinates of a min-max
    bounding box for current geometry.
    */
    @Override
    public double[] getMinMax() {
        double [] minmax = new double[6];
        
        minmax[0] = -(majorRadius + minorRadius);
        minmax[1] = -(majorRadius+minorRadius);
        minmax[2] = minorRadius;
        minmax[3] = majorRadius + minorRadius;
        minmax[4] = majorRadius + minorRadius;
        minmax[5] = -minorRadius;

        return minmax;
    }    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
