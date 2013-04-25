
#pragma warning (disable: 4786)

#include <math.h>
#include <stdlib.h>

#ifdef USE_DOT_H
    #include <iostream.h>
    #include <fstream.h>
    #define USE_STR_DOT_H
#else
    #include <fstream>
    #include <iostream>
    #if !defined( __BORLANDC__ ) || __BORLANDC__ >= 0x0530
        using namespace std;
    #else
        #define USE_STR_DOT_H
    #endif
#endif


#ifndef SAFE_STL
    #include <map>
    #include <vector>
    #include <string>
    #include <queue>
    #include <functional>
    #include <algorithm>
    using namespace std;
#else
    #include "map.h"
    #include "vector.h"
    #include "string.h"
    #include "queue.h"
    #include "functional.h"
    #include "algorithm.h"
    #include "StartConv.h"
#endif


#include "Wrapper.h"

class ibstream
{
  public:
    ibstream( istream & is );

    int readBit( );
    istream & getInputStream( ) const;

  private:
    istream & in;
    char buffer;
    int bufferPos;
};


class obstream
{
  public:
    obstream( ostream & os );
    ~obstream( );

    void writeBit( int val );
    void writeBits( const vector<int> & val );
    ostream & getOutputStream( ) const;
    void flush( );

private:
    ostream & out;
    char buffer;
    int bufferPos;
};



class CharCounter
{
  public:
    CharCounter( );
    CharCounter( istream & input );

    int getCount( char ch );
    void setCount( char ch, int count );

  private:
    map<char,int,less<char> > theCounts;
};


struct HuffNode
{
    HuffNode *left;
    HuffNode *right;
    HuffNode *parent;
    int value;
    int weight;

    HuffNode( int v, int w, HuffNode *lt, HuffNode *rt, HuffNode *pt )
      : value( v ), weight( w ), left( lt ), right( rt ), parent( pt ) { }
};


class HuffmanTree
{
  public:
    HuffmanTree( );
    HuffmanTree( const CharCounter & cc );

    enum { ERROR = -3, INCOMPLETE_CODE = -2, END = -1 };

      // Here, vector<int> is usable by ibstream and obstreams
    vector<int> getCode( int ch );
    int getChar( const vector<int> & code ) const;

      // Write the encoding table using character counts
    void writeEncodingTable( ostream & out );
    void readEncodingTable( istream & in );

  private:
    CharCounter theCounts;
    map<int,HuffNode *,less<int> > theNodes;
    HuffNode *root;

    void createTree( );
    vector<int> getCode( HuffNode *current );
};

class Compressor
{
  public:
    static void compress( const string & inFile );
    static void uncompress( const string & compressedFile );
};

static const int BITS_PER_CHAR = 8;
static const int DIFF_CHARS = 256;
static const int READ_MODE = ios::in | ios::binary;
static const int WRITE_MODE = ios::out | ios::binary;

int getBit( char pack, int pos )
{
    return ( pack & ( 1 << pos ) ) ? 1 : 0;
}

void setBit( char & pack, int pos, int val )
{
    if( val == 1 )
        pack |= ( val << pos );
}

ibstream::ibstream( istream & is ) : bufferPos( BITS_PER_CHAR ), in( is )
{
}

int ibstream::readBit( )
{
    if( bufferPos == BITS_PER_CHAR )
    {
        in.get( buffer );
        if( in.eof( ) )
            return EOF;
        bufferPos = 0;
    }

    return getBit( buffer, bufferPos++ );
}

istream & ibstream::getInputStream( ) const
{
    return in;
}


obstream::obstream( ostream & os ) : bufferPos( 0 ), buffer( 0 ), out( os )
{
}

obstream::~obstream( )
{
    flush( );
}

void obstream::flush( )
{
    if( bufferPos == 0 )
        return;

    out.put( buffer );
    bufferPos = 0;
    buffer = 0;
}

void obstream::writeBit( int val )
{
    setBit( buffer, bufferPos++, val );
    if( bufferPos == BITS_PER_CHAR )
        flush( );
}

void obstream::writeBits( const vector<int> & val )
{
    for( int i = 0; i < val.size( ); i++ )
        writeBit( val[ i ] );
}

ostream & obstream::getOutputStream( ) const
{
    return out;
}



CharCounter::CharCounter( )
{
}

CharCounter::CharCounter( istream & input )
{
    char ch;
    while( !input.get( ch ).eof( ) )
        theCounts[ ch ]++;
}


int CharCounter::getCount( char ch )
{
    return theCounts[ ch ];
}

void CharCounter::setCount( char ch, int count )
{
    theCounts[ ch ] = count;
}


HuffmanTree::HuffmanTree( const CharCounter & cc ) : theCounts( cc )
{
    root = NULL;
    createTree( );
}

HuffmanTree::HuffmanTree( )
{
    root = NULL;
}

vector<int> HuffmanTree::getCode( int ch )
{
    if( root == NULL )
        return vector<int>( );
    return getCode( theNodes[ ch ] );
}

