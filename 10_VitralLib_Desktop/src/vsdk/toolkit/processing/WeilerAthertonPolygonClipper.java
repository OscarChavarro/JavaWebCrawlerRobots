package vsdk.toolkit.processing;

//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - September 4 2014 - Leonardo Rebolledo: Original base version          =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [WEATH1977] Kevin Weiler and Peter Atherton. "HIDDEN SURFACE REMOVAL    =
//= USING POLYGON AREA SORTING",                                            =
//= Program of Computer Graphics, Cornell University. Ithaca, New York, 1977=
//===========================================================================
import java.util.ArrayList;
import vsdk.toolkit.environment.geometry.Polygon2D;

/**
 * This class implements the Weiler Atherton algorithm to clip convex and non
 * convex polygons with holes with convex and non convex polygons with holes.
 * Also, the capacity to manage special cases(where points of clip or clipped
 * polygons coincide with the contour of each other) is added.
 *
 * In this class, the section of the paper [WEATH1977] that is implemented is
 * "POLYGON CLIPPING ALGORITHM".
*/
public class WeilerAthertonPolygonClipper {

    private _Polygon2DWA clipPolyWA;
    private _Polygon2DWA subjectPolyWA;
    // Temporal variables.
    private boolean firstIntersection;
    private boolean previousOut;
    private final boolean coincidentPoints[] = new boolean[4];

