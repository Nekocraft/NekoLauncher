/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher.utils;

import sun.misc.BASE64Decoder;

public class Base64Utils {
 
 public static String getBASE64(byte[] b) {
  String s = null;
  if (b != null) {
   s = new sun.misc.BASE64Encoder().encode(b);
  }
  return s;
 }
 
 public static byte[] getFromBASE64(String s) {
  byte[] b = null;
  if (s != null) {
   BASE64Decoder decoder = new BASE64Decoder();
   try {
    b = decoder.decodeBuffer(s);
    return b;
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
  return b;
 }
 public static void main(String[] args){
     System.out.println(getBASE64("hello".getBytes()));
     System.out.println(new String(getFromBASE64(getBASE64("hello".getBytes()))));
 }
}