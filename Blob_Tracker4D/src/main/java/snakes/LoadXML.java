package snakes;

import java.io.File;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.SetupImgLoader;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import spim.fiji.spimdata.SpimData2;
import spim.fiji.spimdata.XmlIoSpimData2;

public class LoadXML
{
public static SpimData2 loadSpimData( final File xml ) throws SpimDataException
{
XmlIoSpimData2 io = new XmlIoSpimData2( "" );
return io.load( xml.getAbsolutePath() );
}

public static void getImg( final SpimData2 spimData2, final int viewSetupId, final int timepointId )
{
SetupImgLoader< ? > l = spimData2.getSequenceDescription().getImgLoader().getSetupImgLoader( viewSetupId );
Object type = l.getImageType();
System.out.println( type.getClass().getSimpleName() );
RandomAccessibleInterval< ? > i = l.getImage( timepointId );
ImageJFunctions.show( (RandomAccessibleInterval<UnsignedShortType>)i );
}
public static void main( String[] args ) throws SpimDataException
{
SpimData2 d = loadSpimData( new File( "/home/preibisch/Documents/Microscopy/SPIM/Drosophila MS2-GFP//dataset.xml" ) );
getImg( d, 0, 50 );
}
}
