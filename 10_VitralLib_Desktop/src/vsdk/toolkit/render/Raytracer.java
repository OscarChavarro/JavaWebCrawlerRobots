//===========================================================================
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [BLIN1978b] Blinn, James F. "Simulation of wrinkled surfaces", SIGGRAPH =
//=          proceedings, 1978.                                             =
//= [FOLE1992] Foley, vanDam, Feiner, Hughes. "Computer Graphics,           =
//=          principles and practice" - second edition, Addison Wesley,     =
//=          1992.                                                          =
//= [WHIT1980] Whitted, Turner. "An Improved Illumination Model for Shaded  =
//=            Display", 1980.                                              =
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2005 - Oscar Chavarro: Original base version                 =
//= - February 13 2006 - Oscar Chavarro: updated raytracing code to manage  =
//=   rayable objects, with geometries implementing intersection operation  =
//=   in an object-coordinate basis.                                        =
//= - May 16 2006 - Alfonso Barbosa: modify to manage ZBuffers              =
//= - November 1 2006 - Alfonso Barbosa / Diana Reyes: execute generalized  =
//=   for inclusion of sub-viewport spec.                                   =
//= - November 19 2006 - Oscar Chavarro: material handling supporting       =
//=   submaterials inside geometry.                                         =
//= - December 20 2006 - Oscar Chavarro: bump mapping added                 =
//===========================================================================

package vsdk.toolkit.render;

import java.util.ArrayList;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.RendererConfiguration;
import vsdk.toolkit.media.RGBImage;
import vsdk.toolkit.media.ZBuffer;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.geometry.GeometryIntersectionInformation;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.gui.ProgressMonitor;

/**
This class provides an encaptulation for a rendering algorithm, 
implementing simple recursive raytracing as presented in [WHIT1980].
Includes a normal perturbation for the simulation of wrinkled surfaces,
as described in [BLIN1978b].
This class is appropiate to play a role of "concrete strategy" in
a "Strategy" design pattern.

\todo  Upgrade ArrayList management to Java 1.5 code style (typed templates)
*/
public class Raytracer extends RenderingElement {
    private Vector3D static_tmp;
    private Vector3D static_poffset;
    private Ray static_shadowRay;
    private GeometryIntersectionInformation static_info;
    private static final double TINY = 0.0001;

    public Raytracer()
    {
        // Dark magic similar to the one described Sphere::intersect
        static_tmp = new Vector3D();
        static_poffset = new Vector3D();
        static_shadowRay = new Ray();
        static_info = new GeometryIntersectionInformation();
    }

