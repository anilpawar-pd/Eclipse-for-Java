import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.*;
import java.util.PriorityQueue;

interface BitUtils
{
    public static final int BITS_PER_BYTES = 8;
    public static final int DIFF_BYTES = 256;
    public static final int EOF = 256;   
}

// BitInputStream class: Bit-input stream wrapper class.
//
// CONSTRUCTION: with an open InputStream.
//
// ******************PUBLIC OPERATIONS***********************
// int readBit( )              --> Read one bit as a 0 or 1
// void close( )               --> Close underlying stream

class BitInputStream
{
    public BitInputStream( InputStream is )
    {
        in = is;
        bufferPos = BitUtils.BITS_PER_BYTES;
    }
    
    public int readBit( ) throws IOException
    {
        if ( bufferPos == BitUtils.BITS_PER_BYTES )
        {
            buffer = in.read( );
            if( buffer == -1 )
                return -1;
            bufferPos = 0;
        }
        
        return getBit( buffer, bufferPos++ );    
    }
    
    public void close( ) throws IOException
    {
        in.close( );
    }
    
    private static int getBit( int pack, int pos )
    {
        return ( pack & ( 1 << pos ) ) != 0 ? 1 : 0;
    }
    
    private InputStream in;
    private int buffer;
    private int bufferPos;
}    

// BitOutputStream class: Bit-output stream wrapper class.
//
// CONSTRUCTION: with an open OutputStream.
//
// ******************PUBLIC OPERATIONS***********************
// void writeBit( val )        --> Write one bit (0 or 1)
// void writeBits( vald )      --> Write array of bits
// void flush( )               --> Flush buffered bits
// void close( )               --> Close underlying stream

class BitOutputStream
{
    public BitOutputStream( OutputStream os )
    {
        bufferPos = 0;
        buffer = 0;
        out = os;
    }
    
    public void writeBit( int val ) throws IOException
    {
        buffer = setBit( buffer, bufferPos++, val );
        if( bufferPos == BitUtils.BITS_PER_BYTES )
            flush( );
    }
    
    public void writeBits( int [ ] val ) throws IOException
    {
        for( int v : val )
            writeBit( v );
    }    
    
    public void flush( ) throws IOException
    {
        if( bufferPos == 0 )
            return;
        
        out.write( buffer );
        bufferPos = 0;
        buffer = 0;    
    }
    
    public void close( ) throws IOException
    {
        flush( );
        out.close( );
    }
    
    private int setBit( int pack, int pos, int val )
    {
        if( val == 1 )
            pack |= ( val << pos );
        return pack;
    }
    
    private OutputStream out;
    private int buffer;
    private int bufferPos;
}

// CharCounter class: A character counting class.
//
// CONSTRUCTION: with no parameters or an open InputStream.
//
// ******************PUBLIC OPERATIONS***********************
// int getCount( ch )           --> Return # occurrences of ch
// void setCount( ch, count )   --> Set # occurrences of ch
// ******************ERRORS**********************************
// No error checks.

class CharCounter
{
    public CharCounter( )
    {
    }
    
    public CharCounter( InputStream input ) throws IOException
    {
        int ch;
        int sum=0;
        
        while( ( ch = input.read( ) ) != -1 )        
        {
        	//System.out.print((char)ch);
            theCounts[ ch ]++;//theCounts��¼�˲�ͬ��˹������ֵĴ�����ch���±�պö�Ӧch����İ�˹���� 
        }
        //System.out.print("\n");
        for(int forpch=0;forpch<256;forpch++)
        {
        	sum=sum+theCounts[forpch];
        //	System.out.print((char)forpch);
        //	System.out.print(" occuring times: ");
        //	System.out.print(theCounts[forpch]);
        //	System.out.print("\n");
        }
    	System.out.print("The Size of Origin File is ");
    	System.out.print(sum*8);
    	System.out.print(" Bits. \n");

    }
    
    public int getCount( int ch )
    {
        return theCounts[ ch & 0xff ];
    }
    
    public void setCount( int ch, int count )
    {
        theCounts[ ch & 0xff ] = count;
    }
    
    public int [ ] theCounts = new int[ BitUtils.DIFF_BYTES + 1 ];
}

// Basic node in a Huffman coding tree.
class HuffNode implements Comparable<HuffNode>
{
    public int value;
    public int weight;
    
    public int compareTo( HuffNode rhs )
    {
        return weight - rhs.weight;
    }
    
    HuffNode left;
    HuffNode right;
    HuffNode parent;
    
    HuffNode( int v, int w, HuffNode lt, HuffNode rt, HuffNode pt )
    {
        value = v; weight = w; left = lt; right = rt; parent = pt;
    }
}

