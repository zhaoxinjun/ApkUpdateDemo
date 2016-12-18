package user.com.apkupdatedemo;

/**
 * 1.获取服务器IP地址
 * 2.@author:zhaoxinjun
 * 3.@  2016/12/18.
 */


public class GetServerUrl{
    //这里用的是本地的JAVAEE工程--tomcat服务器，可根据实际情况修改。
    static String url="http://169.254.112.92:8080";
    public static String getUrl() {
        return url;
    }
}