    /**
     * This function performs the clipping operation. 
     *
     * @param clipPoly are the clipping polygons.
     * @param subjectPoly are the polygons to clip.
     * @param innerPolyOut are the clipped polygons that lie inside of the
     * clipping polygon.
     * @param outerPolyOut are the clipped polygons that lie outside of the
     * clipping polygon.
     */
    public void clipPolygons(Polygon2D clipPoly, Polygon2D subjectPoly, Polygon2D innerPolyOut, Polygon2D outerPolyOut) {
        _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeC;
        _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeS;
        _DoubleLinkedListNode<_VertexNode2D> dllnVertNodePrevC;
        _DoubleLinkedListNode<_VertexNode2D> dllnVertNodePrevS;
        _VertexNode2D nodeC, nodePrevC;
        _VertexNode2D nodeS, nodePrevS;
        _VertexNode2D nodeIntersecS;
        ArrayList<_DoubleLinkedListNode<_VertexNode2D>> intersecVertListOut = new ArrayList<_DoubleLinkedListNode<_VertexNode2D>>();
        ArrayList<_DoubleLinkedListNode<_VertexNode2D>> intersecVertListIn = new ArrayList<_DoubleLinkedListNode<_VertexNode2D>>();
        _DoubleLinkedListNode<_VertexNode2D> iterator;
        boolean emptyInnerPolyOut = true;
        boolean emptyOuterPolyOut = true;
        int i,j;

        if ( innerPolyOut == null ) {
            return;
        }
        if ( outerPolyOut == null ) {
            return;
        }
        clipPolyWA = new _Polygon2DWA(clipPoly, true);
        subjectPolyWA = new _Polygon2DWA(subjectPoly, true);
        //Find the intersections and fills intersecVertListOut and intersecVertListIn.
        //for ( _Polygon2DContourWA p2DContClip : getClipPolyWA().loops ) {
        for ( i = 0; i < clipPolyWA.loops.size(); ++i ) {
            _Polygon2DContourWA p2DContClip = clipPolyWA.loops.get(i);
            p2DContClip.isClipped = false;
            dllnVertNodeC = p2DContClip.vertices.getHead();
            if ( p2DContClip.vertices.size() > 1 ) {
                do {
                    nodeC = dllnVertNodeC.data;
                    dllnVertNodePrevC = dllnVertNodeC.previous;
                    nodePrevC = dllnVertNodePrevC.data;
                    previousOut = false; //To avoid parser error.
//                    for ( _Polygon2DContourWA p2DContSubj : getSubjectPolyWA().loops ) {
                    for ( j = 0; j < subjectPolyWA.loops.size(); ++j ) {
                        _Polygon2DContourWA p2DContSubj = subjectPolyWA.loops.get(j);
                        dllnVertNodeS = p2DContSubj.vertices.getHead();
                        if ( p2DContSubj.vertices.size() > 1 ) {
                            firstIntersection = true; //For all lines in the current polygon for current cuting edge.
                            do {
                                nodeS = dllnVertNodeS.data;
                                dllnVertNodePrevS = dllnVertNodeS.previous;
                                nodePrevS = dllnVertNodePrevS.data;
                                nodeIntersecS = new _VertexNode2D();
                                if ( intersecLineLine2D(nodePrevC, nodeC, nodePrevS, nodeS, nodeIntersecS, coincidentPoints) ) {
                                    makeCut(
                                        p2DContClip, p2DContSubj, dllnVertNodePrevC, dllnVertNodeC, dllnVertNodePrevS, dllnVertNodeS, nodeIntersecS, intersecVertListOut, intersecVertListIn);
                                }
                                dllnVertNodeS = dllnVertNodeS.next;
                            } while ( dllnVertNodeS != p2DContSubj.vertices.getHead() );
                        }
                    }
                    dllnVertNodeC = dllnVertNodeC.next;
                } while ( dllnVertNodeC != p2DContClip.vertices.getHead() );
            }
        }
        //The actual clipping is now performed.
        //Inner polygons:
//        for ( DoubleLinkedListNode<VertexNode2D> dllnNodeS : intersecVertListOut ) {
        for ( i=0; i < intersecVertListOut.size(); ++i ) {
            _DoubleLinkedListNode<_VertexNode2D> dllnNodeS = intersecVertListOut.get(i);
            if ( (dllnNodeS.data.flags & 0x01) == 0 ) {
                if ( emptyInnerPolyOut ) {
                    emptyInnerPolyOut = false;
                } else {
                    innerPolyOut.nextLoop();
                }
                iterator = dllnNodeS;
                do {
                    iterator.data.flags = (byte) (iterator.data.flags | 0x01); //Now this node may not be used in an interior polygon.
                    innerPolyOut.addVertex(iterator.data.x, iterator.data.y, iterator.data.color.r, iterator.data.color.g, iterator.data.color.b);
                    iterator = iterator.next;
                    iterator.data.flags = (byte) (iterator.data.flags | 0x01); //Now this node may not be used in an interior polygon.
                    if ( iterator.data.pairNode != null ) {
                        iterator = iterator.data.pairNode;
                    }
                } while ( iterator != dllnNodeS && iterator.data.pairNode != dllnNodeS );
            }
        }
        //Outer polygons:
        //outerPolyOut = new Polygon2D();
//        for ( DoubleLinkedListNode<VertexNode2D> dllnNodeS : intersecVertListIn ) {
        for ( i=0; i < intersecVertListIn.size(); ++i ) {
            _DoubleLinkedListNode<_VertexNode2D> dllnNodeS = intersecVertListIn.get(i);
            if ( (dllnNodeS.data.flags & 0x02) == 0 ) {
                boolean isSubject;

                if ( emptyOuterPolyOut ) {
                    emptyOuterPolyOut = false;
                } else {
                    outerPolyOut.nextLoop();
                }
                iterator = dllnNodeS;
                isSubject = true;
                do {
                    iterator.data.flags = (byte) (iterator.data.flags | 0x02); //Now this node may not be used in an outer polygon.
                    outerPolyOut.addVertex(iterator.data.x, iterator.data.y, iterator.data.color.r, iterator.data.color.g, iterator.data.color.b);
                    if ( isSubject ) {
                        iterator = iterator.next;
                    } else {
                        iterator = iterator.previous;
                    }
                    iterator.data.flags = (byte) (iterator.data.flags | 0x02); //Now this node may not be used in an outer polygon.
                    if ( iterator.data.pairNode != null ) {
                        iterator = iterator.data.pairNode;
                        isSubject = !isSubject;
                    }
                } while ( iterator != dllnNodeS && iterator.data.pairNode != dllnNodeS );
            }
        }

        //Polygons fully inside of fully outside:
        //Classify holes and contours in clip polygon.
        classifyHolesAndContours(getClipPolyWA());
        //Classify holes and contours in subject polygon.
        classifyHolesAndContours(getSubjectPolyWA());

        //Clip polygons, in subject polygons, with clipping poligons fully inside.
//        for ( _Polygon2DContourWA p2DContClip : getClipPolyWA().loops ) {
        for ( i=0; i < clipPolyWA.loops.size(); ++i ) {
            _Polygon2DContourWA p2DContClip = clipPolyWA.loops.get(i);
            if ( !p2DContClip.isClipped ) {
                _DoubleLinkedListNode<_VertexNode2D> dllnNode, head;
                boolean insideAContourNotAHole = false; //Inside a contour and not inside a hole.
                _VertexNode2D vertClip;
                boolean pointInPolygon;

                head = p2DContClip.vertices.getHead();
                if ( head == null ) {
                    continue;
                }
//                for ( _Polygon2DContourWA p2DContSubj : getSubjectPolyWA().loops ) {
                for ( j=0; j < subjectPolyWA.loops.size(); ++j ) {
                    _Polygon2DContourWA p2DContSubj = subjectPolyWA.loops.get(j);
                    dllnNode = head;
                    pointInPolygon = false;
                    do {
                        vertClip = dllnNode.data;
                        if ( isPointInPolygon2D(vertClip, p2DContSubj.vertices) == 1 ) {
                            pointInPolygon = true;
                            break;
                        }
                        dllnNode = dllnNode.next;
                    } while ( dllnNode != head );
                    if ( pointInPolygon ) {
                        insideAContourNotAHole = !insideAContourNotAHole;
                    }
                }
                //Clip:
                if ( insideAContourNotAHole ) {
//                    DoubleLinkedListNode<VertexNode2D> dllnNode,head;
//                    head = p2DContClip.vertices.getHead();
                    if ( emptyInnerPolyOut ) {
                        emptyInnerPolyOut = false;
                    } else {
                        innerPolyOut.nextLoop();
                    }
                    if ( emptyOuterPolyOut ) {
                        emptyOuterPolyOut = false;
                    } else {
                        outerPolyOut.nextLoop();
                    }
                    dllnNode = head;
                    do { //If clip poly is hole then hole in innerPolyOut[0] else polygon in innerPolyOut[0].
                        innerPolyOut.addVertex(dllnNode.data.x, dllnNode.data.y, dllnNode.data.color.r, dllnNode.data.color.g, dllnNode.data.color.b);
                        dllnNode = dllnNode.next;
                    } while ( dllnNode != head );
                    dllnNode = head;
                    do { //If clip poly is hole then polygon in outerPolyOut[0] else hole in outerPolyOut[0].
                        outerPolyOut.addVertex(dllnNode.data.x, dllnNode.data.y, dllnNode.data.color.r, dllnNode.data.color.g, dllnNode.data.color.b);
                        dllnNode = dllnNode.previous;
                    } while ( dllnNode != head );
                }
            }
        }
        //Classify non clipped subject polygons.
//        for ( _Polygon2DContourWA p2DContSubj : getSubjectPolyWA().loops ) {
        for ( i=0; i < subjectPolyWA.loops.size(); ++i ) {
            _Polygon2DContourWA p2DContSubj = subjectPolyWA.loops.get(i);
            if ( !p2DContSubj.isClipped ) {
                boolean insideAContourNotAHole = false; //Inside a contour and not inside a hole.
                _VertexNode2D vertSubj;
                _DoubleLinkedListNode<_VertexNode2D> dllnNode, head;
                boolean pointInPolygon;

                //vertSubj = p2DContSubj.vertices.getHead().data;
                head = p2DContSubj.vertices.getHead();
                if ( head == null ) {
                    continue;
                }
//                for ( _Polygon2DContourWA p2DContClip : getClipPolyWA().loops ) {
                for ( j=0; j < clipPolyWA.loops.size(); ++j ) {
                    _Polygon2DContourWA p2DContClip = clipPolyWA.loops.get(j);
                    dllnNode = head;
                    pointInPolygon = false;

                    do {
                        vertSubj = dllnNode.data;
                        if ( isPointInPolygon2D(vertSubj, p2DContClip.vertices) == 1 ) {
                            pointInPolygon = true;
                            break;
                        }
                        dllnNode = dllnNode.next;
                    } while ( dllnNode != head );
                    if ( pointInPolygon ) {
                        insideAContourNotAHole = !insideAContourNotAHole;
                    }
                }
                head = p2DContSubj.vertices.getHead();
//                if(insideAContourNotAHole)
//                    poly = innerPolyOut[0];
//                else
//                    poly = outerPolyOut[0];
//                if(poly == null)
//                    poly = new Polygon2D();
//                else
//                    poly.nextLoop();

                if ( insideAContourNotAHole ) {
                    if ( emptyInnerPolyOut ) {
                        emptyInnerPolyOut = false;
                    } else {
                        innerPolyOut.nextLoop();
                    }
                } else {
                    if ( emptyOuterPolyOut ) {
                        emptyOuterPolyOut = false;
                    } else {
                        outerPolyOut.nextLoop();
                    }
                }

                dllnNode = head;
                do {
                    if ( insideAContourNotAHole ) {
                        innerPolyOut.addVertex(dllnNode.data.x, dllnNode.data.y, dllnNode.data.color.r, dllnNode.data.color.g, dllnNode.data.color.b);
                    } else {
                        outerPolyOut.addVertex(dllnNode.data.x, dllnNode.data.y, dllnNode.data.color.r, dllnNode.data.color.g, dllnNode.data.color.b);
                    }
                    dllnNode = dllnNode.next;
                } while ( dllnNode != head );
            }
        }

    }

