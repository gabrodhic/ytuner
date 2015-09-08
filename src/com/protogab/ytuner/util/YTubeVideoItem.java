package com.protogab.ytuner.util;



import java.io.Serializable;
import java.text.DecimalFormat;

public class YTubeVideoItem
    implements Serializable
{

    public static String Html_enc = "";
    public String DownloadUrl;
    public String Extention;
    public String Publish_Video;
    public String Title;
    public String Update_Video;
    public String Url;        
    public String VedioLeantgh;
    public long VedioLeantgh_val;
    public long VedioSize;
    public String VedioTitle;
    public String ext;
    public int height_video;
    public String leangth;
    public int width_video;
    public String quality;

    public YTubeVideoItem()
    {
        Title = "";
        leangth = "";
        Url = "";
        ext = "";
        Publish_Video = "";
        Update_Video = "";
        
        VedioLeantgh = "";
        VedioLeantgh_val = 0L;
        Extention = "UN";
        height_video = 0;
        width_video = 0;
        Title = "";
        VedioLeantgh = "";
        leangth = "";
        Url = "";
        ext = "";
        Publish_Video = "";
        Update_Video = "";
        quality = "";
        
    }

    private String formatSize(int i, int j)
    {
        String s;
        if (j >= 720)
        {
            s = " HD";
        } else
        {
            s = "";
        }
        return (new StringBuilder(String.valueOf(i))).append(" x ").append(j).append(s).toString();
    }

    private String formatSizeBinary(long l)
    {
        String as[] = {
            "Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"
        };
        Double double1 = Double.valueOf(l);
        int i = 0;
        do
        {
            if (double1.doubleValue() < 1024D || i >= as.length)
            {
                DecimalFormat decimalformat = new DecimalFormat("###.##");
                Object aobj[] = new Object[2];
                aobj[0] = decimalformat.format(double1).toString();
                aobj[1] = as[i];
                return String.format("%s %s", aobj);
            }
            double1 = Double.valueOf(double1.doubleValue() / 1024D);
            i++;
        } while (true);
    }

    public String DescribeVedio()
    {
        Object aobj[] = new Object[3];
        aobj[0] = Extention;
        aobj[1] = formatSize(width_video, height_video);
        aobj[2] = formatSizeBinary(VedioSize);
        return String.format("%s (%s) - (%s)", aobj);
    }

    public void SetQuality(String s, int i, int j, String q)
    {
        Extention = s;
        width_video = i;
        height_video = j;
        quality = q;
    }

    public Boolean getQuality()
    {
        if (DownloadUrl.contains("itag=5"))
        {
            SetQuality("flv", 320, 240, "");////////
        } else
        if (DownloadUrl.contains("itag=6"))
        {
            SetQuality("flv", 480, 360, "");//////
        } else
        if (DownloadUrl.contains("itag=17"))
        {
            SetQuality("3gp", 176, 144, "verylow");
        } else
        if (DownloadUrl.contains("itag=18"))
        {
            SetQuality("mp4", 480, 360, "medium");
        } else
        if (DownloadUrl.contains("itag=134"))
        {
            SetQuality("mp4", 480, 360, "medium");
        } else
        if (DownloadUrl.contains("itag=135"))
        {
            SetQuality("mp4", 640, 480, "mediumhigh");
        } else
        if (DownloadUrl.contains("itag=22"))
        {
            SetQuality("mp4", 1280, 720, "high");
        } else
        if (DownloadUrl.contains("itag=34"))
        {
            SetQuality("flv", 400, 226, "");//////
        } else
        if (DownloadUrl.contains("itag=35"))
        {
            SetQuality("mp4", 640, 480, "mediumhigh");
        } else
        if (DownloadUrl.contains("itag=36"))
        {
            SetQuality("3gp", 320, 240, "low");
        } else
        if (DownloadUrl.contains("itag=37") || DownloadUrl.contains("itag=137"))
        {
            SetQuality("mp4", 1920, 1080, "veryhigh");
        } else
        if (DownloadUrl.contains("itag=38"))
        {
            SetQuality("mp4", 2048, 1080, "veryhigh");
        } else
        if (DownloadUrl.contains("itag=43"))
        {
            SetQuality("webm", 480, 360, "");//???
        } else
        if (DownloadUrl.contains("itag=44"))
        {
            SetQuality("mp4", 640, 480, "mediumhigh");
        } else
        if (DownloadUrl.contains("itag=85"))
        {
            SetQuality("mp4", 1920, 1080, "veryhigh");
        } else
        if (DownloadUrl.contains("itag=45"))
        {
            SetQuality("webm", 1280, 720, "");/////////
        } else
        if (DownloadUrl.contains("itag=136"))
        {
            SetQuality("webm", 1280, 720, "");//?????
        } else
        if (DownloadUrl.contains("itag=141"))
        {
            SetQuality("mp4", 1280, 481, "");//????
        } else
        if (DownloadUrl.contains("itag=46"))
        {
            SetQuality("webm", 1920, 1080, "");///////
        } else
        if (DownloadUrl.contains("itag=82"))
        {
            SetQuality("mp4", 640, 360, "medium");
        } else
        if (DownloadUrl.contains("itag=83"))
        {
            SetQuality("mp4", 854, 680, "mediumhigh");
        } else
        if (DownloadUrl.contains("itag=84"))
        {
            SetQuality("mp4", 1280, 720, "high");
        } else
        if (DownloadUrl.contains("itag=85"))
        {
            SetQuality("3Dmp4", 1920, 1080, "");///???
        } else
        {
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(true);
    }

}
