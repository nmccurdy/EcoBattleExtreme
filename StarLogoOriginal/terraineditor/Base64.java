/********************************************************************
 * Copyright 2000 by the Massachusetts Institute of Technology.  
 * All rights reserved.
 *
 * Developed by Mitchel Resnick, Andrew Begel, and Brian Silverman at the Media
 * Laboratory, MIT, Cambridge, Massachusetts, with support from ???.
 * 
 * This distribution is approved by Nicholas Negroponte, Director of the Media
 * Laboratory, MIT.
 *
 * Permission to use, copy, or modify this software and its documentation for
 * educational and research purposes only and without fee is hereby granted,
 * provided that this copyright notice and the original authors' names appear
 * on all copies and supporting documentation.  If individual files are
 * separated from this distribution directory structure, this copyright notice
 * must be included.  For any other uses of this software, in original or
 * modified form, including but not limited to distribution in whole or in
 * part, specific prior permission must be obtained from MIT.  These programs
 * shall not be used, rewritten, or adapted as the basis of a commercial
 * software or hardware product without first obtaining appropriate licenses
 * from MIT.  MIT makes no representations about the suitability of this
 * software for any purpose.  It is provided "as is" without express or implied
 * warranty.
 *******************************************************************/

package terraineditor;
 
public class Base64
{
    private static final int LINEBREAK_LENGTH = 48;

    protected final static char[] enc_table =
    {
    //   0   1   2   3   4   5   6   7
        'A','B','C','D','E','F','G','H', // 0 
        'I','J','K','L','M','N','O','P', // 1 
        'Q','R','S','T','U','V','W','X', // 2 
        'Y','Z','a','b','c','d','e','f', // 3 
        'g','h','i','j','k','l','m','n', // 4 
        'o','p','q','r','s','t','u','v', // 5 
        'w','x','y','z','0','1','2','3', // 6 
        '4','5','6','7','8','9','+','/'  // 7 
    };

    protected final static byte[] dec_table = 
    { 
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
        -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    };
 
    public Base64() {}

    public static String encode(byte[] data)
    {
        return new String(encodeAsByteArray(data));
    }

    public static byte[] encodeAsByteArray(byte[] data)
    {
        int i = 0, j = 0;
        int len = data.length;
        int delta = len % 3;
        int outlen = ((len+LINEBREAK_LENGTH-1)/LINEBREAK_LENGTH)*2 +
            ((len+2)/3)*4 + (len == 0 ? 2 : 0);
        byte[] output = new byte[outlen];

        byte a, b, c;
        for (int count = len / 3; count > 0; count--)
        {
            a = data[i++];
            b = data[i++];
            c = data[i++];
            output[j++] = (byte) (enc_table[(a >>> 2) & 0x3F]); 
            output[j++] = (byte) (enc_table[((a << 4) & 0x30) + ((b >>> 4) & 0x0F)]); 
            output[j++] = (byte) (enc_table[((b << 2) & 0x3C) + ((c >>> 6) & 0x03)]); 
            output[j++] = (byte) (enc_table[c & 0x3F]); 

            if (i % LINEBREAK_LENGTH == 0) {
                output[j++] = (byte) '\r';
                output[j++] = (byte) '\n';
            }
        }

        if (delta == 1)
        {
            a = data[i++];
            output[j++] = (byte) (enc_table[(a >>> 2) & 0x3F]); 
            output[j++] = (byte) (enc_table[((a << 4) & 0x30)]); 
            output[j++] = (byte) '='; 
            output[j++] = (byte) '='; 
        }
        else if (delta == 2)
        {
            a = data[i++];
            b = data[i++];
            output[j++] = (byte) (enc_table[(a >>> 2) & 0x3F]); 
            output[j++] = (byte) (enc_table[((a << 4) & 0x30) + ((b >>> 4) & 0x0F)]); 
            output[j++] = (byte) (enc_table[((b << 2) & 0x3C)]); 
            output[j++] = (byte) '='; 
        }
        if (i == 0 || i % LINEBREAK_LENGTH != 0) {
            output[j++] = (byte) '\r';
            output[j++] = (byte) '\n';
        }

        if (j != outlen)
            throw new InternalError("Bug in Base64.java: incorrect length calculated for base64 output");

        return output;
    }

    public static byte[] decode(byte[] data) 
    {
        int padCount = 0;
        int i, len = data.length;
          int real_len = 0;

        for (i=len-1; i >= 0; --i)
        {
            if (data[i] > ' ')
                real_len++;

            if (data[i] == 0x3D) 
                padCount++;
        }

        if (real_len % 4 != 0)
            throw new IllegalArgumentException("Length not a multiple of 4 in Base64.decode()");

        int ret_len = (real_len/4)*3 - padCount;
        byte[] ret = new byte[ret_len];

        i = 0;
        byte[] t = new byte[4];
        int output_index = 0;
        int j = 0;
        t[0] = t[1] = t[2] = t[3] = 0x3D; 
        while (i < len)
        {
            byte c = data[i++];
            if (c > ' ')
                t[j++] = c;

            if (j == 4)
            {
                output_index += decode(ret, output_index, t[0], t[1], t[2], t[3]);
                j = 0;
                t[0] = t[1] = t[2] = t[3] = 0x3D; 
            }
        }
        if (j > 0)
            decode(ret, output_index, t[0], t[1], t[2], t[3]);

        return ret;
    }

    public static byte[] decode(String msg) throws IllegalArgumentException
    {
        return decode(msg.getBytes());
    }

    private static int decode(byte[] ret, int ret_off, byte a, byte b, byte c, byte d)
    {
        byte da = dec_table[a];
        byte db = dec_table[b];
        byte dc = dec_table[c];
        byte dd = dec_table[d];

        if (da == -1 || db == -1 || (dc == -1 && c != 0x3D) || (dd == -1 && d != 0x3D))
            throw new IllegalArgumentException("Invalid character [" +
                (a & 0xFF) + ", " + (b & 0xFF) + ", " + (c & 0xFF) + ", " + (d & 0xFF) + "] in Base64.decodeByteArray()");

        ret[ret_off++] = (byte) (da << 2 | db >>> 4);
        if (c == 0x3D) 
            return 1;
        ret[ret_off++] = (byte) (db << 4 | dc >>> 2);
        if (d == 0x3D) 
            return 2;
        ret[ret_off++] = (byte) (dc << 6 | dd);
        return 3;
    }
}