    /**
     * Detects what kind of intersection is and make a cut if some conditions
     * are met.
     *
     * @param p2DContClip
     * @param p2DContSubj
     * @param dllnVertNodePrevC
     * @param dllnVertNodeC
     * @param dllnVertNodePrevS
     * @param dllnVertNodeS
     * @param nodeIntersecS
     * @param intersecVertListOut
     * @param intersecVertListIn
     */
    private void makeCut(
        _Polygon2DContourWA p2DContClip, _Polygon2DContourWA p2DContSubj, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodePrevC, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeC, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodePrevS, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeS, _VertexNode2D nodeIntersecS, ArrayList<_DoubleLinkedListNode<_VertexNode2D>> intersecVertListOut, ArrayList<_DoubleLinkedListNode<_VertexNode2D>> intersecVertListIn) {
        _VertexNode2D nodeC, nodePrevC;
        _VertexNode2D nodeS, nodePrevS;
        double dotProd;
        boolean firstCutOutOfSubject;

        if ( !coincidentPoints[0] && !coincidentPoints[2] ) { //Normal case.
            firstCutOutOfSubject = false;
            if ( firstIntersection ) {
                firstCutOutOfSubject = (crossProduct2D(
                    dllnVertNodePrevS.data, dllnVertNodeS.data, dllnVertNodePrevC.data, dllnVertNodeC.data) < 0);
            }
            updatePolygonsAndListsWithCuts(
                p2DContClip, p2DContSubj, dllnVertNodePrevC, dllnVertNodeC, dllnVertNodePrevS, dllnVertNodeS, nodeIntersecS, firstCutOutOfSubject, intersecVertListOut, intersecVertListIn, true);
            return;
        }
        // There are one or more coincident points.
        nodePrevC = dllnVertNodePrevC.data;
        nodeC = dllnVertNodeC.data;
        nodePrevS = dllnVertNodePrevS.data;
        nodeS = dllnVertNodeS.data;
        dotProd = dotProductNorm2D(nodePrevC, nodeC, nodePrevS, nodeS);
        if ( dotProd >= 0.9999 && dotProd <= 1.0001 ) { //Are parallel and in the same direction.
            /**
             * <br>To understand this vectors, see the image: For the first points:<br>
             * <img src="doc-files\ParallelLinesFirstPoints.png" alt="Parallel lines, first points">
             * <br>For the second points:<br>
             * <img src="doc-files\ParallelLinesSecondPoints.png" alt="Parallel lines, second points">
             */
            _VertexNode2D vecParallel = new _VertexNode2D();
            _VertexNode2D negVecParallel = new _VertexNode2D();
            _VertexNode2D vecAwayParallelLines1C = new _VertexNode2D();
            _VertexNode2D vecAwayParallelLines1S = new _VertexNode2D();
            _VertexNode2D vecAwayParallelLines2C = new _VertexNode2D();
            _VertexNode2D vecAwayParallelLines2S = new _VertexNode2D();

            vecParallel.x = nodeC.x - nodePrevC.x;
            vecParallel.y = nodeC.y - nodePrevC.y;
            negVecParallel.x = -vecParallel.x;
            negVecParallel.y = -vecParallel.y;
            if ( coincidentPoints[0] ) {
                vecAwayParallelLines1C.x = dllnVertNodePrevC.previous.data.x - dllnVertNodePrevC.data.x;
                vecAwayParallelLines1C.y = dllnVertNodePrevC.previous.data.y - dllnVertNodePrevC.data.y;
            } else {
                vecAwayParallelLines1C.x = negVecParallel.x;
                vecAwayParallelLines1C.y = negVecParallel.y;
            }
            if ( coincidentPoints[2] ) {
                vecAwayParallelLines1S.x = dllnVertNodePrevS.previous.data.x - dllnVertNodePrevS.data.x;
                vecAwayParallelLines1S.y = dllnVertNodePrevS.previous.data.y - dllnVertNodePrevS.data.y;
            } else {
                vecAwayParallelLines1S.x = negVecParallel.x;
                vecAwayParallelLines1S.y = negVecParallel.y;
            }
            if ( coincidentPoints[1] ) {
                vecAwayParallelLines2C.x = dllnVertNodeC.next.data.x - dllnVertNodeC.data.x;
                vecAwayParallelLines2C.y = dllnVertNodeC.next.data.y - dllnVertNodeC.data.y;
            } else {
                vecAwayParallelLines2C.x = vecParallel.x;
                vecAwayParallelLines2C.y = vecParallel.y;
            }
            if ( coincidentPoints[3] ) {
                vecAwayParallelLines2S.x = dllnVertNodeS.next.data.x - dllnVertNodeS.data.x;
                vecAwayParallelLines2S.y = dllnVertNodeS.next.data.y - dllnVertNodeS.data.y;
            } else {
                vecAwayParallelLines2S.x = vecParallel.x;
                vecAwayParallelLines2S.y = vecParallel.y;
            }
            // First points.
            if ( are3VectorsOrderedCounterclockwise2D(
                vecParallel, vecAwayParallelLines1C, vecAwayParallelLines1S) == 1 ) { //Make cut.
                firstCutOutOfSubject = true; //Is used in the next function only if firstIntersection[0]=true.
                updatePolygonsAndListsWithCuts(
                    p2DContClip, p2DContSubj, dllnVertNodePrevC, dllnVertNodeC, dllnVertNodePrevS, dllnVertNodeS, nodeIntersecS, firstCutOutOfSubject, intersecVertListOut, intersecVertListIn, true);
            }
            // Second points.
            if ( are3VectorsOrderedCounterclockwise2D(
                negVecParallel, vecAwayParallelLines2S, vecAwayParallelLines2C) == 1 ) { //Make second cut
                firstCutOutOfSubject = false; //Is used in the next function only if firstIntersection[0]=true.
                updatePolygonsAndListsWithCuts(
                    p2DContClip, p2DContSubj, dllnVertNodePrevC, dllnVertNodeC, dllnVertNodePrevS, dllnVertNodeS, nodeIntersecS, firstCutOutOfSubject, intersecVertListOut, intersecVertListIn, false);
            }
        } else if ( Math.abs(dotProd) < 0.9999 ) { //Are not parallel.
            // Here only coincidentPoints[0] and coincidentPoints[2] are considered.
            _VertexNode2D vecAC = new _VertexNode2D();
            _VertexNode2D vecBC = new _VertexNode2D();
            _VertexNode2D vecAS = new _VertexNode2D();
            _VertexNode2D vecBS = new _VertexNode2D();
            boolean thereAreCut;
            byte orderVecAC, orderVecBC;

            vecAC.x = dllnVertNodeC.data.x - dllnVertNodePrevC.data.x;
            vecAC.y = dllnVertNodeC.data.y - dllnVertNodePrevC.data.y;
            if ( coincidentPoints[0] ) {
                vecBC.x = dllnVertNodePrevC.previous.data.x - dllnVertNodePrevC.data.x;
                vecBC.y = dllnVertNodePrevC.previous.data.y - dllnVertNodePrevC.data.y;
            } else {
                vecBC.x = -vecAC.x;
                vecBC.y = -vecAC.y;
            }
            vecAS.x = dllnVertNodeS.data.x - dllnVertNodePrevS.data.x;
            vecAS.y = dllnVertNodeS.data.y - dllnVertNodePrevS.data.y;
            if ( coincidentPoints[2] ) {
                vecBS.x = dllnVertNodePrevS.previous.data.x - dllnVertNodePrevS.data.x;
                vecBS.y = dllnVertNodePrevS.previous.data.y - dllnVertNodePrevS.data.y;
            } else {
                vecBS.x = -vecAS.x;
                vecBS.y = -vecAS.y;
            }
            // find if there is a cut.
            orderVecAC = are3VectorsOrderedCounterclockwise2D(vecAS, vecAC, vecBS);
            orderVecBC = are3VectorsOrderedCounterclockwise2D(vecAS, vecBC, vecBS);
            thereAreCut = false;
            firstCutOutOfSubject = false; //To avoid parser error.
            if ( orderVecAC != 0 && orderVecBC != 0 ) {// Pararel lines are considered in another section.
                if ( (orderVecAC == 1) != (orderVecBC == 1) ) {
                    thereAreCut = true;
                    firstCutOutOfSubject = (orderVecAC == -1); // Ordered clockwise.
                }
            }
            if ( thereAreCut ) {
                updatePolygonsAndListsWithCuts(
                    p2DContClip, p2DContSubj, dllnVertNodePrevC, dllnVertNodeC, dllnVertNodePrevS, dllnVertNodeS, nodeIntersecS, firstCutOutOfSubject, intersecVertListOut, intersecVertListIn, true);
            }
        }
    }