// Huffman tree class interface: manipulate huffman coding tree.
//
// CONSTRUCTION: with no parameters or a CharCounter object.
//
// ******************PUBLIC OPERATIONS***********************
// int [ ] getCode( ch )        --> Return code given character
// int getChar( code )          --> Return character given code
// void writeEncodingTable( out ) --> Write coding table to out
// void readEncodingTable( in ) --> Read encoding table from in
// ******************ERRORS**********************************
// Error check for illegal code.

class HuffmanTree
{
    public HuffmanTree( )
    {
        theCounts = new CharCounter( );
        root = null;
    }
    
    public HuffmanTree( CharCounter cc )
    {
        theCounts = cc;
        root = null;
        createTree( );
    }
    
    public static final int ERROR = -3;
    public static final int INCOMPLETE_CODE = -2;
    public static final int END = BitUtils.DIFF_BYTES;
    
    /**
     * Return the code corresponding to character ch.
     * (The parameter is an int to accomodate EOF).
     * If code is not found, return an array of length 0.
     */
    /**
     * ���ض�Ӧ�ַ��Ķ����Ʊ���
     */
    public int [ ] getCode( int ch )//��ÿ����˹�������
    {
        HuffNode current = theNodes[ ch ];
        if( current == null )//�жϸð�˹�����Ƿ�����ڻ���������
            return null;
            
        String v = "";
        codelen=0;
        HuffNode par = current.parent;//ѡȡ���ýڵ�ĸ��ڵ㣬������������ɡ�0����������Һ������ɡ�1��
        
        while ( par != null )
        {
            if( par.left == current )
                v = "0" + v;
            else
                v = "1" + v;
            codelen=codelen+1;
            current = current.parent;
            par = current.parent;//����һ�㣬�����жϸñ��0����1
        }
        
        int [ ] result = new int[ v.length( ) ];
        for( int i = 0; i < result.length; i++ )
        {
            result[ i ] = v.charAt( i ) == '0' ? 0 : 1;//����result���飬�����ڣ���Ŵ����ĸ��ڵ㵽�ð�˹�����Ҷ�ڵ�
        	//System.out.print(result[i]);
        }
            //����õĶ�������ֵ
        //System.out.print('\n');
        return result;
    }  
    
    /**
     * Get the character corresponding to code.
     */
    /**
     * ����ĳ����������������Ӧ���ַ�
     */
    public int getChar( String code )
    {
        HuffNode p = root;//�Ӹ��ڵ㿪ʼ
        for( int i = 0; p != null && i < code.length( ); i++ )
            if( code.charAt( i ) == '0' )//���ݶ��������־������������ķ���
                p = p.left;
            else
                p = p.right;
                
        if( p == null )
            return ERROR;
            
        return p.value;//����ֵvalue�м�¼��ASCII��ֵ            
    }
    
      // Write the encoding table using character counts
    /**
     * Writes an encoding table to an output stream.
     * Format is character, count (as bytes).
     * A zero count terminates the encoding table.
     */
    public void writeEncodingTable( DataOutputStream out ) throws IOException
    {
        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )
        {
            if( theCounts.getCount( i ) > 0 )
            {
                out.writeByte( i );//д��˹�����±굽buffer
                out.writeInt( theCounts.getCount( i ) );//дȨֵ��buffer
            }
        }
        out.writeByte( 0 );
        out.writeInt( 0 );
    }
    
    /**
     * Read the encoding table from an input stream in format
     * given above and then construct the Huffman tree.
     * Stream will then be positioned to read compressed data.
     */
    public void readEncodingTable( DataInputStream in ) throws IOException
    {
        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )
            theCounts.setCount( i, 0 );
        
        int ch;
        int num;
        
        for( ; ; )
        {
            ch = in.readByte( );
            num = in.readInt( );
            if( num == 0 )//�������ĳ�ַ���Ȩ��Ϊ0
                break;
            theCounts.setCount( ch, num );//���ö�Ӧ���ֳ��ֵ�Ƶ��
        }
        
        createTree( );//����theCount������������
    }
              
    /**
     * ����Huffman�����������
     */
    private void createTree( )
    {
        PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>( );//�������ȼ����У����������ָ��ʴ�С�ŷ�����
        
        for( int i = 0; i < BitUtils.DIFF_BYTES; i++ )//ɨ��ÿһ����˹��������
            if ( theCounts.getCount( i ) > 0 )//�жϸ������Ƿ���ֹ�����Ƶ��>0��
            {
                HuffNode newNode = new HuffNode( i,
                       theCounts.getCount( i ), null, null, null );//�Ե�i�����֣�����ȨֵΪƵ�ʵĻ������ڵ�
                theNodes[ i ] =  newNode;//���нڵ㱣����theNode������
                pq.add( newNode );//�ڵ���ӣ�������Ȩֵ��С˳����������
            }
            
        theNodes[ END ] = new HuffNode( END, 1, null, null, null );//Ϊ���������ַ������ڵ㣬�������
        pq.add( theNodes[ END ] );
        
        while( pq.size( ) > 1 )
        {
            HuffNode n1 = pq.remove( );
            HuffNode n2 = pq.remove( );//�Ӷ������˳�����Ȩֵ��С�Ĺ������ڵ�
            HuffNode result = new HuffNode( INCOMPLETE_CODE,
                                  n1.weight + n2.weight, n1, n2, null );//�����������ڵ��������ǵĸ��ڵ㣬ֵΪ-2
            n1.parent = n2.parent = result;//���ڵ���λ���ӽڵ���
            pq.add( result );//�����ڵ���뵽���У��ӽڵ��Ѿ����ڶ����У�
        }      
        
        root = pq.element( );//���������ĸ�
    }
    
    private CharCounter theCounts;
    private HuffNode [ ] theNodes = new HuffNode[ BitUtils.DIFF_BYTES + 1 ];
    private HuffNode root;
    public int codelen;
}
   

