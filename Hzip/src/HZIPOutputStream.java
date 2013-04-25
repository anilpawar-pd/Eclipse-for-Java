import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.Math;

/**
 * Writes to HZIPOutputStream are compressed and
 * sent to the output stream being wrapped.
 * No writing is actually done until close.
 */
public class HZIPOutputStream extends OutputStream
{
    public HZIPOutputStream( OutputStream out ) throws IOException
    {
        dout = new DataOutputStream( out );
    }
    
    public void write( int ch ) throws IOException
    { 
        byteOut.write( ch );
    }
    
    public void close( ) throws IOException
    {
        byte [ ] theInput = byteOut.toByteArray( );
        ByteArrayInputStream byteIn = new ByteArrayInputStream( theInput );
        

        
        CharCounter countObj = new CharCounter( byteIn );//传入byte字符，生成CharCount类countObj，类中包括了每个字符出现的频率
        byteIn.close( );
        
        HuffmanTree codeTree = new HuffmanTree( countObj );//根据countObj生成树
        codeTree.writeEncodingTable( dout );//dout=>hout>fout
        
        BitOutputStream bout = new BitOutputStream( dout );
        long a=System.nanoTime();
        int m=0;

        for( int i = 0; i < theInput.length; i++ )
        {
            //int ch=theInput[ i ] & (0xff) ;                   
            //System.out.print((char)ch);
            //System.out.print(" is coded as ");
            int [] result=codeTree.getCode( theInput[ i ] & (0xff) );
            m=m+result.length;
            bout.writeBits( result );//将生成的霍夫曼二进制码写到压缩文件流中
        }
        //System.out.print("EOF is coded as ");
        int [] eof=codeTree.getCode( BitUtils.EOF );
        bout.writeBits(eof);//将eof对应的二进制码写到文件末尾
        m=m+eof.length;
        System.out.print("After Compressing the Size is ");
        System.out.print(m);
        System.out.print(" Bits. \n");
        bout.close( );//关闭输出的压缩文件
        byteOut.close( );//关闭输出流
        long b=System.nanoTime();
        System.out.print("The Time for Compression Is ");
        System.out.print(b-a);
        System.out.print(" ns\n");

        /*
         * 输出码表，ci为阿斯克码，intch[ci]为ci的对应编码
         */
        int mother=0;
        int codelength[]=new int[257];
        for( int ci=0;ci<256;ci++ )//统计每个阿斯克码的码长，输出每个阿斯克码被编成的二进制形式
        {
        	mother=countObj.theCounts[ci]+mother;
        	int [] intch=codeTree.getCode(ci);	
        	if (intch!=null)
        	{
        		codelength[ci]=intch.length;
        		System.out.print((char)ci);
        		//System.out.print(ci);
        		System.out.print(" is Coded as ");
        		for(int l=0;l<intch.length;l++)
        			System.out.print(intch[l]);
        		System.out.println();
        	}
        	else 
        		codelength[ci]=0;
        
        }
        
        /*
         * 计算平均码长
         * 计算信息熵
         */
        int RL=0;
        double H=0;
        double p=0;
        for( int rh=0;rh<256;rh++)
        {
        	if(countObj.theCounts[rh]!=0)
        	{
        		RL=RL+codelength[rh]*countObj.theCounts[rh];//码长*频次
        		p=countObj.theCounts[rh];
        		p=p/mother;									//每个码的概率
        		H=H-p*Math.log(p)/Math.log((double)2);		//求信息熵
        	}
        }
        double AverageRL=(double)RL/mother;	//平均码长
        System.out.print("The Average Length of HuffmanCode is ");
        System.out.print(AverageRL);
        System.out.print(" Bit per Symbol. \n");
        System.out.print("The Entropy of This Text is ");
        System.out.print(H);
        System.out.print(" Bit.\n");
        //System.out.print("The Difference is ");
        //System.out.print(AverageRL-H);
    }
    
    private ByteArrayOutputStream byteOut = new ByteArrayOutputStream( );//暂存文件中字符的缓冲流，可转化成数组以便操作
    private DataOutputStream dout;
}