    /**
     * Insert points of cuts in original polygons(or update the pairNode
     * property of the VertexNode2D of the polygons, in the case of coincident
     * points), the lists of intersections are filled too.
     *
     * @param p2DContClip
     * @param p2DContSubj
     * @param dllnVertNodePrevC
     * @param dllnVertNodeC
     * @param dllnVertNodePrevS
     * @param dllnVertNodeS
     * @param nodeIntersecS
     * @param firstCutOutOfSubject Only valid if firstIntersection is true.
     * @param intersecVertListOut
     * @param intersecVertListIn
     * @param operateOnFirstPointsOfLines False only in the case of a cut in the
     * second points of parallel clip and subject lines.
     */
    private void updatePolygonsAndListsWithCuts(
        _Polygon2DContourWA p2DContClip, _Polygon2DContourWA p2DContSubj, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodePrevC, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeC, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodePrevS, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeS, _VertexNode2D nodeIntersecS, boolean firstCutOutOfSubject, ArrayList<_DoubleLinkedListNode<_VertexNode2D>> intersecVertListOut, ArrayList<_DoubleLinkedListNode<_VertexNode2D>> intersecVertListIn, boolean operateOnFirstPointsOfLines) {
        _VertexNode2D nodeIntersecC;
        _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeCutS;

        dllnVertNodeCutS = null; //To avoid parser error.
        if ( !coincidentPoints[0] && !coincidentPoints[2] ) { //Normal case.
            nodeIntersecC = new _VertexNode2D(nodeIntersecS);
            //Update the two linked lists:
            nodeIntersecS.pairNode = insertOrderedNodeBetweenTwoNodes(
                p2DContClip.vertices, dllnVertNodePrevC, dllnVertNodeC, nodeIntersecC);
            nodeIntersecC.pairNode = p2DContSubj.vertices.insertBefore(nodeIntersecS, dllnVertNodeS);
            dllnVertNodeCutS = nodeIntersecC.pairNode;
        } else {
            _VertexNode2D nodeIntersec;

            if ( operateOnFirstPointsOfLines ) {
                if ( coincidentPoints[0] ) {
                    if ( coincidentPoints[2] ) {
                        dllnVertNodePrevC.data.pairNode = dllnVertNodePrevS;
                        dllnVertNodePrevS.data.pairNode = dllnVertNodePrevC;
                        dllnVertNodeCutS = dllnVertNodePrevS;
                    } else {
                        nodeIntersec = new _VertexNode2D(dllnVertNodePrevC.data);
                        dllnVertNodePrevC.data.pairNode
                            = p2DContSubj.vertices.insertBefore(nodeIntersec, dllnVertNodeS);
                        nodeIntersec.pairNode = dllnVertNodePrevC;
                        dllnVertNodeCutS = dllnVertNodePrevC.data.pairNode;
                    }
                } else if ( coincidentPoints[2] ) {
                    nodeIntersec = new _VertexNode2D(dllnVertNodePrevS.data);
                    dllnVertNodePrevS.data.pairNode = insertOrderedNodeBetweenTwoNodes(
                        p2DContClip.vertices, dllnVertNodePrevC, dllnVertNodeC, nodeIntersec);
                    nodeIntersec.pairNode = dllnVertNodePrevS;
                    dllnVertNodeCutS = dllnVertNodePrevS;
                }
            } else {
                if ( coincidentPoints[1] ) {
                    if ( coincidentPoints[3] ) {
                        dllnVertNodeC.data.pairNode = dllnVertNodeS;
                        dllnVertNodeS.data.pairNode = dllnVertNodeC;
                        dllnVertNodeCutS = dllnVertNodeS;
                    } else {
                        nodeIntersec = new _VertexNode2D(dllnVertNodeC.data);
                        dllnVertNodeC.data.pairNode
                            = p2DContSubj.vertices.insertBefore(nodeIntersec, dllnVertNodeS);
                        nodeIntersec.pairNode = dllnVertNodeC;
                        dllnVertNodeCutS = dllnVertNodeC.data.pairNode;
                    }
                } else if ( coincidentPoints[3] ) {
                    nodeIntersec = new _VertexNode2D(dllnVertNodeS.data);
                    dllnVertNodeS.data.pairNode = insertOrderedNodeBetweenTwoNodes(
                        p2DContClip.vertices, dllnVertNodePrevC, dllnVertNodeC, nodeIntersec);
                    nodeIntersec.pairNode = dllnVertNodeS;
                    dllnVertNodeCutS = dllnVertNodeS;
                }
            }
        }
        // Fill the two list containing the intersections in the subject polygon;
        // when the contour of the clip polygon leaves and when enters the subject polygon.
        if ( firstIntersection ) {
            p2DContSubj.isClipped = true;
            p2DContClip.isClipped = true;
            if ( firstCutOutOfSubject ) {
                intersecVertListOut.add(dllnVertNodeCutS);
                previousOut = true;
            } else {
                intersecVertListIn.add(dllnVertNodeCutS);
                previousOut = false;
            }
            firstIntersection = false;
        } else {
            if ( previousOut ) {
                intersecVertListIn.add(dllnVertNodeCutS);
            } else {
                intersecVertListOut.add(dllnVertNodeCutS);
            }
            previousOut = !previousOut;
        }
    }