public class Hzip
{

    public static void compress( String inFile ) throws IOException
    {
        String compressedFile = inFile + ".huf";
        InputStream in = new BufferedInputStream(
                         new FileInputStream( inFile ) );
        OutputStream fout = new BufferedOutputStream(
                            new FileOutputStream( compressedFile ) );//foutָ����ѹ���ļ����ļ���
        HZIPOutputStream hzout = new HZIPOutputStream( fout );//
        int ch;
        while( ( ch = in.read( ) ) != -1 )
            hzout.write( ch );//��in��ָ���inFile�ļ��е�char���ζ���hout
        in.close( );
        hzout.close( );//close�����а�����huffman����Ĺ���     
    }
        
    public static void uncompress( String compressedFile ) throws IOException
    {
        String inFile;
        String extension;
        
        inFile = compressedFile.substring( 0, compressedFile.length( ) - 4 );//��ѹ���ļ����ļ���
        extension = compressedFile.substring( compressedFile.length( ) - 4 );//ȡ����ѹ���ļ�����չ�ļ���
        
        if( !extension.equals( ".huf" ) )//�ж��Ƿ�Ϊ.hufѹ���ļ����ǲ�Ҳ����ƥ�����
        {
            System.out.println( "Not a Compressed File!" );//���򱨴�
            return;
        }
        
        inFile += ".uc";    // for debugging, so as to not clobber original
        InputStream fin = new BufferedInputStream(
                          new FileInputStream( compressedFile ) );
        DataInputStream in = new DataInputStream( fin );
        HZIPInputStream hzin = new HZIPInputStream( in );
        
        OutputStream fout = new BufferedOutputStream(
                            new FileOutputStream( inFile ) );
        int ch;
        long c=System.nanoTime();
        //System.out.print("The result of uncompression: \n");
        while( ( ch = hzin.read( ) ) != -1 )
        {
            fout.write( ch );
            //System.out.print((char)ch);//�����ѹ���ı����
        }
        hzin.close( );
        long d=System.nanoTime();
        //System.out.println("\n");
        System.out.print("The Time for Uncompression Is ");
        System.out.print(d-c);
        System.out.print(" ns\n");
        fout.close( );
        
        /*
         * �������ļ���һ����һ���صĶԱ�
         * ��������������Լ���ͬ���þ�������
         */
        String or=inFile.substring(0, inFile.length( )-3);
        File originalfile=new File(or);
        File unzipfile=new File(inFile);
        FileReader inoriginal=new FileReader(originalfile);
        FileReader inunzip=new FileReader(unzipfile);
        BufferedReader br1 = new BufferedReader(inoriginal); 
        BufferedReader br2 = new BufferedReader(inunzip); 
        char forbr1[]=new char[1];
        char forbr2[]=new char[1];
        int error=0;
        while(br1.ready()&br2.ready())
        {
        	int temp1=br1.read(forbr1);
        	int temp2=br2.read(forbr2);
        	if (temp1!=temp2)
        	{
        		System.out.println("Error!!!");
        		error++;
        		System.out.print((char)temp1);
        		System.out.print((char)temp2);
        		System.out.println();
        	}

        }
        System.out.print("There are ");
        System.out.print(error);
        System.out.print(" Difference(s) between the Original and Uncompressed File.");
        br1.close();
        br2.close();
        inoriginal.close();
        inunzip.close();
        
        
    }

    public static void main( String [ ] args ) throws IOException
    {
        if( args.length < 2 )//����arg�����ϱ�׼�������׼�����棩
        {
            System.out.println( "Usage: java Hzip -[cu] files" );
            return;
        }
        
        String option = args[ 0 ];//����arg��ָ��ѹ�����ѹ������-c��ʾѹ����-u��ʾ��ѹ�����Լ�����������ļ���
        for( int i = 1; i < args.length; i++ )
        {
            String nextFile = args[ i ];
            if( option.equals( "-c" ) )
                compress( nextFile );
            else if( option.equals( "-u" ) )
                uncompress( nextFile );
            else
            {
                System.out.println( "Usage: java Hzip -[cu] files" );
                return;
            }
        }
    }
}
