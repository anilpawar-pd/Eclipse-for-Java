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
        

        
        CharCounter countObj = new CharCounter( byteIn );//����byte�ַ�������CharCount��countObj�����а�����ÿ���ַ����ֵ�Ƶ��
        byteIn.close( );
        
        HuffmanTree codeTree = new HuffmanTree( countObj );//����countObj������
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
            bout.writeBits( result );//�����ɵĻ�������������д��ѹ���ļ�����
        }
        //System.out.print("EOF is coded as ");
        int [] eof=codeTree.getCode( BitUtils.EOF );
        bout.writeBits(eof);//��eof��Ӧ�Ķ�������д���ļ�ĩβ
        m=m+eof.length;
        System.out.print("After Compressing the Size is ");
        System.out.print(m);
        System.out.print(" Bits. \n");
        bout.close( );//�ر������ѹ���ļ�
        byteOut.close( );//�ر������
        long b=System.nanoTime();
        System.out.print("The Time for Compression Is ");
        System.out.print(b-a);
        System.out.print(" ns\n");

        /*
         * ������ciΪ��˹���룬intch[ci]Ϊci�Ķ�Ӧ����
         */
        int mother=0;
        int codelength[]=new int[257];
        for( int ci=0;ci<256;ci++ )//ͳ��ÿ����˹������볤�����ÿ����˹���뱻��ɵĶ�������ʽ
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
         * ����ƽ���볤
         * ������Ϣ��
         */
        int RL=0;
        double H=0;
        double p=0;
        for( int rh=0;rh<256;rh++)
        {
        	if(countObj.theCounts[rh]!=0)
        	{
        		RL=RL+codelength[rh]*countObj.theCounts[rh];//�볤*Ƶ��
        		p=countObj.theCounts[rh];
        		p=p/mother;									//ÿ����ĸ���
        		H=H-p*Math.log(p)/Math.log((double)2);		//����Ϣ��
        	}
        }
        double AverageRL=(double)RL/mother;	//ƽ���볤
        System.out.print("The Average Length of HuffmanCode is ");
        System.out.print(AverageRL);
        System.out.print(" Bit per Symbol. \n");
        System.out.print("The Entropy of This Text is ");
        System.out.print(H);
        System.out.print(" Bit.\n");
        //System.out.print("The Difference is ");
        //System.out.print(AverageRL-H);
    }
    
    private ByteArrayOutputStream byteOut = new ByteArrayOutputStream( );//�ݴ��ļ����ַ��Ļ���������ת���������Ա����
    private DataOutputStream dout;
}