    /**
     * Identifies holes and contours in a polygon and stores that information in
     * the polygon. Assumes no intersections between holes and contours.
     *
     * @param polygon Polygon whose contours will be classified.
     */
    private void classifyHolesAndContours(_Polygon2DWA polygon) {
        int i,j;
        
//        for ( _Polygon2DContourWA p2DContTest : polygon.loops ) {
        for ( i = 0; i < polygon.loops.size(); ++i ) {
            _Polygon2DContourWA p2DContTest = polygon.loops.get(i);
            _DoubleLinkedListNode<_VertexNode2D> dllnNode, head;
            _VertexNode2D vertTest;
            boolean pointInPolygon;

//            for ( _Polygon2DContourWA p2DCont : polygon.loops ) {
            for ( j = 0; j < polygon.loops.size(); ++j ) {
                _Polygon2DContourWA p2DCont = polygon.loops.get(j);
                if ( p2DCont != p2DContTest ) {
                    head = p2DContTest.vertices.getHead();
                    if ( head == null ) {
                        continue;
                    }
                    dllnNode = head;
                    pointInPolygon = false;
                    do {
                        vertTest = dllnNode.data;
                        if ( isPointInPolygon2D(vertTest, p2DCont.vertices) == 1 ) {
                            pointInPolygon = true;
                            break;
                        }
                        dllnNode = dllnNode.next;
                    } while ( dllnNode != head );
                    if ( pointInPolygon ) {
                        p2DContTest.isHole = !p2DContTest.isHole;
                    }
                }
            }
        }
    }