vector<int> HuffmanTree::getCode( HuffNode *current )
{
    vector<int> v;
    HuffNode *par = current->parent; 

    while( par != NULL )
    {
        if( par->left == current )
            v.push_back( 0 );
        else
            v.push_back( 1 );
        current = current->parent;
        par = current->parent; 
    }
    reverse( v.begin( ), v.end( ) );
    return v;
}

int HuffmanTree::getChar( const vector<int> & code ) const
{
    HuffNode *p = root;
    for( int i = 0; p != NULL && i < code.size( ); i++ )
        if( code[ i ] == 0 )
            p = p->left;
        else
            p = p->right;

    if( p == NULL )
        return ERROR;
    return p->value;
}

void HuffmanTree::writeEncodingTable( ostream & out )
{
    for( int i = 0; i < DIFF_CHARS; i++ )
        if( theCounts.getCount( i ) > 0 )
            out << static_cast<char>( i ) << theCounts.getCount( i ) << endl;
    out << '\0' << 0 << endl;
}

void HuffmanTree::readEncodingTable( istream & in )
{
    for( int i = 0; i < DIFF_CHARS; i++ )
        theCounts.setCount( i, 0 );

    char ch;
    int num;
    char nl;

    for( ; ; )
    {
        in.get( ch );
        in >> num;
        in.get( nl );
        if( num == 0 )
            break;
        theCounts.setCount( ch, num );
    }
    createTree( );
}


bool operator< ( const HuffNode & lhs, const HuffNode & rhs )
{
    return lhs.weight > rhs.weight;
}

void HuffmanTree::createTree( )
{
    priority_queue<Pointer<HuffNode>, vector<Pointer<HuffNode> >,
                   less<Pointer<HuffNode> > > pq;

    for( int i = 0; i < DIFF_CHARS; i++ )
        if( theCounts.getCount( i ) > 0 )
        {
            HuffNode *newNode = new HuffNode( i, theCounts.getCount( i ), NULL, NULL, NULL );
            theNodes[ i ] = newNode;
            pq.push( Pointer<HuffNode>( newNode ) );
        }

    theNodes[ END ] = new HuffNode( END, 1, NULL, NULL, NULL );
    pq.push( Pointer<HuffNode>( theNodes[ END ] ) );

    while( pq.size( ) > 1 )
    {
        HuffNode *n1 = pq.top( ); pq.pop( );
        HuffNode *n2 = pq.top( ); pq.pop( );
        HuffNode *result = new HuffNode( INCOMPLETE_CODE, n1->weight + n2->weight, n1, n2, NULL );
        n1->parent = n2->parent = result;
        pq.push( Pointer<HuffNode>( result ) );
    }

    root = pq.top( );
}


void Compressor::compress( const string & inFile )
{
    string compressedFile = inFile + ".huf";
    ifstream in( inFile.c_str( ), READ_MODE );

    CharCounter countObj( in );
    HuffmanTree codeTree( countObj );

    ofstream out( compressedFile.c_str( ), WRITE_MODE );
    codeTree.writeEncodingTable( out );
    obstream bout( out );

    in.clear( ); in.seekg( 0, ios::beg ); // Rewind the stream

    char ch;
    while( in.get( ch ) )
        bout.writeBits( codeTree.getCode( ch & (0xff) ) );
 
    bout.writeBits( codeTree.getCode( EOF ) );
}

void Compressor::uncompress( const string & compressedFile )
{
    int i;
    string inFile;
    string extension;

    for( i = 0; i < compressedFile.length( ) - 4; i++ )
        inFile += compressedFile[ i ];

    for( ; i < compressedFile.length( ); i++ )
        extension += compressedFile[ i ];

    if( extension != ".huf" )
    {
        cerr << "Not a compressed file" << endl;
        return;
    }

    inFile += ".uc";  // for debugging, so as to not clobber original
    ifstream in( compressedFile.c_str( ), READ_MODE );
    ofstream out( inFile.c_str( ), WRITE_MODE );

    HuffmanTree codeTree;
    codeTree.readEncodingTable( in );

    ibstream bin( in );
    vector<int> bits;
    int bit;
    int decode;

    for( ; ; )
    {
        bit = bin.readBit( );
        bits.push_back( bit );

        decode = codeTree.getChar( bits );
        if( decode == HuffmanTree::INCOMPLETE_CODE )
            continue;
        else if( decode == HuffmanTree::ERROR )
        {
            cerr << "Error decoding!" << endl;
            break;
        }
        else if( decode == HuffmanTree::END )
            break;
        else
        {
            out.put( static_cast<char>( decode ) );
            bits.resize( 0 );
        }
    }
}

int main( int argc, char *argv[] )
{
    if( argc < 3 )
    {
        cerr << "Usage: " << argv[0] << " -[cu] files" << endl;
        return 1;
    }

    string option  = argv[ 1 ];

    for( int i = 2; i < argc; i++ )
    {
        string nextFile = argv[ i ];
        if( option == "-c" )
            Compressor::compress( nextFile );
        else if( option == "-u" )
            Compressor::uncompress( nextFile );
        else
        {
            cerr << "Usage: " << argv[0] << " -[cu] files" << endl;
            return 1;
        }
    }

    return 0;
}


#ifdef SAFE_STL
    #include "EndConv.h"
#endif