    /*
    @param info.p the point of intersection
    @param info.n unit-length surface normal
    @param viewVector unit-length vector towards the ray's origin

    Note: The info datastructure must contain point and normal in world
    coordinates.

    Warning: This method includes the use of the ray transformation technique
    that permits the representation of geometries centered in its origin,
    and its combination with geometric transformations. (This must be taken
    into account in the reflection and refraction calculations)

    \todo  Check the inconsistent use of tangent vector in bump mapping
    calculation... it is non sense to always be <0, 1, 0>.
    */
    private void evaluateIlluminationModel(
        GeometryIntersectionInformation info, Vector3D viewVector, 
        ArrayList <Light> lights, ArrayList <SimpleBody> objects,
        Background background,
        Material material, RendererConfiguration inQualitySelection,
        int recursions, ColorRgb outColor) {
        //-----------------------------------------------------------------
        SimpleBody nearestObject;
        ColorRgb backgroundColor = background.colorInDireccion(info.n);
        ColorRgb ambient;
        ColorRgb diffuse;
        ColorRgb specular;
        ColorRgb lightEmission;

        //- Normal perturbation / bump mapping ----------------------------
        // This code follows the variable name convention used on equation
        // [FOLE1992].16.23, section [FOLE1992].16.3.3.
        //-----------------------------------------------------------------
        if ( info.normalMap != null ) {
            // Information inherent to current geometry
            Vector3D N;                      // Normal vector on surface
            Vector3D Ps;                     // Tangent vector on surface
            Vector3D Pt;                     // Binormal vector on surface
            // Information extracted from precomputed normal map (after F)
            Vector3D normalVariation;        // Normal variation for point
                                             // at texture coordinates (u, v)
            double Bu;                       // dF/du for bumpmap F
            double Bv;                       // dF/dv for bumpmap F
            // Auxiliary variables
            Vector3D normalPerturbation;
            Vector3D NxPt;
            Vector3D NxPs;

            normalVariation = info.normalMap.getNormal(info.u, 1-info.v);
            if ( normalVariation != null ) {
                // Evaluation of [BLIN1978b]/[FOLE1992].16.23 equation
                N = info.n;    N.normalize();
                Ps = info.t;   Ps.normalize();

                // This is non-sense, but it works! Currently not using
                // tangent vector from geometry! Explain this!
                Ps.x = 0; Ps.y = 1; Ps.z = 0;

                Pt = N.crossProduct(Ps);
                NxPt = N.crossProduct(Pt);
                NxPs = N.crossProduct(Ps);
                Bu = normalVariation.x;
                Bv = normalVariation.y;
                // Note: this only works when `N` is a unit vector. If not,
                //      `normalPerturbation` must be divided by N's length
                normalPerturbation =
                    NxPt.multiply(Bu).substract(NxPs.multiply(Bv));
                info.n = info.n.add(normalPerturbation);
                info.n.normalize();
            }
        }

        //-----------------------------------------------------------------
        //-----------------------------------------------------------------
        int i;
        for ( i = 0; i< lights.size(); i++ ) {
            Light light = lights.get(i);
            lightEmission = light.getSpecular();

            if ( light.tipo_de_luz == Light.AMBIENT ) {
                ambient = material.getAmbient();
                outColor.r += ambient.r*lightEmission.r;
                outColor.g += ambient.g*lightEmission.g;
                outColor.b += ambient.b*lightEmission.b;
              } 
              else {
                Vector3D l;
                if ( light.tipo_de_luz == Light.POINT ) {
                    l = new Vector3D(light.lvec.x - info.p.x, 
                                     light.lvec.y - info.p.y, 
                                     light.lvec.z - info.p.z);
                    l.normalize();
                  } 
                  else {
                    l = new Vector3D(-light.lvec.x, -light.lvec.y, -light.lvec.z);
                }

                // Check if the surface point is in shadow
                static_poffset.x = info.p.x + VSDK.EPSILON*l.x;
                static_poffset.y = info.p.y + VSDK.EPSILON*l.y;
                static_poffset.z = info.p.z + VSDK.EPSILON*l.z;
                static_shadowRay.origin.clone(static_poffset);
                static_shadowRay.direction.clone(l);
                static_shadowRay.direction.normalize();
                nearestObject = selectNearestThingInRayDirection(static_shadowRay, objects);
                if ( nearestObject != null ) {
                    //delete l;
                    continue;
                }

                double lambert = info.n.dotProduct(l);
                if ( lambert > 0 ) {
                    diffuse = material.getDiffuse();
                    if ( info.texture != null ) {
                        diffuse.modulate(
                            info.texture.getColorRgbBiLinear(info.u, 1-info.v));
                    }
                    if ( (diffuse.r + diffuse.g + diffuse.b) > 0 ) {
                        outColor.r += lambert*diffuse.r*lightEmission.r;
                        outColor.g += lambert*diffuse.g*lightEmission.g;
                        outColor.b += lambert*diffuse.b*lightEmission.b;
                    }
                    specular = material.getSpecular();

                    if ( (specular.r + specular.g + specular.b) > 0 ) {
                        lambert *= 2;
                        static_tmp.x = lambert*info.n.x - l.x;
                        static_tmp.y = lambert*info.n.y - l.y;
                        static_tmp.z = lambert*info.n.z - l.z;
                        double spec = 
                            viewVector.dotProduct(static_tmp);

                        if ( spec > 0 ) {
                            // OJO: Raro...
                            spec = ((specular.r + specular.g + specular.b)/3)*(
                                Math.pow(spec, material.getPhongExponent()));
                            outColor.r += spec*lightEmission.r;
                            outColor.g += spec*lightEmission.g;
                            outColor.b += spec*lightEmission.b;
                        }
                    }
                }
                //delete l;
              } // else case of "if ( light.tipo_de_luz == Light.AMBIENT )" conditional
        } // for ( i = 0; i< lights.size(); i++ )

        // Compute illumination due to reflection
        double kr = material.getReflectionCoefficient();
        if ( kr > 0 && recursions > 0 ) {
            double t = viewVector.dotProduct(info.n);
            if ( t > 0 ) {
                t *= 2;
                Vector3D reflect = new Vector3D(t*info.n.x - viewVector.x, 
                                                t*info.n.y - viewVector.y, 
                                                t*info.n.z - viewVector.z);
                Vector3D poffset = new Vector3D(info.p.x + VSDK.EPSILON*reflect.x, 
                                                info.p.y + VSDK.EPSILON*reflect.y, 
                                                info.p.z + VSDK.EPSILON*reflect.z);
                Ray reflected_ray = new Ray(poffset, reflect);

                //delete reflect;
                //delete poffset;

                nearestObject = 
                    selectNearestThingInRayDirection(reflected_ray, objects);
                if ( nearestObject != null ) {
                    Vector3D rv = new Vector3D();
                    GeometryIntersectionInformation subInfo = 
                        new GeometryIntersectionInformation();

                    //--------------------------------------------------------
                    nearestObject.doExtraInformation(
                        reflected_ray, reflected_ray.t, subInfo);

                    //-----
                    if ( !inQualitySelection.isTextureSet() ) {
                        subInfo.texture = null;
                    }
                    else {
                        if ( subInfo.texture == null ) {
                            subInfo.texture = nearestObject.getTexture();
                        }
                    }

                    //-----
                    if ( !inQualitySelection.isBumpMapSet() ) {
                        subInfo.normalMap = nearestObject.getNormalMap();
                    }

                    //--------------------------------------------------------

                    rv.x = -reflected_ray.direction.x;
                    rv.y = -reflected_ray.direction.y;
                    rv.z = -reflected_ray.direction.z;                    
                    ColorRgb rcolor = new ColorRgb();
                    evaluateIlluminationModel(subInfo, rv, lights, objects, 
                                              background, material, 
                                              inQualitySelection,
                                              recursions - 1,
                                              rcolor);

                    outColor.r += kr*rcolor.r;
                    outColor.g += kr*rcolor.g;
                    outColor.b += kr*rcolor.b;

                    //delete subInfo;
                  } 
                  else {
                    outColor.r += kr*backgroundColor.r;
                    outColor.g += kr*backgroundColor.g;
                    outColor.b += kr*backgroundColor.b;
                }
            }
        }

        // Add code for refraction here
        // <TODO>

        // Clamp outColor to MAX 1.0 intensity.
        outColor.r = (outColor.r > 1) ? 1 : outColor.r;
        outColor.g = (outColor.g > 1) ? 1 : outColor.g;
        outColor.b = (outColor.b > 1) ? 1 : outColor.b;

        //delete backgroundColor;
    }

