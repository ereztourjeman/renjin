/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.sexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class DoubleNaNTest {


  @Test
  public void test() {

    assertTrue("isNA(NA) #1", DoubleVector.isNA(DoubleVector.NA));
    assertTrue("isNaN(NaN)", Double.isNaN(DoubleVector.NaN));
    assertTrue("isNaN(NA)", Double.isNaN(DoubleVector.NA));
    assertTrue("isNA(NA) #2", DoubleVector.isNA(DoubleVector.NA));
    assertFalse("isNA(NaN)", DoubleVector.isNA(DoubleVector.NaN));

  }
  
  @Test
  public void bitsTest() {
    long na = 0x7ff00000000007A2L;
    long lowerWordMask = 0x00000000FFFFFFFFL;
    
    System.out.println(bits(na));
    System.out.println(bits(lowerWordMask));
    
    
    long naMasked = na & lowerWordMask;

    System.out.println(bits(naMasked));
    System.out.println(bits(1954L));
    
    assertThat(naMasked, equalTo(1954L));
    
    
  }
  
  @Test
  public void test2() {
    double x = DoubleVector.NA;

    assertTrue("isNA(x = NA) #1", DoubleVector.isNA(x));
    assertTrue("isNaN(x = NA)", Double.isNaN(x));
    assertTrue("isNA(x = NA) #2", DoubleVector.isNA(x));
  }
    
  private String bits(long x) {
    String bits = Long.toBinaryString(x);
    while(bits.length() < 64) {
      bits = "0" + bits;
    }
    StringBuilder formatted = new StringBuilder();
    for(int i=0;i<bits.length();++i) {
      if(i%4==0) {
        formatted.append(" ");
      }
      formatted.append(bits.charAt(i));
    }
    return formatted.toString();
  }

  private String bits(double x) {
    return bits(Double.doubleToRawLongBits(x));
  }

}