    /**
     * Tests if the given three vectors laying in the origin, are ordered
     * counterclockwise clockwise or two or three vectors are collinear.
     *
     * @param v1 Vector one.
     * @param v2 Vector two.
     * @param v3 Vector three.
     * @return 1: are ordered counterclockwise, -1: are ordered clockwise, 0:
     * there are collinear vectors in the same direction.
     */
    private byte are3VectorsOrderedCounterclockwise2D(_VertexNode2D v1, _VertexNode2D v2, _VertexNode2D v3) {
        double temp;

        temp = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
        v1.x = v1.x / temp;
        v1.y = v1.y / temp;
        temp = Math.sqrt(v2.x * v2.x + v2.y * v2.y);
        v2.x = v2.x / temp;
        v2.y = v2.y / temp;
        temp = Math.sqrt(v3.x * v3.x + v3.y * v3.y);
        v3.x = v3.x / temp;
        v3.y = v3.y / temp;
        temp = dotProduct2D(v1, v2);
        if ( temp > 0.9999 && temp < 1.0001 ) {
            return 0;
        }
        temp = dotProduct2D(v2, v3);
        if ( temp > 0.9999 && temp < 1.0001 ) {
            return 0;
        }
        temp = dotProduct2D(v1, v3);
        if ( temp > 0.9999 && temp < 1.0001 ) {
            return 0;
        }

        if ( crossProduct2D(v1, v3) < 0 ) {
            if ( crossProduct2D(v2, v3) >= 0 ) {
                return 1;
            } else if ( crossProduct2D(v1, v2) > 0 ) {
                return 1;
            } else {
                return -1;
            }
        } else {
            if ( crossProduct2D(v2, v3) <= 0 ) {
                return -1;
            } else if ( crossProduct2D(v2, v1) > 0 ) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /* Function isPointInPolygon2D is based on the function developed by W. Randolph
     Franklin:
     http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     visited on sep 08 2014.
     The part that tests if the point is in de boundary is new.
    
     License to Use

     Copyright (c) 1970-2003, Wm. Randolph Franklin

     Permission is hereby granted, free of charge, to any person obtaining a
     copy of this software and associated documentation files (the "Software"),
     to deal in the Software without restriction, including without limitation
     the rights to use, copy, modify, merge, publish, distribute, sublicense,
     and/or sell copies of the Software, and to permit persons to whom the
     Software is furnished to do so, subject to the following conditions:

     Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimers.
     Redistributions in binary form must reproduce the above copyright notice
     in the documentation and/or other materials provided with the distribution.
     The name of W. Randolph Franklin may not be used to endorse or promote
     products derived from this Software without specific prior written permission.
     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
     OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
     IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
     */
    /**
     * Test if point is inside, outside or on the boundary of the polygon.
     *
     * @param point
     * @param polygon
     * @return 1 if the point is inside, -1 if the point is outside, 0 if the
     * point is on the boundary.
     */
    private byte isPointInPolygon2D(_VertexNode2D point, _CircDoubleLinkedList<_VertexNode2D> polygon) {
        _DoubleLinkedListNode<_VertexNode2D> dllnNode;
        boolean isInside = false;
        double temp;

        dllnNode = polygon.getHead();
        if ( dllnNode == null ) {
            return -1;
        }
        do {
//            if(point.y == dllnNode.data.y && point.x<dllnNode.data.x){
//                if(dllnNode.data.y<dllnNode.next.data.y != dllnNode.data.y<dllnNode.previous.data.y)
//                    isInside = !isInside;
//            } else
            temp = dllnNode.data.y - dllnNode.next.data.y;
            if ( Math.abs(temp) < 0.0001 ) { //Horizontal line.
                if ( Math.abs(point.y - (dllnNode.next.data.y + temp / 2)) < 0.0001 ) {
                    if ( (dllnNode.next.data.x - point.x) * (point.x - dllnNode.data.x) >= 0 ) {
                        return 0;
                    }
                }
            }
            if ( point.y < dllnNode.data.y != point.y < dllnNode.next.data.y ) {
                temp = ((dllnNode.next.data.x - dllnNode.data.x) * (point.y - dllnNode.data.y) / (dllnNode.next.data.y - dllnNode.data.y) + dllnNode.data.x);
                if ( Math.abs(point.x - temp) < 0.0001 ) {
                    return 0;
                }
                if ( point.x < temp ) {
                    isInside = !isInside;
                }
            }
            dllnNode = dllnNode.next;
        } while ( dllnNode != polygon.getHead() );
        if ( isInside ) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Returns the z coordinate, x and y are zero.
     *
     * @param a1
     * @param a2
     * @param b1
     * @param b2
     * @return
     */
    private double crossProduct2D(_VertexNode2D a1, _VertexNode2D a2, _VertexNode2D b1, _VertexNode2D b2) {
        double[] v1, v2;

        v1 = new double[2];
        v2 = new double[2];
        v1[0] = a2.x - a1.x;
        v1[1] = a2.y - a1.y;
        v2[0] = b2.x - b1.x;
        v2[1] = b2.y - b1.y;
        return v1[0] * v2[1] - v1[1] * v2[0];
    }

    /**
     * Returns the z coordinate, x and y are zero.
     *
     * @param v1
     * @param v2
     * @return
     */
    private double crossProduct2D(_VertexNode2D v1, _VertexNode2D v2) {
        return v1.x * v2.y - v1.y * v2.x;
    }

    private double dotProduct2D(_VertexNode2D v1, _VertexNode2D v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    private double dotProductNorm2D(_VertexNode2D a1, _VertexNode2D a2, _VertexNode2D b1, _VertexNode2D b2) {
        double temp;
        double[] v1, v2;

        v1 = new double[2];
        v2 = new double[2];
        v1[0] = a2.x - a1.x;
        v1[1] = a2.y - a1.y;
        v2[0] = b2.x - b1.x;
        v2[1] = b2.y - b1.y;
        temp = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1]);
        v1[0] = v1[0] / temp;
        v1[1] = v1[1] / temp;
        temp = Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1]);
        v2[0] = v2[0] / temp;
        v2[1] = v2[1] / temp;
        return v1[0] * v2[0] + v1[1] * v2[1];
    }

    /**
     * Insert a node between two nodes that define a line; between these nodes
     * could have other nodes (formed by other intersections), but the node are
     * inserted in the correct position.
     *
     * @param linkedList The linked list.
     * @param dllnVertNode node one.
     * @param dllnVertNodeNext node two.
     * @param nodeIntersec 2d point.
     * @return The inserted node.
     */
    private _DoubleLinkedListNode<_VertexNode2D> insertOrderedNodeBetweenTwoNodes(
        _CircDoubleLinkedList<_VertexNode2D> linkedList, _DoubleLinkedListNode<_VertexNode2D> dllnVertNode, _DoubleLinkedListNode<_VertexNode2D> dllnVertNodeNext, _VertexNode2D nodeIntersec) {
        int sign;
        _DoubleLinkedListNode<_VertexNode2D> dllnNode;

        dllnNode = dllnVertNode;
        if ( Math.abs(dllnVertNode.data.x - dllnVertNodeNext.data.x) > Math.abs(dllnVertNode.data.y - dllnVertNodeNext.data.y) ) {
            sign = (int) ((dllnVertNodeNext.data.x - dllnVertNode.data.x) / Math.abs(dllnVertNodeNext.data.x - dllnVertNode.data.x));
            while ( dllnNode.data.x * sign < nodeIntersec.x * sign && dllnNode != dllnVertNodeNext ) {
                dllnNode = dllnNode.next;
            }
        } else {
            sign = (int) ((dllnVertNodeNext.data.y - dllnVertNode.data.y) / Math.abs(dllnVertNodeNext.data.y - dllnVertNode.data.y));
            while ( dllnNode.data.y * sign < nodeIntersec.y * sign && dllnNode != dllnVertNodeNext ) {
                dllnNode = dllnNode.next;
            }
        }
        return linkedList.insertBefore(nodeIntersec, dllnNode);
    }