    /**
    This method intersect the `inOut_Ray` with all of the geometries contained
    in `inSimpleBodiesArray`. If none of the geometries is intersected
    `null` is returned, otherwise a reference to the containing SimpleBody
    is returned.

    Warning: This method includes the use of the ray transformation technique
    that permits the representation of geometries centered in its origin,
    and its combination with geometric transformations.
    */
    private SimpleBody 
    selectNearestThingInRayDirection(Ray inOut_Ray, ArrayList <SimpleBody> inSimpleBodiesArray) {
        int i;
        SimpleBody gi;
        SimpleBody nearestObject;
        double nearestDistance;

        nearestDistance = Double.MAX_VALUE;
        nearestObject = null;
        for ( i = 0; i < inSimpleBodiesArray.size(); i++ ) {
            inOut_Ray.t = Double.MAX_VALUE;
            gi = inSimpleBodiesArray.get(i);
            if ( gi.doIntersection(inOut_Ray) && 
                 inOut_Ray.t < nearestDistance &&
                 inOut_Ray.t > VSDK.EPSILON ) {
                nearestDistance = inOut_Ray.t;
                nearestObject = gi;
            }
        }
        inOut_Ray.t = nearestDistance;
        return nearestObject;
    }

    /**
    Warning: This method includes the use of the ray transformation technique
    that permits the representation of geometries centered in its origin,
    and its combination with geometric transformations.

    Note that this method can return null, that means a transparent pixel
    should be used.
    */
    private void followRayPath(Ray inRay,
                               ArrayList <SimpleBody> inSimpleBodiesArray, 
                               ArrayList <Light> inLightsArray,
                               Background in_background,
                               RendererConfiguration inQualitySelection,
                               ColorRgb outColor)
    {
        SimpleBody nearestObject;
        Ray myRay;

        nearestObject = selectNearestThingInRayDirection(inRay, inSimpleBodiesArray);
        if ( nearestObject != null ) {
            //------------------------------------------------------------
            nearestObject.doExtraInformation(inRay, inRay.t, static_info);
            //-----
            if ( !inQualitySelection.isTextureSet() ) {
                static_info.texture = null;
            }
            else {
                if ( static_info.texture == null ) {
                    static_info.texture = nearestObject.getTexture();
                }
            }

            //-----
            if ( !inQualitySelection.isBumpMapSet() ) {
                static_info.normalMap = nearestObject.getNormalMap();
            }

            //------------------------------------------------------------
            Vector3D viewVector = new Vector3D();
            viewVector.x = -inRay.direction.x;
            viewVector.y = -inRay.direction.y;
            viewVector.z = -inRay.direction.z;

            Material material;
            if ( static_info.material != null ) {
                material = static_info.material;
            }
            else {
                material = nearestObject.getMaterial();
            }

            evaluateIlluminationModel(
                static_info, viewVector, inLightsArray, inSimpleBodiesArray, in_background, material,
                inQualitySelection, 3, outColor);
            //delete viewVector;
          }
          else {
            ColorRgb c;
            c = in_background.colorInDireccion(inRay.direction);
            outColor.r = c.r;
            outColor.g = c.g;
            outColor.b = c.b;
        }
    }

