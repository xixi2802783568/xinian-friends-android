package cn.xinian.app;
final class Skin {
  static final String[] NAMES={"白茶","晴空","暮紫","夜航"};
  final int background,surface,field,ink,muted,accent,onAccent,line;final boolean dark;
  Skin(int index){dark=index==3;int[][] p={
    {0xfff7f5f1,0xffffffff,0xfff3f1ed,0xff292722,0xff7a756b,0xff9a573d,0xffffffff,0xffe6e1d9},
    {0xfff1f6fc,0xffffffff,0xffeaf1fa,0xff172c47,0xff687c96,0xff306bd3,0xffffffff,0xffdbe5f2},
    {0xfff7f3fa,0xffffffff,0xfff1eaf6,0xff342b42,0xff83738f,0xff81609e,0xffffffff,0xffe8deef},
    {0xff121820,0xff1d2530,0xff263140,0xffedf1f7,0xffa1afc1,0xffb7cdf6,0xff16243b,0xff344051}
  };int[] c=p[Math.max(0,Math.min(index,3))];background=c[0];surface=c[1];field=c[2];ink=c[3];muted=c[4];accent=c[5];onAccent=c[6];line=c[7];}
}