    /**
     * Finds intersection between two segments of line. When find coincident
     * points, only inP0 and inP2 are considered that make intersection.
     *
     * @param inP0
     * @param inP1
     * @param inP2
     * @param inP3
     * @param outPIntersec Is filled only if the intersection is not a
     * coincident point or points.
     * @param outCoincidentPoints Array of four booleans. Indicates if any point
     * is coincident with the opposite line(the points of the opposite line are
     * included).
     * @return True if find intersection. Only inP0 and inP2 are considered that
     * make intersection in the case of coincident points.
     */
    public boolean intersecLineLine2D(
        _VertexNode2D inP0, _VertexNode2D inP1, _VertexNode2D inP2, _VertexNode2D inP3, _VertexNode2D outPIntersec, boolean[] outCoincidentPoints) {
        double temp1, temp2;
        boolean vertical1, vertical2;
        double m1, b1, m2, b2;
        boolean intersec1, intersec2;
        boolean pointToPointCoincidence;
        boolean point0ToPoint2Coincidence;
        boolean point0ToPoint3Coincidence;
        boolean point2ToPoint1Coincidence;

        outCoincidentPoints[0] = false;
        outCoincidentPoints[1] = false;
        outCoincidentPoints[2] = false;
        outCoincidentPoints[3] = false;
        pointToPointCoincidence = false;
        point0ToPoint2Coincidence = false;
        point0ToPoint3Coincidence = false;
        point2ToPoint1Coincidence = false;
        if ( Math.abs(inP0.x - inP2.x) < 0.0001 && Math.abs(inP0.y - inP2.y) < 0.0001 ) {
            outCoincidentPoints[0] = true;
            outCoincidentPoints[2] = true;
            point0ToPoint2Coincidence = true;
            pointToPointCoincidence = true;
        }
        if ( Math.abs(inP0.x - inP3.x) < 0.0001 && Math.abs(inP0.y - inP3.y) < 0.0001 ) {
            outCoincidentPoints[0] = true;
            outCoincidentPoints[3] = true;
            point0ToPoint3Coincidence = true;
            pointToPointCoincidence = true;
        }
        if ( Math.abs(inP1.x - inP2.x) < 0.0001 && Math.abs(inP1.y - inP2.y) < 0.0001 ) {
            outCoincidentPoints[1] = true;
            outCoincidentPoints[2] = true;
            point2ToPoint1Coincidence = true;
            pointToPointCoincidence = true;
        }
        if ( Math.abs(inP1.x - inP3.x) < 0.0001 && Math.abs(inP1.y - inP3.y) < 0.0001 ) {
            outCoincidentPoints[1] = true;
            outCoincidentPoints[3] = true;
            pointToPointCoincidence = true;
        }
//        if(pointToPointCoincidence)
//            return point0ToPoint2Coincidence;
        vertical1 = false;
        vertical2 = false;
        temp1 = inP1.x - inP0.x;
        temp2 = inP3.x - inP2.x;
        if ( temp1 > -0.00001 && temp1 < 0.00001 ) {
            vertical1 = true;
        }
        if ( temp2 > -0.00001 && temp2 < 0.00001 ) {
            vertical2 = true;
        }
        if ( vertical1 && vertical2 ) {
            if ( Math.abs(inP0.x - inP2.x) < 0.0001 ) {
                if ( (inP3.y - inP0.y) * (inP0.y - inP2.y) > 0 ) {
                    outCoincidentPoints[0] = true;
                }
                if ( (inP1.y - inP2.y) * (inP2.y - inP0.y) > 0 ) {
                    outCoincidentPoints[2] = true;
                }
                if ( (inP3.y - inP1.y) * (inP1.y - inP2.y) > 0 ) {
                    outCoincidentPoints[1] = true;
                }
                if ( (inP1.y - inP3.y) * (inP3.y - inP0.y) > 0 ) {
                    outCoincidentPoints[3] = true;
                }
                if ( pointToPointCoincidence ) {
                    if ( point0ToPoint2Coincidence ) {
                        return true;
                    } else {
                        return (outCoincidentPoints[0] && !point0ToPoint3Coincidence) || (outCoincidentPoints[2] && !point2ToPoint1Coincidence);
                    }
                } else {
                    return outCoincidentPoints[0] || outCoincidentPoints[2];
                }
            } else {
                return point0ToPoint2Coincidence;
            }
        }
        m1 = 0;
        b1 = 0;
        m2 = 0;
        b2 = 0;//To avoid parser error.
        if ( !vertical1 ) {
            m1 = (inP1.y - inP0.y) / (inP1.x - inP0.x);
            b1 = inP0.y - m1 * inP0.x;
        }
        if ( !vertical2 ) {
            m2 = (inP3.y - inP2.y) / (inP3.x - inP2.x);
            b2 = inP2.y - m2 * inP2.x;
        }
        if ( vertical1 ) {
            if ( (inP1.y - inP3.y) * (inP3.y - inP0.y) > 0 && Math.abs(inP0.x - inP3.x) < 0.0001 ) {
                outCoincidentPoints[3] = true;
            }
            if ( (inP1.y - inP2.y) * (inP2.y - inP0.y) > 0 && Math.abs(inP0.x - inP2.x) < 0.0001 ) {
                outCoincidentPoints[2] = true;
                if ( pointToPointCoincidence ) {
                    return point0ToPoint2Coincidence;
                } else {
                    return true;
                }
            }
            outPIntersec.x = inP0.x;
            outPIntersec.y = m2 * outPIntersec.x + b2;
            if ( (inP3.x - outPIntersec.x) * (outPIntersec.x - inP2.x) > 0 ) {
                if ( Math.abs(inP1.y - outPIntersec.y) < 0.0001 ) {
                    outCoincidentPoints[1] = true;
                }
                if ( Math.abs(inP0.y - outPIntersec.y) < 0.0001 ) {
                    outCoincidentPoints[0] = true;
                    if ( pointToPointCoincidence ) {
                        return point0ToPoint2Coincidence;
                    } else {
                        return true;
                    }
                }
                if ( (inP1.y - outPIntersec.y) * (outPIntersec.y - inP0.y) > 0 ) {
                    if ( pointToPointCoincidence ) {
                        return point0ToPoint2Coincidence;
                    } else {
                        return true;
                    }
                }
            }
            return point0ToPoint2Coincidence;
        }
        if ( vertical2 ) {
            if ( (inP3.y - inP1.y) * (inP1.y - inP2.y) > 0 && Math.abs(inP2.x - inP1.x) < 0.0001 ) {
                outCoincidentPoints[1] = true;
            }
            if ( (inP3.y - inP0.y) * (inP0.y - inP2.y) > 0 && Math.abs(inP2.x - inP0.x) < 0.0001 ) {
                outCoincidentPoints[0] = true;
                if ( pointToPointCoincidence ) {
                    return point0ToPoint2Coincidence;
                } else {
                    return true;
                }
            }
            outPIntersec.x = inP2.x;
            outPIntersec.y = m1 * outPIntersec.x + b1;
            if ( (inP1.x - outPIntersec.x) * (outPIntersec.x - inP0.x) > 0 ) {
                if ( Math.abs(inP3.y - outPIntersec.y) < 0.0001 ) {
                    outCoincidentPoints[3] = true;
                }
                if ( Math.abs(inP2.y - outPIntersec.y) < 0.0001 ) {
                    outCoincidentPoints[2] = true;
                    if ( pointToPointCoincidence ) {
                        return point0ToPoint2Coincidence;
                    } else {
                        return true;
                    }
                }
                if ( (inP3.y - outPIntersec.y) * (outPIntersec.y - inP2.y) > 0 ) {
                    if ( pointToPointCoincidence ) {
                        return point0ToPoint2Coincidence;
                    } else {
                        return true;
                    }
                }
            }
            return point0ToPoint2Coincidence;
        }
        if ( Math.abs(m2 - m1) < 0.00001 ) { //The two lines are parallel.
            if ( Math.abs(b1 - b2) < 0.0001 ) {
                if ( (inP3.x - inP0.x) * (inP0.x - inP2.x) >= 0 ) {
                    outCoincidentPoints[0] = true;
                }
                if ( (inP3.x - inP1.x) * (inP1.x - inP2.x) >= 0 ) {
                    outCoincidentPoints[1] = true;
                }
                if ( (inP1.x - inP2.x) * (inP2.x - inP0.x) >= 0 ) {
                    outCoincidentPoints[2] = true;
                }
                if ( (inP1.x - inP3.x) * (inP3.x - inP0.x) >= 0 ) {
                    outCoincidentPoints[3] = true;
                }
                if ( pointToPointCoincidence ) {
                    if ( point0ToPoint2Coincidence ) {
                        return true;
                    } else {
                        return (outCoincidentPoints[0] && !point0ToPoint3Coincidence) || (outCoincidentPoints[2] && !point2ToPoint1Coincidence);
                    }
                } else {
                    return outCoincidentPoints[0] || outCoincidentPoints[2];
                }
            }
            return point0ToPoint2Coincidence;
        }
        //The two lines are not parallel.
        intersec1 = false;
        intersec2 = false;
        if ( m1 < 1 ) {//For precision purposes.
            outPIntersec.x = (b1 - b2) / (m2 - m1);
            outPIntersec.y = m1 * outPIntersec.x + b1;

            if ( (inP1.x - outPIntersec.x) * (outPIntersec.x - inP0.x) >= 0 ) {
                intersec1 = true;
            }
        } else {
            outPIntersec.y = (b1 * m2 - b2 * m1) / (m2 - m1);
            outPIntersec.x = (outPIntersec.y - b1) / m1;
            if ( (inP1.y - outPIntersec.y) * (outPIntersec.y - inP0.y) >= 0 ) {
                intersec1 = true;
            }
        }
        if ( m2 < 1 ) {//For precision purposes.
            if ( (inP3.x - outPIntersec.x) * (outPIntersec.x - inP2.x) >= 0 ) {
                intersec2 = true;
            }
        } else {
            if ( (inP3.y - outPIntersec.y) * (outPIntersec.y - inP2.y) >= 0 ) {
                intersec2 = true;
            }
        }
        if ( intersec1 && intersec2 ) {
            if ( Math.abs(inP0.x - outPIntersec.x) < 0.0001 && Math.abs(inP0.y - outPIntersec.y) < 0.0001 ) {
                outCoincidentPoints[0] = true;
                if ( pointToPointCoincidence ) {
                    return point0ToPoint2Coincidence;
                } else {
                    return true;
                }
            }
            if ( Math.abs(inP1.x - outPIntersec.x) < 0.0001 && Math.abs(inP1.y - outPIntersec.y) < 0.0001 ) {
                outCoincidentPoints[1] = true;
                return point0ToPoint2Coincidence;
            }
            if ( Math.abs(inP2.x - outPIntersec.x) < 0.0001 && Math.abs(inP2.y - outPIntersec.y) < 0.0001 ) {
                outCoincidentPoints[2] = true;
                if ( pointToPointCoincidence ) {
                    return point0ToPoint2Coincidence;
                } else {
                    return true;
                }
            }
            if ( Math.abs(inP3.x - outPIntersec.x) < 0.0001 && Math.abs(inP3.y - outPIntersec.y) < 0.0001 ) {
                outCoincidentPoints[3] = true;
                return point0ToPoint2Coincidence;
            }
            if ( pointToPointCoincidence ) {
                return point0ToPoint2Coincidence;
            } else {
                return true;
            }
        } else {
            return point0ToPoint2Coincidence;
        }
    }

    /**
     * @return the clipPolyWA who has the clip points after the clipping
     * process.
     */
    public _Polygon2DWA getClipPolyWA() {
        return clipPolyWA;
    }

    /**
     * @return the subjectPolyWA who has the clip points after the clipping
     * process.
     */
    public _Polygon2DWA getSubjectPolyWA() {
        return subjectPolyWA;
    }
}