    public void execute(RGBImage inoutViewport,
                        RendererConfiguration inQualitySelection,
                        ArrayList <SimpleBody> inSimpleBodiesArray,
                        ArrayList <Light> in_arr_luces,
                        Background in_background,
                        Camera inCamera,
                        ProgressMonitor report)
    {
        execute(inoutViewport, inQualitySelection,
                inSimpleBodiesArray, in_arr_luces,
                in_background, inCamera, report, null, 0, 0,
                inoutViewport.getXSize(), inoutViewport.getYSize());
    }

    public void execute(RGBImage inoutViewport, 
                        RendererConfiguration inQualitySelection,
                        ArrayList <SimpleBody> inSimpleBodiesArray,
                        ArrayList <Light> in_arr_luces,
                        Background in_background,
                        Camera inCamera,
                        ProgressMonitor report,
                        ZBuffer depthmap)
    {
        execute(inoutViewport, inQualitySelection, inSimpleBodiesArray, 
                in_arr_luces,
                in_background, inCamera, report, depthmap, 0, 0,
                inoutViewport.getXSize(), inoutViewport.getYSize());
    }

    /**
    Macroalgoritmo de control para raytracing. Este m&eacute;todo recibe
    el modelo de una escena 3D previamente construida en memoria y una
    imagen, y modifica la imagen de tal forma que contiene una visualizacion
    de la escena, result de aplicar la t&eacute;cnica de raytracing.

    PARAMETERS
    - `inout_viewport`: imagen RGB en donde el algoritmo calcular&aacute; su
       result.
    - `inSimpleBodiesArray`: arreglo din&aacute;mico de SimpleBodys que constituyen los
       objetos visibles de la escena.
    - `inLightsArray`: arreglo din&aacute;mico de Light'es (luces puntuales)
    - `in_background`: especificaci&oacute;n de un color de fondo para la escena
      (i.e. el color que se ve si no se ve ning&uacute;n objeto!)
    - `inCamera`: especificaci&oacute;n de la transformaci&oacute;n de
      proyecci&oacute;n 3D a 2D que se lleva a cabo en el proceso de 
      visualizaci&oacute;n.
    - `depthmap`: can be null or a reference to a ZBuffer. If it is null,
      nothing is done with this parameter. If it is not null, the associated
      ZBuffer is filled with depth values corresponding to distances 
      calculated in world space coordinates from ray intersections.
      Note that depth values are not scaled neither clamped to any specific
      range, so post-processing should be done if wanting to combine that
      with other depth maps, as those generated from OpenGL's ZBuffer.
    - `liveReport` can be null. In that case no report is updated.


    PRE:
    - Todas las referencias estan creadas, asi sea que apunten a estructuras
      vac&iacute;as.
    - La imagen `inout_viewport` esta creada, y es de el tama&ntilde;o que
      el usuario desea para su visualizaci&oacute;n.
    - In the case the ZBuffer depthmap is not null, the ZBuffer must be
      initialized to the same size of the image inoutViewport.

    POST:
    - `inout_viewport` contiene una representaci&oacute;n visual de la
       escena 3D (`inSimpleBodiesArray`, `inLightsArray`, `in_background`), tal que corresponde a
       una proyecci&oacute;n 3D a 2D controlada por la c&aacute;mara
       virtual `inCamera`.

    NOTA: Este algoritmo se inici&oacute; como una modificaci&oacute;n del 
          raytracer del curso 6.837 (computaci&oacute;n gr&aacute;fica) de MIT,
          original de Leonard McMillan y Tomas Lozano Perez, pero puede 
          considerarse que es una re-escritura y re-estructuraci&oacute;n 
          completa de Oscar Chavarro.
    */
    public void execute(RGBImage inoutViewport,
                        RendererConfiguration inQualitySelection,
                        ArrayList <SimpleBody> inSimpleBodiesArray,
                        ArrayList <Light> inLightsArray,
                        Background inBackground,
                        Camera inCamera,
                        ProgressMonitor liveReport,
                        ZBuffer outDepthmap,
                        int limx1, int limy1,
                        int limx2, int limy2)
    {
        int x, y;
        int relativeX;
        int relativeY;
        Ray rayo;
        ColorRgb color = new ColorRgb();

        inCamera.updateVectors();

        if ( liveReport != null ) {
            liveReport.begin();
        }
        for ( y = limy1, relativeY = 0; y < limy2; y++, relativeY++ ) {
            if ( liveReport != null ) {
                liveReport.update(0, inoutViewport.getYSize(), y);
            }
            for ( x = limx1, relativeX = 0; x < limx2; x++, relativeX++ ) {
                //- Trazado individual de un rayo --------------------------
                // Es importante que la operacion generateRay sea inline
                // (i.e. "final")
                rayo = inCamera.generateRay(x, y);
                color.r = 0;
                color.g = 0;
                color.b = 0;
                followRayPath(rayo, inSimpleBodiesArray,
                              inLightsArray, inBackground, 
                              inQualitySelection, color);
                if ( outDepthmap != null ) {
                    outDepthmap.setZ(x, y, (float)rayo.t);
                }
                //- Exporto el result de color del pixel ----------------
                if ( color != null ) {
                    inoutViewport.putPixel(relativeX, relativeY,
                                              (byte)(255 * color.r),
                                              (byte)(255 * color.g),
                                              (byte)(255 * color.b));
                }
            }
        }
        //delete color;
        //delete ray;

        if ( liveReport != null ) {
            liveReport.end();
        }
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
