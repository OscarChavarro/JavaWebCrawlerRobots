//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 18 2007 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.io.metadata;

import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import vsdk.toolkit.io.PersistenceElement;
import vsdk.toolkit.media.GeometryMetadata;
import vsdk.toolkit.media.ShapeDescriptor;
import vsdk.toolkit.media.FourierShapeDescriptor;
import vsdk.toolkit.media.PrimitiveCountShapeDescriptor;

public class ShapeDescriptorPersistence extends PersistenceElement
{
    private static final byte TYPE_ENDING                    = 0x01;
    private static final byte TYPE_STRING                    = 0x02;
    private static final byte TYPE_FOURIER_DESCRIPTOR        = 0x03;
    private static final byte TYPE_PRIMITIVECOUNT_DESCRIPTOR = 0x04;

    public static void
    exportGeometryMetadata(OutputStream writer, GeometryMetadata m)
        throws Exception
    {
        ArrayList<ShapeDescriptor> list;

        writeLongBE(writer, m.getId());
        exportByte(writer, TYPE_STRING);
        writeAsciiString(writer, m.getFilename());
        list = m.getDescriptors();
        exportDescriptorMetadata(writer, list);
        exportEnding(writer);
    }

    /**
    Returns null if gets error... can also return Exception
    */
    public static GeometryMetadata
    importGeometryMetadata(InputStream reader) throws Exception {
        GeometryMetadata m;
        byte subChunkId;
        byte chunkId;
        int bytesToSkip;
        double vector[];
        int i;
        ShapeDescriptor shapeDescriptor;
        String label;
        long metadataId;

        metadataId = readLongBE(reader);
        chunkId = importByte(reader);
        switch ( chunkId ) {
          case TYPE_STRING:
            m = new GeometryMetadata();
            m.setId(metadataId);
            m.setFilename(readAsciiString(reader));
            do {
                subChunkId = importByte(reader);
                switch( subChunkId ) {
                  case TYPE_ENDING:
                    break;
                  case TYPE_FOURIER_DESCRIPTOR:
                    label = readAsciiString(reader);
                    bytesToSkip = readSignedShortBE(reader);
                    vector = new double[bytesToSkip/4];
                    for ( i = 0; i < vector.length; i++ ) {
                        vector[i] = readFloatBE(reader);
                    }
                    shapeDescriptor = new FourierShapeDescriptor(label);
                    shapeDescriptor.setFeatureVector(vector);
                    m.getDescriptors().add(shapeDescriptor);
                    reader.skip(bytesToSkip - vector.length*4);
                    break;
                  case TYPE_PRIMITIVECOUNT_DESCRIPTOR:
                    label = readAsciiString(reader);
                    bytesToSkip = readSignedShortBE(reader);
                    vector = new double[bytesToSkip/4];
                    for ( i = 0; i < vector.length; i++ ) {
                        vector[i] = readFloatBE(reader);
                    }
                    shapeDescriptor = new PrimitiveCountShapeDescriptor(label);
                    shapeDescriptor.setFeatureVector(vector);
                    m.getDescriptors().add(shapeDescriptor);
                    reader.skip(bytesToSkip - vector.length*4);
                    break;
                  default:
                    bytesToSkip = readSignedShortBE(reader);
                    System.out.println("Skipping bytes: " + bytesToSkip);
                    reader.skip(bytesToSkip);
                    break;
                }
            } while ( subChunkId != TYPE_ENDING );
            break;
          default:
            System.err.println("ERROR importing database (wrong format " +
                chunkId + ")!");
            return null;
        }
        return m;
    }

    private static void
    exportByte(OutputStream writer, byte var)
        throws Exception
    {
        byte data[] = new byte[1];
        data[0] = var;
        writer.write(data, 0, data.length);
    }

    private static byte
    importByte(InputStream reader)
        throws Exception
    {
        byte data[] = new byte[1];
        reader.read(data, 0, data.length);
        return data[0];
    }

    private static void
    exportEnding(OutputStream writer)
        throws Exception
    {
        exportByte(writer, TYPE_ENDING);
    }

    private static void
    exportDescriptorMetadata(OutputStream writer,
                   ArrayList<ShapeDescriptor> inShapeDescriptorsArray)
                   throws Exception
    {
        int i, j;
        ShapeDescriptor s;
        double featureVector[];

        for ( i = 0; i < inShapeDescriptorsArray.size(); i++ ) {
            s = inShapeDescriptorsArray.get(i);
            //-----------------------------------------------------------------
            if ( s instanceof FourierShapeDescriptor ) {
                exportByte(writer, TYPE_FOURIER_DESCRIPTOR);
              }
              else if ( s instanceof PrimitiveCountShapeDescriptor ) {
                exportByte(writer, TYPE_PRIMITIVECOUNT_DESCRIPTOR);
              }
              else {
                System.out.println("Non registered class. Dumping skipped.");
                return;
            }

            //-----------------------------------------------------------------
            writeAsciiString(writer, s.getLabel());

            //-----------------------------------------------------------------
            featureVector = s.getFeatureVector();
            writeSignedShortBE(writer, featureVector.length*4); // Bytes to skip
            for ( j = 0; j < featureVector.length; j++ ) {
                writeFloatBE(writer, (float)featureVector[j]);
            }
        }
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
