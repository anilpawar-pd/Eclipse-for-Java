import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;

/**
 * HZIPInputStream wraps an input stream.
 * read returns an uncompressed byte from the
 * wrapped input stream.
 */
public class HZIPInputStream extends InputStream
{
    public HZIPInputStream( InputStream in ) throws IOException
    {
        DataInputStream din = new DataInputStream( in );
                
        codeTree = new HuffmanTree( );
        codeTree.readEncodingTable( din );//压缩文件流+对应码表+内置创建霍夫曼树函数=>霍夫曼树，用于解码
        
        bin = new BitInputStream( in );
    }
    
    public int read( ) throws IOException
    { 
        String bits = "";
        int bit;
        int decode;

        while( true )
        {
            bit = bin.readBit( );        
            if( bit == -1 )
                throw new IOException( "Unexpected EOF" );
                
            bits += bit;
        
            decode = codeTree.getChar( bits );
            if( decode == HuffmanTree.INCOMPLETE_CODE )
                continue;
            else if( decode == HuffmanTree.ERROR )
                throw new IOException( "Decoding error" );
            else if( decode == HuffmanTree.END )
                return -1;
            else
                return decode;
        }
    }
    
    public void close( ) throws IOException
    {
        bin.close( );
    }
    
    private BitInputStream bin;
    private HuffmanTree codeTree;
}
